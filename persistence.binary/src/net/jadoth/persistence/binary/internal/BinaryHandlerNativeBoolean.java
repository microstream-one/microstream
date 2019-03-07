package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeBoolean extends AbstractBinaryHandlerNativeCustomValueFixedLength<Boolean>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeBoolean()
	{
		super(Boolean.class, defineValueType(boolean.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final Boolean instance, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeBoolean(this.typeId(), oid, instance.booleanValue());
	}

	@Override
	public Boolean create(final Binary bytes)
	{
		return bytes.buildBoolean();
	}

}
