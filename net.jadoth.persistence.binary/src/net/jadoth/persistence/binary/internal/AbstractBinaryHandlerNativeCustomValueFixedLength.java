package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;


public abstract class AbstractBinaryHandlerNativeCustomValueFixedLength<T>
extends AbstractBinaryHandlerNativeCustomValue<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeCustomValueFixedLength(
		final long                                                         typeId,
		final Class<T>                                                     type  ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> fields
	)
	{
		super(typeId, type, fields);
	}
	
	protected AbstractBinaryHandlerNativeCustomValueFixedLength(
		final Class<T>                                                     type  ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> fields
	)
	{
		super(type, fields);
	}
	
	// damn git
	
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
