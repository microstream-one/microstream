package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerByte extends AbstractBinaryHandlerCustomValueFixedLength<Byte>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerByte New()
	{
		return new BinaryHandlerByte();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerByte()
	{
		super(Byte.class, defineValueType(byte.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Byte instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeByte(this.typeId(), objectId, instance.byteValue());
	}

	@Override
	public Byte create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.buildByte();
	}

}
