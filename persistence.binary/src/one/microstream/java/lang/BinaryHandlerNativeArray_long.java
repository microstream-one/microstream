package one.microstream.java.lang;

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
	public void store(final Binary bytes, final long[] array, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.store_longs(this.typeId(), objectId, array);
	}

	@Override
	public long[] create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.create_longs();
	}

	@Override
	public void update(final Binary bytes, final long[] instance, final PersistenceLoadHandler handler)
	{
		bytes.update_longs(instance);
	}

}
