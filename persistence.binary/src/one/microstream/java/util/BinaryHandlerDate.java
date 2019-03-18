package one.microstream.java.util;

import java.util.Date;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerDate extends AbstractBinaryHandlerCustomValueFixedLength<Date>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final long LENGTH_TIMESTAMP = Long.BYTES;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerDate()
	{
		super(
			Date.class,
			pseudoFields(
				pseudoField(long.class, "timestamp")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Date instance, final long oid, final PersistenceStoreHandler handler)
	{
		// the data content of a date is simple the timestamp long, nothing else
		bytes.store_long(
			bytes.storeEntityHeader(LENGTH_TIMESTAMP, this.typeId(), oid),
			instance.getTime()
		);
	}

	@Override
	public Date create(final Binary bytes)
	{
		return new Date(bytes.get_long(0));
	}

	@Override
	public void update(final Binary bytes, final Date instance, final PersistenceLoadHandler builder)
	{
		instance.setTime(bytes.get_long(0));
	}

}
