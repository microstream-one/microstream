package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_short extends AbstractBinaryHandlerNativeArrayPrimitive<short[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_short New()
	{
		return new BinaryHandlerNativeArray_short();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_short()
	{
		super(short[].class, defineElementsType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                          data    ,
		final short[]                         array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_shorts(this.typeId(), objectId, array);
	}

	@Override
	public short[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_shorts();
	}

	@Override
	public void updateState(final Binary data, final short[] instance, final PersistenceLoadHandler handler)
	{
		data.update_shorts(instance);
	}
}
