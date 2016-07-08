package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;


public abstract class AbstractBinaryHandlerNativeCustomValueVariableLength<T>
extends AbstractBinaryHandlerNativeCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeCustomValueVariableLength(
		final long                                                                    typeId      ,
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> pseudoFields
	)
	{
		super(typeId, type, pseudoFields);
	}

	/**
	 * No instance references as it is a value type format.
	 */
	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

	/**
	 * Variable length.
	 */
	@Override
	public final boolean isVariableBinaryLengthType()
	{
		return true;
	}

	/**
	 * Variable length.
	 */
	@Override
	public final boolean hasVariableBinaryLengthInstances()
	{
		return true;
	}

}
