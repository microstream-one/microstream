package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import net.jadoth.persistence.types.PersistenceTypeHandlerCustom;


public abstract class AbstractBinaryHandlerNativeCustom<T>
extends AbstractBinaryHandlerNative<T>
implements PersistenceTypeHandlerCustom<Binary, T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeCustom(
		final long                                                                    typeId      ,
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> pseudoFields
	)
	{
		super(typeId, type, pseudoFields);
	}
	
	public AbstractBinaryHandlerNativeCustom(
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> pseudoFields
	)
	{
		super(type, pseudoFields);
	}

}
