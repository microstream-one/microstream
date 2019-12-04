package one.microstream.java.util;

import java.util.OptionalLong;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerOptionalLong extends AbstractBinaryHandlerCustomValueFixedLength<OptionalLong>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long
		BINARY_OFFSET_IS_PRESENT =                                     0,
		BINARY_OFFSET_VALUE      = BINARY_OFFSET_IS_PRESENT + Byte.BYTES, // Boolean.BYTES does not exist
		BINARY_LENGTH            = BINARY_OFFSET_VALUE      + Long.BYTES
	;

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerOptionalLong New()
	{
		return new BinaryHandlerOptionalLong();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerOptionalLong()
	{
		/*
		 * Note on fields:
		 * These are not tied to JDK-specific internals. It's merely a sufficient state
		 * (even properly queryable in the implementation, fancy that) to represent an instance.
		 * The identical naming is "by coincidence" and will continue to work even if they change theirs internally.
		 */
		super(
			OptionalLong.class,
			CustomFields(
				CustomField(boolean.class, "isPresent"),
				CustomField(long   .class, "value"    )
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                  bytes   ,
		final OptionalLong            instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		bytes.store_boolean(
			BINARY_OFFSET_IS_PRESENT,
			instance.isPresent()
		);

		bytes.store_long(
			BINARY_OFFSET_VALUE,
			instance.orElse(0L)
		);
	}

	@Override
	public OptionalLong create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		final boolean isPresent = bytes.read_boolean(BINARY_OFFSET_IS_PRESENT);
		
		// luckily, an uninitialized instance (all-zeroes, meaning isPresent == false) is all that is required.
		return isPresent
			? OptionalLong.of(
				bytes.read_long(BINARY_OFFSET_VALUE)
			)
			: XMemory.instantiateBlank(OptionalLong.class)
		;
	}
	
}
