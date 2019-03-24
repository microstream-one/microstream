package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_short extends AbstractBinaryHandlerNativeArrayPrimitive<short[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerNativeArray_short()
	{
		super(short[].class, defineElementsType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final short[] array, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_short(this.typeId(), oid, array);
	}

	@Override
	public short[] create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.createArray_short();
	}

	@Override
	public void update(final Binary bytes, final short[] instance, final PersistenceLoadHandler handler)
	{
		bytes.updateArray_short(instance);
	}
}
