package net.jadoth.persistence.binary.internal;

import java.util.Arrays;

import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeArray_char extends AbstractBinaryHandlerNativeArrayPrimitive<char[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_char(final long typeId)
	{
		super(typeId, char[].class, defineElementsType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final char[] array, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeArray_char(bytes, this.typeId(), oid, array);
	}

	@Override
	public char[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_char(bytes);
	}

	@Override
	public void update(final Binary bytes, final char[] instance, final SwizzleBuildLinker builder)
	{
		BinaryPersistence.updateArray_char(instance, bytes);
	}

	@Override
	public boolean isEqual(
		final char[]                   source                    ,
		final char[]                   target                    ,
		final ObjectStateHandlerLookup instanceStateHandlerLookup
	)
	{
		return Arrays.equals(source, target);
	}

}
