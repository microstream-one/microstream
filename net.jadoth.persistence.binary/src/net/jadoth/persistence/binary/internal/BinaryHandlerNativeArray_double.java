package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;

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
	public void store(final Binary bytes, final double[] array, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeArray_double(bytes, this.typeId(), oid, array);
	}

	@Override
	public double[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_double(bytes);
	}

	@Override
	public void update(final Binary bytes, final double[] instance, final SwizzleBuildLinker builder)
	{
		BinaryPersistence.updateArray_double(instance, bytes);
	}

}
