package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_boolean extends AbstractBinaryHandlerNativeArrayPrimitive<boolean[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_boolean New()
	{
		return new BinaryHandlerNativeArray_boolean();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_boolean()
	{
		super(boolean[].class, defineElementsType(boolean.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final boolean[]                       array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_booleans(this.typeId(), objectId, array);
	}

	@Override
	public final boolean[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_booleans();
	}

	@Override
	public final void updateState(final Binary data, final boolean[] instance, final PersistenceLoadHandler handler)
	{
		data.update_booleans(instance);
	}

}
