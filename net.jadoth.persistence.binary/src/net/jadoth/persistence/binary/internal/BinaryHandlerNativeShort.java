package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeShort extends AbstractBinaryHandlerNativeCustomValueFixedLength<Short>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeShort()
	{
		super(Short.class, defineValueType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Short instance, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeShort(this.typeId(), oid, instance.shortValue());
	}

	@Override
	public Short create(final Binary bytes)
	{
		return bytes.buildShort();
	}

}
