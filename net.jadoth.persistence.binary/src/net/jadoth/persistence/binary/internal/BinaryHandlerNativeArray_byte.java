package net.jadoth.persistence.binary.internal;

import java.util.Arrays;

import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeArray_byte extends AbstractBinaryHandlerNativeArrayPrimitive<byte[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_byte()
	{
		super(byte[].class, defineElementsType(byte.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final byte[] array, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeArray_byte(bytes, this.typeId(), oid, array);
	}

	@Override
	public byte[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_byte(bytes);
	}

	@Override
	public void update(final Binary bytes, final byte[] instance, final SwizzleBuildLinker builder)
	{
		BinaryPersistence.updateArray_byte(instance, bytes);
	}

	@Override
	public boolean isEqual(
		final byte[]                   source                    ,
		final byte[]                   target                    ,
		final ObjectStateHandlerLookup instanceStateHandlerLookup
	)
	{
		return Arrays.equals(source, target);
	}

}
