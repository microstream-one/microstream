package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMember;
import net.jadoth.persistence.types.PersistenceTypeHandlerCustom;


public abstract class AbstractBinaryHandlerNativeCustom<T>
extends AbstractBinaryHandlerNative<T>
implements PersistenceTypeHandlerCustom<Binary, T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeCustom(
		final Class<T>                                                    type  ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> fields
	)
	{
		super(type, fields);
	}

}
