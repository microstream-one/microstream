package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;


public abstract class AbstractBinaryHandlerNativeCustomValue<T>
extends AbstractBinaryHandlerNativeCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeCustomValue(
		final long                                                         typeId,
		final Class<T>                                                     type  ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> fields
	)
	{
		super(typeId, type, fields);
	}
	
	protected AbstractBinaryHandlerNativeCustomValue(
		final Class<T>                                                     type  ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> fields
	)
	{
		super(type, fields);
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
