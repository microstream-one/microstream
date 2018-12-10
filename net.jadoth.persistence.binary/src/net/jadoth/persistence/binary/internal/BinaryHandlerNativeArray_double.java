package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;

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
		BinaryPersistence.storeArray_double(bytes, this.typeId(), oid, array);
	}

	@Override
	public double[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_double(bytes);
	}

	@Override
	public void update(final Binary bytes, final double[] instance, final PersistenceLoadHandler builder)
	{
		BinaryPersistence.updateArray_double(instance, bytes);
	}

}
