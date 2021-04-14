package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
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
	public void store(
		final Binary                          data    ,
		final int[]                           array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_ints(this.typeId(), objectId, array);
	}

	@Override
	public int[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_ints();
	}

	@Override
	public void updateState(final Binary data, final int[] instance, final PersistenceLoadHandler handler)
	{
		data.update_ints(instance);
	}

}
