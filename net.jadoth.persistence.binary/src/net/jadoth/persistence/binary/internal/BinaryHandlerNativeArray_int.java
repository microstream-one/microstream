package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceBuildLinker;
import net.jadoth.persistence.types.PersistenceHandler;

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
	public void store(final Binary bytes, final int[] array, final long oid, final PersistenceHandler handler)
	{
		BinaryPersistence.storeArray_int(bytes, this.typeId(), oid, array);
	}

	@Override
	public int[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_int(bytes);
	}

	@Override
	public void update(final Binary bytes, final int[] instance, final PersistenceBuildLinker builder)
	{
		BinaryPersistence.updateArray_int(instance, bytes);
	}

}
