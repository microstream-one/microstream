package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_byte extends AbstractBinaryHandlerNativeArrayPrimitive<byte[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_byte New()
	{
		return new BinaryHandlerNativeArray_byte();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_byte()
	{
		super(byte[].class, defineElementsType(byte.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final byte[] array, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.store_bytes(this.typeId(), objectId, array);
	}

	@Override
	public byte[] create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return bytes.create_bytes();
	}

	@Override
	public void update(final Binary bytes, final byte[] instance, final PersistenceObjectIdResolver idResolver)
	{
		bytes.update_bytes(instance);
	}

}
