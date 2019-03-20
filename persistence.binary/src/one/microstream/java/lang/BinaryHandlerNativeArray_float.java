package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_float extends AbstractBinaryHandlerNativeArrayPrimitive<float[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_float()
	{
		super(float[].class, defineElementsType(float.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final float[] array, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_float(this.typeId(), oid, array);
	}

	@Override
	public float[] create(final Binary bytes)
	{
		return bytes.createArray_float();
	}

	@Override
	public void update(final Binary bytes, final float[] instance, final PersistenceLoadHandler handler)
	{
		bytes.updateArray_float(instance);
	}

}
