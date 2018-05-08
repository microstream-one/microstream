package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;

public final class BinaryHandlerNativeArray_boolean extends AbstractBinaryHandlerNativeArrayPrimitive<boolean[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_boolean()
	{
		super(boolean[].class, defineElementsType(boolean.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(final Binary bytes, final boolean[] array, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeArray_boolean(bytes, this.typeId(), oid, array);
	}

	@Override
	public final boolean[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_boolean(bytes);
	}

	@Override
	public final void update(final Binary bytes, final boolean[] instance, final SwizzleBuildLinker builder)
	{
		BinaryPersistence.updateArray_boolean(instance, bytes);
	}

}
