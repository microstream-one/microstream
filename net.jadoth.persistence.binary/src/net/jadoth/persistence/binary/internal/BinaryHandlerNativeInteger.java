package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeInteger extends AbstractBinaryHandlerNativeCustom<Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeInteger(final long tid)
	{
		super(tid, Integer.class, defineValueType(int.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

//	@Override
//	public long getFixedBinaryContentLength()
//	{
//		return 4L;
//	}

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return false;
	}

	@Override
	public void store(final Binary bytes, final Integer instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeInteger(bytes, this.typeId(), oid, instance.intValue());
	}

	@Override
	public Integer create(final Binary bytes)
	{
		return BinaryPersistence.buildInteger(bytes);
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public boolean hasVariableBinaryLengthInstances()
	{
		return false;
	}

}
