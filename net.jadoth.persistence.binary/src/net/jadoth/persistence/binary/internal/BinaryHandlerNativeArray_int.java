package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;

public final class BinaryHandlerNativeArray_int extends AbstractBinaryHandlerNativeArrayPrimitive<int[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_int()
	{
		super(int[].class, defineElementsType(int.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final int[] array, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeArray_int(bytes, this.typeId(), oid, array);
	}

	@Override
	public int[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_int(bytes);
	}

	@Override
	public void update(final Binary bytes, final int[] instance, final SwizzleBuildLinker builder)
	{
		BinaryPersistence.updateArray_int(instance, bytes);
	}

}
