package net.jadoth.util.code;

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
		super(typeName, superclass, null, null, null, members);
	}
	
	public CodeGeneratorEntity(
		final String   typeName           ,
		final String   superclass         ,
		final String   getterPrefixBoolean,
		final String   getterPrefixNormal ,
		final String   setterPrefix       ,
		final Field... members
	)
	{
		super(typeName, superclass, getterPrefixBoolean, getterPrefixNormal, setterPrefix, members);
	}

}
