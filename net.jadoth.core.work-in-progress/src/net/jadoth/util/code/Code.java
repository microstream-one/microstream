package net.jadoth.util.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.jadoth.chars.JadothChars;
import net.jadoth.chars.VarString;
import net.jadoth.typing.JadothTypes;
import net.jadoth.util.code.Field.FinalField;

public abstract class Code
{
	// CHECKSTYLE.OFF: ConstantName: keyword names are intentionally unchanged

	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final String VISIBILITY_private   = "private";
	private static final String VISIBILITY_protected = "protected";
	private static final String VISIBILITY_public    = "public";

	static final String toLowerCaseFirstLetter(final String s)
	{
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	static final String toUpperCaseFirstLetter(final String s)
	{
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	static final VarString appendOverride(final VarString vs, final int level)
	{
		return vs.lf().repeat(level, '\t').add("@Override");
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////


	static final VarString sectionHeader(final VarString vs, final int level, final String title)
	{
		return vs
		.lf().tab(level).repeat(76, '/') // 76 plus 4 for reasonable tab size yields 80 width
		.lf().tab(level).repeat(2, '/').blank().add(title).blank().repeat(2, '/')
		.lf().tab(level).repeat(6 + title.length() - 1, '/')
		;
	}


	public static final Visibility private$ = new Visibility()
	{
		@Override
		public VarString assemble(final VarString vs)
		{
			return vs.add(VISIBILITY_private).blank();
		}
	};

	public static final Visibility DEFAULT = new Visibility()
	{
		@Override
		public VarString assemble(final VarString vs)
		{
			return vs;
		}
	};

	public static final Visibility protected$ = new Visibility()
	{
		@Override
		public VarString assemble(final VarString vs)
		{
			return vs.add(VISIBILITY_protected).blank();
		}
	};

	public static final Visibility public$ = new Visibility()
	{
		@Override
		public VarString assemble(final VarString vs)
		{
			return vs.add(VISIBILITY_public).blank();
		}
	};
	
	public static final FinalField Final(final String typeName, final String fieldName)
	{
		return new Field.FinalField(typeName, fieldName, Code.DEFAULT, null);
	}
	
	public static final Field Field(final String typeName, final String fieldName)
	{
		return new Field.Implementation(typeName, fieldName, Code.DEFAULT, FieldType.MUTABLE_WITH_SETTER, null);
	}
	
	public static final Field Field(final String typeName, final String fieldName, final FieldType type)
	{
		return new Field.Implementation(typeName, fieldName, Code.DEFAULT, type, null);
	}
	
	public static final Field Field(final String typeName, final String fieldName, final FieldType type, final String initializer)
	{
		return new Field.Implementation(typeName, fieldName, Code.DEFAULT, type, initializer);
	}
	
	
	public static String generateEntity(final String typeName, final Field... members)
	{
		return generateEntity(typeName, null, members);
	}
	
	public static String generateEntity(final String typeName, final String superclass, final Field... members)
	{
		final CodeGeneratorEntity generator = new CodeGeneratorEntity(typeName, members);
		return generator.generateCode();
	}
	
	public static String generateEntity(
		final String   typeName           ,
		final String   superclass         ,
		final String   getterPrefixBoolean,
		final String   getterPrefixNormal ,
		final String   setterPrefix       ,
		final Field... members
	)
	{
		final CodeGeneratorEntity generator = new CodeGeneratorEntity(typeName, superclass, getterPrefixBoolean, getterPrefixNormal, setterPrefix, members);
		return generator.generateCode();
	}


	// transient to skip fields in persistence layers collecting constants
	public static final transient String CODE_CONVENTION_GETTER_PREFIX   = "get";
	public static final transient String CODE_CONVENTION_ISGETTER_PREFIX = "is" ;
	public static final transient String CODE_CONVENTION_SETTER_PREFIX   = "set";

	/**
	 * Derive getter name from field.
	 *
	 * @param field the field
	 * @return the string
	 */
	public static String deriveGetterNameFromField(final java.lang.reflect.Field field)
	{
		return deriveGetterNameFromField(field, true);
	}

	/**
	 * Derive getter name from field.
	 *
	 * @param field the field
	 * @param usePrefix_is_forBoolean the use prefix_is_for boolean
	 * @return the string
	 */
	public static String deriveGetterNameFromField(
		final java.lang.reflect.Field field                  ,
		final boolean                 usePrefix_is_forBoolean
	)
	{
		return Code.deriveGetterNameFromFieldName(
			field.getName(),
			usePrefix_is_forBoolean && JadothTypes.isBoolean(field.getType())
		);
	}

	/**
	 * Derive setter name from field.
	 *
	 * @param field the field
	 * @return the string
	 */
	public static String deriveSetterNameFromField(final java.lang.reflect.Field field)
	{
		return Code.deriveSetterNameFromFieldName(field.getName());
	}

	/**
	 * Derive getter name from field name.
	 *
	 * @param fieldName the field name
	 * @param usePrefix_is the use prefix_is
	 * @return the string
	 */
	public static String deriveGetterNameFromFieldName(final String fieldName, final boolean usePrefix_is)
	{
		return JadothChars.createMedialCapitalsString(
			usePrefix_is
			? CODE_CONVENTION_ISGETTER_PREFIX
			: CODE_CONVENTION_GETTER_PREFIX,
			fieldName
		);
	}

	/**
	 * Derive setter name from field name.
	 *
	 * @param fieldName the field name
	 * @return the string
	 */
	public static String deriveSetterNameFromFieldName(final String fieldName)
	{
		return JadothChars.createMedialCapitalsString(CODE_CONVENTION_SETTER_PREFIX, fieldName);
	}
	
	/**
	 * Gets the full class name.
	 *
	 * @param c the c
	 * @return the full class name
	 */
	public static final String getFullClassName(final Class<?> c)
	{
		return getFullClassName(c, null);
	}

	/**
	 * Returns the full class name without packages of class <code>c</code>.
	 * <p>
	 * Examples:<br>
	 * full class name of String.class: "String"
	 * full class name of HashMap.Entry.class: "HashMap.Entry"
	 * <p>
	 * This is useful if the enclosing class has already been imported,
	 * so that the inner class does not have to be referenced full qualified.
	 * <p>
	 * If <code>enclosingClassParameters</code> is not null, then generic bounds parameter
	 * will be applied to enclosing classes. If <code>enclosingClassParameters</code> does not
	 * contain a bounds parameter string for a parametrized class, "?" will be used as a bounds parameter.
	 * <p>
	 * If <code>enclosingClassParameters</code> is null, no generics bounds parameter will be applied
	 *
	 * @param c the c
	 * @param enclosingClassParameters the enclosing class parameters
	 * @return the full class name
	 * @return
	 */
	public static final String getFullClassName(final Class<?> c, final Map<Class<?>, String> enclosingClassParameters)
	{
		Class<?> currentClass = c;
		final List<Class<?>> enclosingClasses = new ArrayList<>();
		final StringBuilder sb = new StringBuilder(256);

		while((currentClass = currentClass.getEnclosingClass()) != null)
		{
			enclosingClasses.add(currentClass);
		}

		int i = enclosingClasses.size();
		while(i-- > 0)
		{
			currentClass = enclosingClasses.get(i);
			sb.append(currentClass.getSimpleName());
			if(enclosingClassParameters != null && currentClass.getTypeParameters().length > 0)
			{
				final String param =  enclosingClassParameters.get(currentClass);
				sb.append('<').append(param == null ? '?' : param).append('>');
			}
			sb.append('.');
		}
		sb.append(c.getSimpleName());
		return sb.toString();
	}
	


	// CHECKSTYLE.ON: ConstantName
}
