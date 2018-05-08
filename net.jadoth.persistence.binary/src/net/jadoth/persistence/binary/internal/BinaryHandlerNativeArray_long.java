package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;

public final class BinaryHandlerNativeArray_long extends AbstractBinaryHandlerNativeArrayPrimitive<long[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_long()
	{
		super(long[].class, defineElementsType(long.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final long[] array, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeArray_long(bytes, this.typeId(), oid, array);
	}

	@Override
	public long[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_long(bytes);
	}

	@Override
	public void update(final Binary bytes, final long[] instance, final SwizzleBuildLinker builder)
	{
		BinaryPersistence.updateArray_long(instance, bytes);
	}

}
