package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerLong extends AbstractBinaryHandlerCustomValueFixedLength<Long>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerLong New()
	{
		return new BinaryHandlerLong();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLong()
	{
		super(Long.class, defineValueType(long.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static long instanceState(final Long instance)
	{
		return instance.longValue();
	}
	
	private static long binaryState(final Binary data)
	{
		return data.read_long(0);
	}

	@Override
	public void store(final Binary data, final Long instance, final long objectId, final PersistenceStoreHandler handler)
	{
		data.storeLong(this.typeId(), objectId, instance.longValue());
	}

	@Override
	public Long create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildLong();
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Long                   instance,
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
