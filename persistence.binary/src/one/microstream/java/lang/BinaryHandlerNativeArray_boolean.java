package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_boolean extends AbstractBinaryHandlerNativeArrayPrimitive<boolean[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerNativeArray_boolean()
	{
		super(boolean[].class, defineElementsType(boolean.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final boolean[]               array   ,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeArray_boolean(this.typeId(), objectId, array);
	}

	@Override
	public final boolean[] create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.createArray_boolean();
	}

	@Override
	public final void update(final Binary bytes, final boolean[] instance, final PersistenceLoadHandler handler)
	{
		bytes.updateArray_boolean(instance);
	}

}
