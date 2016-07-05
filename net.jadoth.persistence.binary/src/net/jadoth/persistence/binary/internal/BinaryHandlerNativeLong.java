package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeLong extends AbstractBinaryHandlerNativeCustom<Long>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeLong(final long tid)
	{
		super(tid, Long.class, defineValueType(long.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

//	@Override
//	public long getFixedBinaryContentLength()
//	{
//		return 8L;
//	}

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return false;
	}

	@Override
	public void store(final Binary bytes, final Long instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeLong(bytes, this.typeId(), oid, instance.longValue());
	}

	@Override
	public Long create(final Binary bytes)
	{
		return BinaryPersistence.buildLong(bytes);
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
