package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceBuildLinker;
import net.jadoth.persistence.types.PersistenceHandler;

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
	public void store(final Binary bytes, final float[] array, final long oid, final PersistenceHandler handler)
	{
		BinaryPersistence.storeArray_float(bytes, this.typeId(), oid, array);
	}

	@Override
	public float[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_float(bytes);
	}

	@Override
	public void update(final Binary bytes, final float[] instance, final PersistenceBuildLinker builder)
	{
		BinaryPersistence.updateArray_float(instance, bytes);
	}

}
