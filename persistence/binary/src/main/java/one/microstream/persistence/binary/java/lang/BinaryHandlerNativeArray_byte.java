package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
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
	public void store(
		final Binary                          data    ,
		final byte[]                          array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_bytes(this.typeId(), objectId, array);
	}

	@Override
	public byte[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_bytes();
	}

	@Override
	public void updateState(final Binary data, final byte[] instance, final PersistenceLoadHandler handler)
	{
		data.update_bytes(instance);
	}

}
