package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;


public abstract class AbstractBinaryHandlerNativeCustomValueVariableLength<T>
extends AbstractBinaryHandlerNativeCustomValue<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////
	
	protected AbstractBinaryHandlerNativeCustomValueVariableLength(
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> pseudoFields
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
