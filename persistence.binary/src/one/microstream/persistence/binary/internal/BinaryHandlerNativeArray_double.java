package one.microstream.persistence.binary.internal;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_double extends AbstractBinaryHandlerNativeArrayPrimitive<double[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_double()
	{
		super(double[].class, defineElementsType(double.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final double[] array, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_double(this.typeId(), oid, array);
	}

	@Override
	public double[] create(final Binary bytes)
	{
		return bytes.createArray_double();
	}

	@Override
	public void update(final Binary bytes, final double[] instance, final PersistenceLoadHandler builder)
	{
		bytes.updateArray_double(instance);
	}

}
