package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleHandler;
import net.jadoth.swizzling.types.SwizzleBuildLinker;

public final class BinaryHandlerNativeArray_short extends AbstractBinaryHandlerNativeArrayPrimitive<short[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_short()
	{
		super(short[].class, defineElementsType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final short[] array, final long oid, final SwizzleHandler handler)
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
}
