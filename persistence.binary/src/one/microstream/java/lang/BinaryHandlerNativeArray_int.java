package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_int extends AbstractBinaryHandlerNativeArrayPrimitive<int[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_int New()
	{
		return new BinaryHandlerNativeArray_int();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_int()
	{
		super(int[].class, defineElementsType(int.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final int[] array, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.store_ints(this.typeId(), objectId, array);
	}

	@Override
	public int[] create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return bytes.create_ints();
	}

	@Override
	public void update(final Binary bytes, final int[] instance, final PersistenceObjectIdResolver idResolver)
	{
		bytes.update_ints(instance);
	}

}
