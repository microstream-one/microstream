package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
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
	public void store(
		final Binary                          data    ,
		final double[]                        array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_doubles(this.typeId(), objectId, array);
	}

	@Override
	public double[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_doubles();
	}

	@Override
	public void updateState(final Binary data, final double[] instance, final PersistenceLoadHandler handler)
	{
		data.update_doubles(instance);
	}

}
