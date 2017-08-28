package net.jadoth.meta.code;

import net.jadoth.meta.code.Field.FinalField;
import net.jadoth.util.chars.VarString;

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
	


	// CHECKSTYLE.ON: ConstantName
}
