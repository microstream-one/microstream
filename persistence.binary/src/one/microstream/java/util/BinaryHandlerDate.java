package one.microstream.java.util;

import java.util.Date;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerDate extends AbstractBinaryHandlerCustomValueFixedLength<Date>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long LENGTH_TIMESTAMP = Long.BYTES;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerDate New()
	{
		return new BinaryHandlerDate();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerDate()
	{
		super(
			Date.class,
			CustomFields(
				CustomField(long.class, "timestamp")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static long instanceState(final Date instance)
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
		final Date                    instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		data.storeEntityHeader(LENGTH_TIMESTAMP, this.typeId(), objectId);
		
		// the data content of a date is simple the timestamp long, nothing else
		data.store_long(instanceState(instance));
	}

	@Override
	public final Date create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Date(binaryState(data));
	}
	
	@Override
	public final void initializeState(final Binary data, final Date instance, final PersistenceLoadHandler handler)
	{
		this.updateState(data, instance, handler);
	}

	@Override
	public final void updateState(final Binary data, final Date instance, final PersistenceLoadHandler handler)
	{
		instance.setTime(binaryState(data));
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Date                   instance,
		final PersistenceLoadHandler handler
	)
	{
		final long instanceState = instanceState(instance);
		final long binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
