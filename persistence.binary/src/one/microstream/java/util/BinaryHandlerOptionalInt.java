package one.microstream.java.util;

import java.util.OptionalInt;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerOptionalInt extends AbstractBinaryHandlerCustomValueFixedLength<OptionalInt>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long
		BINARY_OFFSET_IS_PRESENT =                                        0,
		BINARY_OFFSET_VALUE      = BINARY_OFFSET_IS_PRESENT + Byte   .BYTES, // Boolean.BYTES does not exist
		BINARY_LENGTH            = BINARY_OFFSET_VALUE      + Integer.BYTES
	;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerOptionalInt()
	{
		/*
		 * Note on fields:
		 * These are not tied to JDK-specific internals. It's merely a sufficient state
		 * (even properly queryable in the implementation, fancy that) to represent an instance.
		 * The identical naming is "by coincidence" and will continue to work even if they change theirs internally.
		 */
		super(
			OptionalInt.class,
			CustomFields(
				CustomField(boolean.class, "isPresent"),
				CustomField(int    .class, "value"    )
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                  bytes   ,
		final OptionalInt             instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		final long contentAddress = bytes.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		bytes.store_boolean(
			contentAddress + BINARY_OFFSET_IS_PRESENT,
			instance.isPresent()
		);
		
		/*
		 * of course no JDK implementation without at least some idiocy:
		 * "orElse" must be the dumbest name on earth for a getter method with that logic
		 * Totally inconsistent to the (also not very bright) "getAsInt", not a verb, not a pseudo-property.
		 * So many errors in such a short and simple method.
		 * What about just "get(int)"? or "getDefaulted(int)"? Or at least "getOrElse(int)"?
		 * Or "value(int)", "valueDefaulted(int)", "valueOrElse(int)"
		 * Or "coalesce(int)" or something like "yield(int)" or "peek(int)" or such.
		 * No, "orElse" was the best they could come up with.
		 * And that is not even the shortest, in case that was their short-sighted priority.
		 * "Finish the implementation now, OR ELSE ...!"
		 * Interns, interns everywhere.
		 */
		bytes.store_int(
			contentAddress + BINARY_OFFSET_VALUE,
			instance.orElse(0)
		);
	}

	@Override
	public OptionalInt create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		final boolean isPresent = bytes.get_boolean(BINARY_OFFSET_IS_PRESENT);
		
		// luckily, an uninitialized instance (all-zeroes, meaning isPresent == false) is all that is required.
		return isPresent
			? OptionalInt.of(
				bytes.get_int(BINARY_OFFSET_VALUE)
			)
			: XMemory.instantiate(OptionalInt.class)
		;
	}
	
}
