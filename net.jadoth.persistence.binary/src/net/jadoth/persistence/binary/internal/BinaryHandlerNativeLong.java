package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleHandler;

public final class BinaryHandlerNativeLong extends AbstractBinaryHandlerNativeCustomValueFixedLength<Long>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeLong()
	{
		super(Long.class, defineValueType(long.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Long instance, final long oid, final SwizzleHandler handler)
	{
		BinaryPersistence.storeLong(bytes, this.typeId(), oid, instance.longValue());
	}

	@Override
	public Long create(final Binary bytes)
	{
		return BinaryPersistence.buildLong(bytes);
	}

}
