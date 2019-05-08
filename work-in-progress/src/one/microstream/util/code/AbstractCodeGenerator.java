package one.microstream.util.code;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XEnum;


/**
 * Quickly hacked together tooling class to ease generation of lengthy foundation types
 *
 * @author Thomas Muenz
 */
public abstract class AbstractCodeGenerator<F extends Field>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Type          type      ;
	final String        superclass; // (15.07.2013 TM)TODO: incorporate superclass into type?
	final BulkList<F>   members   ;
	final VarString     vs             = VarString.New();
	final XEnum<String> staticImports  = EqHashEnum.New();
	boolean             sectionHeaders = true;
	final String        getterPrefixBoolean;
	final String        getterPrefixNormal ;
	final String        setterPrefix       ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SafeVarargs
	public AbstractCodeGenerator(
		final String typeName           ,
		final String superclass         ,
		final String getterPrefixBoolean,
		final String getterPrefixNormal ,
		final String setterPrefix       ,
		final F...   members
	)
	{
		super();
		this.superclass          = superclass           ;
		this.type                = new Type.Default(notNull(typeName), "Default");
		this.members             = BulkList.New(members);
		this.getterPrefixBoolean = getterPrefixBoolean  ;
		this.getterPrefixNormal  = getterPrefixNormal   ;
		this.setterPrefix        = setterPrefix         ;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public AbstractCodeGenerator<F> setSectionHeaders(final boolean enable)
	{
		this.sectionHeaders = enable;
		return this;
	}

	void generateInterfaceMethods()
	{
		this.members.iterate(e ->
			e.assembleInterfaceGetter(this.vs, AbstractCodeGenerator.this.type, this.getterPrefixBoolean, this.getterPrefixNormal)
		);

		this.vs.lf().lf().lf();

		this.members.iterate(e ->
			e.assembleInterfaceSetter(this.vs, AbstractCodeGenerator.this.type, this.setterPrefix)
		);
	}

	void generateImplementationMemberMethods()
	{
		this.vs.lf(3);
		if(this.sectionHeaders)
		{
			Code.sectionHeader(this.vs, 2, "methods");
		}
		this.generateImplementationMemberGetters();
		this.vs.lf(2);
		this.generateImplementationMemberSetters();
	}

	final void generateInterfaceMemberProperty(final Field member)
	{
		this.vs.lf()
		.tab().add("public ").add(member.typeName()).blank().add(member.fieldName()).add("()").add(';').lf()
		;
	}


	void generateImplementationHeader()
	{
		final String superClass = this.superClass();
		this.vs.lf()
		.lf()
		.tab().add("public final class ");
		this.type.assembleImplementationClassName(this.vs);
		if(superClass != null)
		{
			this.vs.add(" extends ").add(superClass);
		}
		this.vs.add(" implements ");
		this.type.assembleTypeName(this.vs).lf()
		.tab().add('{')
		;
	}


	String superClass()
	{
		return this.superclass;
	}


	void generateImplementation()
	{
		this.generateImplementationHeader();
		this.generateImplementationMemberFields();
		this.generateImplementationConstructor();
		this.generateImplementationMemberMethods();
		this.vs.lf().lf().tab().add('}');
	}


	int calculateFieldTypesLength()
	{
		return this.members.iterate(new Consumer<Field>()
		{
			int typeLength;

			@Override
			public final void accept(final Field element)
			{
				if(element.typeName().length() > this.typeLength)
				{
					this.typeLength = element.typeName().length();
				}
			}
		}
		).typeLength;
	}

	int calculateFieldNamesLength()
	{
		return this.members.iterate(new Consumer<Field>()
			{
				int nameLength;

				@Override
				public final void accept(final Field element)
				{
					if(element.fieldName().length() > this.nameLength)
					{
						this.nameLength = element.fieldName().length();
					}
				}
			}
		).nameLength;
	}

	void generateImplementationConstructor()
	{
		this.vs.lf(3);
		if(this.sectionHeaders)
		{
			Code.sectionHeader(this.vs, 2, "constructors");
			this.vs.lf(); // empty line
		}
		this.vs.lf().tab(2).add("public ");
		this.type.assembleImplementationClassName(this.vs).add("(");
		this.generateImplementationFieldConstructorParameters();
		this.vs.deleteLast()
		.lf().tab(2).add(')')
		.lf().tab(2).add('{')
		.lf().tab(3).add("super()").add(';');
		this.generateImplementationFieldConstructorAssignments();
		this.vs.lf().tab(2).add('}')
		;
	}

	void generateImplementationFieldConstructorParameters(final F field, final int typeLength, final int nameLength)
	{
		field.assembleConstructorParameter(this.vs, typeLength, nameLength);
	}

	void generateImplementationFieldConstructorAssignment(final F field, final int nameLength)
	{
		field.assembleConstructorInitialization(this.vs, this.type, nameLength);
	}

	void generateImplementationMemberField(final F field, final int typeLength, final int nameLength)
	{
		field.assembleClassField(this.vs, this.type, typeLength, nameLength);
	}

	void generateImplementationMemberGetter(final F field)
	{
		field.assembleClassGetter(this.vs, this.type, this.getterPrefixBoolean, this.getterPrefixNormal);
	}

	void generateImplementationMemberSetter(final F field)
	{
		field.assembleClassSetter(this.vs, this.type, this.setterPrefix);
	}

	void generateImplementationMemberFields()
	{
		if(this.sectionHeaders)
		{
			Code.sectionHeader(this.vs, 2, "instance fields");
			this.vs.lf(); // empty line
		}

		final int typeLength = this.calculateFieldTypesLength();
		final int nameLength = this.calculateFieldNamesLength();

		this.members.iterate(e ->
			this.generateImplementationMemberField(e, typeLength, nameLength)
		);
	}

	void generateImplementationFieldConstructorParameters()
	{
		final int typeLength = this.calculateFieldTypesLength();
		final int nameLength = this.calculateFieldNamesLength();

		this.members.iterate(e ->
			this.generateImplementationFieldConstructorParameters(e, typeLength, nameLength)
		);
	}

	void generateImplementationFieldConstructorAssignments()
	{
		final int nameLength = this.calculateFieldNamesLength();

		this.members.iterate(e ->
			this.generateImplementationFieldConstructorAssignment(e, nameLength)
		);
	}

	void generateImplementationMemberGetters()
	{
		this.members.iterate(e ->
			this.generateImplementationMemberGetter(e)
		);
	}

	void generateImplementationMemberSetters()
	{
		this.members.iterate(e ->
			this.generateImplementationMemberSetter(e)
		);
	}

	public String generateCode()
	{
		this.vs.reset();
		this.vs.add("public interface ");
		this.type.assembleTypeName(this.vs)
		.lf().add('{');
		this.generateInterfaceMethods();
		this.vs.lf();
		this.generateImplementation();
		return this.vs.lf().lf().add('}').toString();
	}

}
