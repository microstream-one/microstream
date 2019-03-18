package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeLong extends AbstractBinaryHandlerCustomValueFixedLength<Long>
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
	public void store(final Binary bytes, final Long instance, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeLong(this.typeId(), oid, instance.longValue());
	}

	@Override
	public Long create(final Binary bytes)
	{
		return bytes.buildLong();
	}

}
