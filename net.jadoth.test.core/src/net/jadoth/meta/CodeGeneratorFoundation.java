package net.jadoth.meta;

import net.jadoth.util.chars.VarString;


/**
 * Quickly hacked together tooling class to ease generation of lengthy foundation types
 *
 * @author Thomas Muenz
 */
public class CodeGeneratorFoundation extends AbstractCodeGenerator<CodeGeneratorFoundation.Member>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	public static final Member Member(final String typeName)
	{
		return Member(typeName, Code.toLowerCaseFirstLetter(typeName));
	}

	public static final Member Member(final String typeName, final String fieldName)
	{
		return new Member(typeName, fieldName);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public CodeGeneratorFoundation(final String typeName, final String superclass, final Member... members)
	{
		super(typeName, superclass, members);
	}

	public CodeGeneratorFoundation(final String typeName, final Member... members)
	{
		this(typeName, "InstanceDispatcher", members);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	@Override
	void generateInterfaceMethods()
	{
		this.generateInterfaceMemberGetters();
		this.vs.lf().lf();
		this.generateInterfaceMemberSetters();
	}

	void generateInterfaceMemberGetters()
	{
		this.members.iterate(e ->
			e.assembleInterfaceGetter(this.vs, this.type)
		);
	}

	void generateInterfaceMemberSetters()
	{
		this.members.iterate(e ->
				e.assembleInterfaceSetter(this.vs, this.type)
		);
	}

	@Override
	void generateImplementationMemberMethods()
	{
		this.vs.lf(3);
		if(this.sectionHeaders)
		{
			Code.sectionHeader(this.vs, 2, "declared methods");
		}
		this.generateImplementationMemberInternalSetters();
		this.vs.lf(2);
		this.generateImplementationMemberInternalCreates();

		super.generateImplementationMemberMethods();
	}

	private void generateImplementationMemberInternalSetters()
	{
		this.members.iterate(e ->
			this.generateImplementationMemberInternalSetter(e)
		);
	}

	private void generateImplementationMemberInternalCreates()
	{
		this.members.iterate(e ->
			this.generateImplementationMemberInternalCreate(e)
		);
	}

	final void generateImplementationMemberInternalSetter(final Field member)
	{
		final String fieldName      = member.fieldName();
		final String upperFieldName = Code.toUpperCaseFirstLetter(member.fieldName());

		this.vs.lf() // empty line
		.lf().tab(2).add("protected final void internalSet").add(upperFieldName).add('(').add(member.typeName()).blank().add(fieldName).add(')')
		.lf().tab(2).add('{')
		.lf().tab(3).add("this.").add(fieldName).add(" = ").add(fieldName).add(';')
		.lf().tab(2).add('}')

		;
	}

	final void generateImplementationMemberInternalCreate(final Field member)
	{
		final String methodName = "create" + Code.toUpperCaseFirstLetter(member.fieldName()) + "()";

		this.vs.lf() // empty line
		.lf().tab(2).add("protected ").add(member.typeName()).blank().add(methodName)
		.lf().tab(2).add('{')
		.lf().tab(3).add("throw new MissingAssemblyPartException(").add(member.typeName()).add(".class);")
		.lf().tab(2).add('}')
		;
	}

	static final class Member extends Field.AbstractImplementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String upperFieldName;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Member(final String typeName, final String fieldName)
		{
			super(typeName, fieldName);
			this.upperFieldName = Code.toUpperCaseFirstLetter(fieldName);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public void assembleInterfaceGetter(final VarString vs, final Type type)
		{
			vs.lf()
			.tab().add("public ").add(this.typeName()).blank().add("get")
			.add(Code.toUpperCaseFirstLetter(this.fieldName())).add("()").add(';').lf()
			;
		}

		@Override
		public void assembleInterfaceSetter(final VarString vs, final Type type)
		{
			final String upperName = Code.toUpperCaseFirstLetter(this.fieldName());
			vs.lf()
			.tab().add("public ");
			type.assembleTypeName(vs).blank()
			.add("set").add(upperName).add("(").add(this.typeName()).blank().add(this.fieldName()).add(")").add(';').lf()
			;
		}

		@Override
		public void assembleClassField(final VarString vs, final Type type, final int typeLength, final int nameLength)
		{
			vs.lf().tab().tab().add("private ")
			.padRight(this.typeName(), typeLength, ' ').blank()
			.padRight(this.fieldName(), nameLength, ' ')
			.add(";")
			;
		}

		@Override
		public void assembleConstructorParameter(final VarString vs, final int typeLength, final int nameLength)
		{
			// no-op for foundation member field
		}

		@Override
		public void assembleConstructorInitialization(final VarString vs, final Type type, final int nameLength)
		{
			// no-op for foundation member field
		}

		@Override
		public void assembleClassGetter(final VarString vs, final Type type)
		{
			vs.lf(); // empty line
			Code.appendOverride(vs, 2)
			.lf().tab(2).add("public ").add(this.typeName()).blank().add("get").add(this.upperFieldName).add("()")
			.lf().tab(2).add('{')
			.lf().tab(3).add("if(this.").add(this.fieldName()).add(" == null)")
			.lf().tab(3).add("{")
			.lf().tab(4).add("this.").add(this.fieldName()).add(" = this.dispatch(this.create").add(this.upperFieldName).add("());")
			.lf().tab(3).add('}')
			.lf().tab(3).add("return this.").add(this.fieldName()).add(';')
			.lf().tab(2).add('}')
			;
		}

		@Override
		public void assembleClassSetter(final VarString vs, final Type type)
		{
			vs.lf(); // empty line
			Code.appendOverride(vs, 2)
			.lf().tab(2).add("public ");
				type.assembleImplementationFullName(vs).blank()
				.add("set").add(this.upperFieldName).add('(').add(this.typeName()).blank().add(this.fieldName()).add(')')
			.lf().tab(2).add('{')
			.lf().tab(3).add("this.internalSet").add(this.upperFieldName).add('(').add(this.fieldName()).add(')').add(';')
			.lf().tab(3).add("return this;")
			.lf().tab(2).add('}')
			;
		}
	}



	// testing
	public static void main(final String[] args)
	{
		final CodeGeneratorFoundation generator = new CodeGeneratorFoundation(
			"StorageFoundation",
			Member("StorageEntityMarkMonitor.Creator", "entityMarkMonitorCreator")

//			Member("char"  , "segmentStarter"         ),
//			Member("char"  , "segmentTerminator"      ),

//			Member("Predicate<Class<?>>" , "typeEvaluatorTypeIdMappable"),
//			Member("Predicate<Class<?>>" , "typeEvaluatorPersistable")


		);

		System.out.println(generator.generateCode());
	}

}
