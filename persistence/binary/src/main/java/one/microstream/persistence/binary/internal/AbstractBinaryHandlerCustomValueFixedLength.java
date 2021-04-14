package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;


public abstract class AbstractBinaryHandlerCustomValueFixedLength<T, S>
extends AbstractBinaryHandlerCustomValue<T, S>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomValueFixedLength(
		final Class<T>                                                    type  ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> fields
	)
	{
		super(type, fields);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}
	
}
