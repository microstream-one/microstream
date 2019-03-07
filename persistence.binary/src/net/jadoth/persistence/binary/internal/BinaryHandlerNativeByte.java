package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeByte extends AbstractBinaryHandlerNativeCustomValueFixedLength<Byte>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeByte()
	{
		super(Byte.class, defineValueType(byte.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Byte instance, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeByte(this.typeId(), oid, instance.byteValue());
	}

	@Override
	public Byte create(final Binary bytes)
	{
		return bytes.buildByte();
	}

}
