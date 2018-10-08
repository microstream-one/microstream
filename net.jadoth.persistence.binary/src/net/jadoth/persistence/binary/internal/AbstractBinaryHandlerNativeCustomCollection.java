package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoField;


public abstract class AbstractBinaryHandlerNativeCustomCollection<T>
extends AbstractBinaryHandlerNativeCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeCustomCollection(
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
	public final boolean hasInstanceReferences()
	{
		return true;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}
	
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
