package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;


public abstract class AbstractBinaryHandlerCustomValueVariableLength<T>
extends AbstractBinaryHandlerCustomValue<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomValueVariableLength(
		final Class<T>                                                               type        ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> pseudoFields
	)
	{
		super(type, pseudoFields);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}
	
}
