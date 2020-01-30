package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;


public abstract class AbstractBinaryHandlerCustomCollection<T>
extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomCollection(
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> customFields
	)
	{
		super(type, customFields);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}
	
	@Override
	public boolean hasPersistedVariableLength()
	{
		return true;
	}
	
	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}

}
