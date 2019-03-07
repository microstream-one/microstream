package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_long extends AbstractBinaryHandlerNativeArrayPrimitive<long[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

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
	public long[] create(final Binary bytes)
	{
		return bytes.createArray_long();
	}

	@Override
	public void update(final Binary bytes, final long[] instance, final PersistenceLoadHandler builder)
	{
		bytes.updateArray_long(instance);
	}

}
