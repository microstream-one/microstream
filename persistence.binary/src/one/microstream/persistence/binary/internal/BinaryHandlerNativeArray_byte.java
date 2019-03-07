package one.microstream.persistence.binary.internal;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_byte extends AbstractBinaryHandlerNativeArrayPrimitive<byte[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_byte()
	{
		super(byte[].class, defineElementsType(byte.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final byte[] array, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_byte(this.typeId(), oid, array);
	}

	@Override
	public byte[] create(final Binary bytes)
	{
		return bytes.createArray_byte();
	}

	@Override
	public void update(final Binary bytes, final byte[] instance, final PersistenceLoadHandler builder)
	{
		bytes.updateArray_byte(instance);
	}

}
