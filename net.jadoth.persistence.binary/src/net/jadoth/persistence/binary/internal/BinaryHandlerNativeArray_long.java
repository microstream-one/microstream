package net.jadoth.persistence.binary.internal;

import java.util.Arrays;

import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeArray_long extends AbstractBinaryHandlerNativeArrayPrimitive<long[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_long(final long typeId)
	{
		super(typeId, long[].class, defineElementsType(long.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final long[] array, final long oid, final SwizzleStoreLinker linker)
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

	@Override
	public boolean isEqual(
		final long[]                   source                    ,
		final long[]                   target                    ,
		final ObjectStateHandlerLookup instanceStateHandlerLookup
	)
	{
		return Arrays.equals(source, target);
	}

}
