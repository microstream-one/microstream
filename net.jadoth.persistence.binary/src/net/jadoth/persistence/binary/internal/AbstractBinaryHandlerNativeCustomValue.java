package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;


public abstract class AbstractBinaryHandlerNativeCustomValue<T>
extends AbstractBinaryHandlerNativeCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected AbstractBinaryHandlerNativeCustomValue(
		final long                                                                    typeId      ,
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> pseudoFields
	)
	{
		super(typeId, type, pseudoFields);
	}
	
	protected AbstractBinaryHandlerNativeCustomValue(
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
	public final boolean hasInstanceReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}

}
