package one.microstream.persistence.binary.java.time;

import static one.microstream.X.Constant;

import java.time.ZoneOffset;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

// custom type handler for zone offset, needed because of transient field ZoneOffset#id
public final class BinaryHandlerZoneOffset extends AbstractBinaryHandlerCustomValueFixedLength<ZoneOffset, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerZoneOffset New()
	{
		return new BinaryHandlerZoneOffset();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerZoneOffset()
	{
		super(
			ZoneOffset.class,
			Constant(
				CustomField(int.class, "totalSeconds")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	private static int instanceState(final ZoneOffset instance)
	{
		return instance.getTotalSeconds();
	}

	private static int binaryState(final Binary data)
	{
		return data.read_int(0L);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final ZoneOffset                      instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeInteger(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public ZoneOffset create(final Binary data, final PersistenceLoadHandler handler)
	{
		return ZoneOffset.ofTotalSeconds(binaryState(data));
	}

	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////

	// actually never called, just to satisfy the interface
	@Override
	public Integer getValidationStateFromInstance(final ZoneOffset instance)
	{
		return instance.getTotalSeconds();
	}

	// actually never called, just to satisfy the interface
	@Override
	public Integer getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

	@Override
	public void validateState(
		final Binary                 data    ,
		final ZoneOffset             instance,
		final PersistenceLoadHandler handler
	)
	{
		final int instanceState = instanceState(instance);
		final int binaryState   = binaryState(data);

		if(instanceState == binaryState)
		{
			return;
		}

		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
