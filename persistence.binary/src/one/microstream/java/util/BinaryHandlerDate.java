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

	// (07.05.2019 TM)XXX: MS-130 work-in-progress test code. Complete feature and remove.
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
	// constructors //
	/////////////////

	public BinaryHandlerDate()
	{
		super(
			Date.class,
			pseudoFields(
				pseudoField(long.class, "timestamp")
			)
		);

		// (07.05.2019 TM)XXX: MS-130 work-in-progress test code. Complete feature and remove.
//		this.initializeBinaryFields();
//		System.out.println(this.prim1.name() + "   = " + this.prim1.offset());
//		System.out.println(this.string1.name() + " = " + this.string1.offset());
//		System.out.println(this.string2.name() + " = " + this.string2.offset());
//		System.out.println(this.cmplx.name() + "   = " + this.cmplx.offset());
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
	public Date create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new Date(bytes.get_long(0));
	}

	@Override
	public void update(final Binary bytes, final Date instance, final PersistenceLoadHandler handler)
	{
		instance.setTime(bytes.get_long(0));
	}

}
