package one.microstream.persistence.binary.java.util;

import java.util.OptionalDouble;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerOptionalDouble
extends AbstractBinaryHandlerCustomValueFixedLength<OptionalDouble, Double>
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
	
	private static double instanceState(final OptionalDouble instance)
	{
		// or ELSE!!!
		return instance.orElse(0.0);
	}
	
	private static double binaryState(final Binary data)
	{
		return data.read_double(BINARY_OFFSET_VALUE);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final OptionalDouble                  instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		data.store_boolean(
			BINARY_OFFSET_IS_PRESENT,
			instance.isPresent()
		);

		data.store_double(
			BINARY_OFFSET_VALUE,
			instanceState(instance)
		);
	}

	@Override
	public OptionalDouble create(final Binary data, final PersistenceLoadHandler handler)
	{
		final boolean isPresent = data.read_boolean(BINARY_OFFSET_IS_PRESENT);
		
		// luckily, an uninitialized instance (all-zeroes, meaning isPresent == false) is all that is required.
		return isPresent
			? OptionalDouble.of(
				data.read_double(BINARY_OFFSET_VALUE)
			)
			: XMemory.instantiateBlank(OptionalDouble.class)
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Double getValidationStateFromInstance(final OptionalDouble instance)
	{
		return instanceState(instance);
	}

	// actually never called, just to satisfy the interface
	@Override
	public Double getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final OptionalDouble         instance,
		final PersistenceLoadHandler handler
	)
	{
		final double instanceState = instanceState(instance);
		final double binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}
	
}
