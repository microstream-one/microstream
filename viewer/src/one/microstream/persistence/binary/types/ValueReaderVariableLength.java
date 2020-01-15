package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public abstract class ValueReaderVariableLength implements ValueReader
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	protected final PersistenceTypeDefinitionMember typeDefinition;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderVariableLength(final PersistenceTypeDefinitionMember typeDefinition)
	{
		super();
		this.typeDefinition = typeDefinition;
	}

	@Override
	public long getBinarySize(final Binary binary, final long offset)
	{
		return binary.getBinaryListTotalByteLength(offset);
	}

	public long getVariableLength(final Binary binary, final long offset)
	{
		return binary.getBinaryListElementCountUnvalidating(offset);
	}

}
