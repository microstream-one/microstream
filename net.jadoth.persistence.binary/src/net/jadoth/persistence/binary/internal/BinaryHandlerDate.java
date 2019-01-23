package net.jadoth.persistence.binary.internal;

import java.util.Date;

import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerDate extends AbstractBinaryHandlerNativeCustomValueFixedLength<Date>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final int  BITS_3           = 3                   ;
	private static final long LENGTH_TIMESTAMP = Long.SIZE >>> BITS_3;



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
		XMemory.set_long(
			bytes.storeEntityHeader(LENGTH_TIMESTAMP, this.typeId(), oid),
			instance.getTime()
		);
	}

	@Override
	public Date create(final Binary bytes)
	{
		return new Date(XMemory.get_long(bytes.entityContentAddress()));
	}

	@Override
	public void update(final Binary bytes, final Date instance, final PersistenceLoadHandler builder)
	{
		instance.setTime(XMemory.get_long(bytes.entityContentAddress()));
	}

}
