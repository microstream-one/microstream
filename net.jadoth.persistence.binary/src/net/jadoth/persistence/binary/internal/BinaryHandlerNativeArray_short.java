package net.jadoth.persistence.binary.internal;

import java.util.Arrays;

import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.PersistenceStoreFunction;

public final class BinaryHandlerNativeArray_short extends AbstractBinaryHandlerNativeArrayPrimitive<short[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_short(final long typeId)
	{
		super(typeId, short[].class, defineElementsType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final short[] array, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeArray_short(bytes, this.typeId(), oid, array);
	}

	@Override
	public short[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_short(bytes);
	}

	@Override
	public void update(final Binary bytes, final short[] instance, final SwizzleBuildLinker builder)
	{
		BinaryPersistence.updateArray_short(instance, bytes);
	}

	@Override
	public boolean isEqual(
		final short[]                  source                    ,
		final short[]                  target                    ,
		final ObjectStateHandlerLookup instanceStateHandlerLookup
	)
	{
		return Arrays.equals(source, target);
	}

}
