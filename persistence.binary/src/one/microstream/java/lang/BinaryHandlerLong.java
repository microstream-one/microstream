package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerLong extends AbstractBinaryHandlerCustomValueFixedLength<Long>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerLong()
	{
		super(Long.class, defineValueType(long.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Long instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeLong(this.typeId(), objectId, instance.longValue());
	}

	@Override
	public Long create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.buildLong();
	}

}
