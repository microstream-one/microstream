package one.microstream.java.time;

import java.time.ZoneId;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

// this is an "abstract type" TypeHandler that handles all classes implementing ZoneId, not as the actual class.
public final class BinaryHandlerZoneId extends AbstractBinaryHandlerCustomValueVariableLength<ZoneId, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerZoneId New()
	{
		return new BinaryHandlerZoneId();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerZoneId()
	{
		super(
			ZoneId.class,
			CustomFields(
				chars("id")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	private static String instanceState(final ZoneId instance)
	{
		return instance.getId();
	}

	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public void store(
		final Binary                          data    ,
		final ZoneId                          instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public ZoneId create(final Binary data, final PersistenceLoadHandler handler)
	{
		return ZoneId.of(binaryState(data));
	}



	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////

	@Override
	public String getValidationStateFromInstance(final ZoneId instance)
	{
		return instanceState(instance);
	}

	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
