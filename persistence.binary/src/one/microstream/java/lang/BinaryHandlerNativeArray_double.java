package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_double extends AbstractBinaryHandlerNativeArrayPrimitive<double[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_double New()
	{
		return new BinaryHandlerNativeArray_double();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_double()
	{
		super(double[].class, defineElementsType(double.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final double[] array, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_double(this.typeId(), objectId, array);
	}

	@Override
	public double[] create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return bytes.createArray_double();
	}

	@Override
	public void update(final Binary bytes, final double[] instance, final PersistenceObjectIdResolver idResolver)
	{
		bytes.updateArray_double(instance);
	}

}
