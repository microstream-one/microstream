package one.microstream.persistence.binary.java.sql;

import java.sql.Timestamp;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import one.microstream.persistence.binary.java.util.BinaryHandlerDate;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

/**
 * Blunt copy of {@link BinaryHandlerDate} for the as good as superfluous type {@link java.sql.Timestamp}.
 * 
 * 
 */
public final class BinaryHandlerSqlTimestamp extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Timestamp>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerSqlTimestamp New()
	{
		return new BinaryHandlerSqlTimestamp();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerSqlTimestamp()
	{
		super(
			Timestamp.class,
			CustomFields(
				CustomField(long.class, "timestamp")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static long instanceState(final Timestamp instance)
	{
		return instance.getTime();
	}
	
	private static long binaryState(final Binary data)
	{
		return data.read_long(0);
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final Timestamp                       instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(Long.BYTES, this.typeId(), objectId);
		
		// the data content of a date is simple the timestamp long, nothing else
		data.store_long(instanceState(instance));
	}

	@Override
	public final Timestamp create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Timestamp(binaryState(data));
	}

	@Override
	public final void updateState(final Binary data, final Timestamp instance, final PersistenceLoadHandler handler)
	{
		instance.setTime(binaryState(data));
	}

}
