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

	// (07.05.2019 TM)XXX: priv#88 work-in-progress test code. Complete feature and remove.
//	private final BinaryField
//		prim1   = Field(int.class),
//		string1 = Field(String.class),
//		string2 = Field(String.class),
////		cmplx   = FieldBytes(),
//		cmplx   = FieldComplex(
//			Field(String.class, "key"),
//			Field(String.class, "value")
//		)
//	;
	
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

		// (07.05.2019 TM)XXX: priv#88 work-in-progress test code. Complete feature and remove.
//		this.initializeBinaryFields();
//		System.out.println(this.prim1.name() + "   = " + this.prim1.offset());
//		System.out.println(this.string1.name() + " = " + this.string1.offset());
//		System.out.println(this.string2.name() + " = " + this.string2.offset());
//		System.out.println(this.cmplx.name() + "   = " + this.cmplx.offset());
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
