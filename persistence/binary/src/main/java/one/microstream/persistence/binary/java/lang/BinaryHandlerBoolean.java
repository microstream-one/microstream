package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBoolean extends AbstractBinaryHandlerCustomValueFixedLength<Boolean, Boolean>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerBoolean New()
	{
		return new BinaryHandlerBoolean();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerBoolean()
	{
		super(Boolean.class, defineValueType(boolean.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static boolean instanceState(final Boolean instance)
	{
		return instance.booleanValue();
	}
	
	private static boolean binaryState(final Binary data)
	{
		return data.read_boolean(0);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final Boolean                         instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeBoolean(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public Boolean create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildBoolean();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Boolean getValidationStateFromInstance(final Boolean instance)
	{
		// well, lol
		return instance;
	}

	// actually never called, just to satisfy the interface
	@Override
	public Boolean getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Boolean                instance,
		final PersistenceLoadHandler handler
	)
	{
		final boolean instanceState = instanceState(instance);
		final boolean binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
