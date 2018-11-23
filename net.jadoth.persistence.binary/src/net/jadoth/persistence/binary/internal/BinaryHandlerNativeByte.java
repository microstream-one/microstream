package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceHandler;

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
	public void store(final Binary bytes, final Byte instance, final long oid, final PersistenceHandler handler)
	{
		BinaryPersistence.storeByte(bytes, this.typeId(), oid, instance.byteValue());
	}

	@Override
	public Byte create(final Binary bytes)
	{
		return BinaryPersistence.buildByte(bytes);
	}

}
