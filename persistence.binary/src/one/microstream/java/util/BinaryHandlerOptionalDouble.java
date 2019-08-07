package one.microstream.java.util;

import java.util.OptionalDouble;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerOptionalDouble extends AbstractBinaryHandlerCustomValueFixedLength<OptionalDouble>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long
		BINARY_OFFSET_IS_PRESENT =                                       0,
		BINARY_OFFSET_VALUE      = BINARY_OFFSET_IS_PRESENT + Byte  .BYTES, // Boolean.BYTES does not exist
		BINARY_LENGTH            = BINARY_OFFSET_VALUE      + Double.BYTES
	;

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerOptionalDouble New()
	{
		return new BinaryHandlerOptionalDouble();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerOptionalDouble()
	{
		/*
		 * Note on fields:
		 * These are not tied to JDK-specific internals. It's merely a sufficient state
		 * (even properly queryable in the implementation, fancy that) to represent an instance.
		 * The identical naming is "by coincidence" and will continue to work even if they change theirs internally.
		 */
		super(
			OptionalDouble.class,
			CustomFields(
				CustomField(boolean.class, "isPresent"),
				CustomField(double .class, "value"    )
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                  bytes   ,
		final OptionalDouble          instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		final long contentAddress = bytes.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		bytes.store_boolean(
			contentAddress + BINARY_OFFSET_IS_PRESENT,
			instance.isPresent()
		);

		bytes.store_double(
			contentAddress + BINARY_OFFSET_VALUE,
			instance.orElse(0.0)
		);
	}

	@Override
	public OptionalDouble create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		final boolean isPresent = bytes.get_boolean(BINARY_OFFSET_IS_PRESENT);
		
		// luckily, an uninitialized instance (all-zeroes, meaning isPresent == false) is all that is required.
		return isPresent
			? OptionalDouble.of(
				bytes.get_double(BINARY_OFFSET_VALUE)
			)
			: XMemory.instantiate(OptionalDouble.class)
		;
	}
	
}
