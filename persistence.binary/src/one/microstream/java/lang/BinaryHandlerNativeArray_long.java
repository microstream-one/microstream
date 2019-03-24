package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_long extends AbstractBinaryHandlerNativeArrayPrimitive<long[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerNativeArray_long()
	{
		super(long[].class, defineElementsType(long.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final long[] array, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_long(this.typeId(), oid, array);
	}

	@Override
	public long[] create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.createArray_long();
	}

	@Override
	public void update(final Binary bytes, final long[] instance, final PersistenceLoadHandler handler)
	{
		bytes.updateArray_long(instance);
	}

}
