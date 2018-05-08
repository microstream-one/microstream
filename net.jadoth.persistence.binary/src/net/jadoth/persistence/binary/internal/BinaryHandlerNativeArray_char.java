package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;

public final class BinaryHandlerNativeArray_char extends AbstractBinaryHandlerNativeArrayPrimitive<char[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_char()
	{
		super(char[].class, defineElementsType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final char[] array, final long oid, final PersistenceStoreFunction linker)
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

}
