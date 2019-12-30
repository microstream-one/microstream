package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBoolean extends AbstractBinaryHandlerCustomValueFixedLength<Boolean>
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
	public void store(final Binary bytes, final Boolean instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeBoolean(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public Boolean create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.buildBoolean();
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
		
		throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
