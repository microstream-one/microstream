package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.binary.types.BinaryValueAccessor;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMember;


public abstract class AbstractBinaryHandlerNativeCustomValueFixedLength<T>
extends AbstractBinaryHandlerNativeCustomValue<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeCustomValueFixedLength(
		final Class<T>                                                    type  ,
		final BinaryValueAccessor                            binaryValueAccessor,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> fields
	)
	{
		super(type, binaryValueAccessor, fields);
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
