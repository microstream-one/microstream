package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_long extends AbstractBinaryHandlerNativeArrayPrimitive<long[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_long New()
	{
		return new BinaryHandlerNativeArray_long();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_long()
	{
		super(long[].class, defineElementsType(long.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                          data    ,
		final long[]                          array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_longs(this.typeId(), objectId, array);
	}

	@Override
	public long[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_longs();
	}

	@Override
	public void updateState(final Binary data, final long[] instance, final PersistenceLoadHandler handler)
	{
		data.update_longs(instance);
	}

}
