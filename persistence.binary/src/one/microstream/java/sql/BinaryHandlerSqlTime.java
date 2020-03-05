package one.microstream.java.sql;

import java.sql.Time;

import one.microstream.java.util.BinaryHandlerDate;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

/**
 * Blunt copy of {@link BinaryHandlerDate} for the as good as superfluous type {@link java.sql.Time}.
 * 
 * @author TM
 */
public final class BinaryHandlerSqlTime extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Time>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerSqlTime New()
	{
		return new BinaryHandlerSqlTime();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerSqlTime()
	{
		super(
			Time.class,
			CustomFields(
				CustomField(long.class, "timestamp")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static long instanceState(final Time instance)
	{
		return instance.getTime();
	}
	
	private static long binaryState(final Binary data)
	{
		return data.read_long(0);
	}

	@Override
	public final void store(
		final Binary                  data    ,
		final Time                    instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		data.storeEntityHeader(Long.BYTES, this.typeId(), objectId);
		
		// the data content of a date is simple the timestamp long, nothing else
		data.store_long(instanceState(instance));
	}

	@Override
	public final Time create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Time(binaryState(data));
	}

	@Override
	public final void updateState(final Binary data, final Time instance, final PersistenceLoadHandler handler)
	{
		instance.setTime(binaryState(data));
	}

}
