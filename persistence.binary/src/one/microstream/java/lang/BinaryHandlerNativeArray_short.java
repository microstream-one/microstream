package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
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
	public void store(final Binary bytes, final short[] array, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.store_shorts(this.typeId(), objectId, array);
	}

	@Override
	public short[] create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return bytes.create_shorts();
	}

	@Override
	public void update(final Binary bytes, final short[] instance, final PersistenceObjectIdResolver idResolver)
	{
		bytes.update_shorts(instance);
	}
}
