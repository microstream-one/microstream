package net.jadoth.meta.code;

public class CodeGeneratorEntity extends AbstractCodeGenerator<Field>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public CodeGeneratorEntity(final String typeName, final Field... members)
	{
		this(typeName, null, members);
	}

	public CodeGeneratorEntity(final String typeName, final String superclass, final Field... members)
	{
		super(typeName, superclass, members);
	}

}
