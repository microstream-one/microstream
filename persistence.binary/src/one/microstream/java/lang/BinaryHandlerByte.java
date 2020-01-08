package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerByte extends AbstractBinaryHandlerCustomValueFixedLength<Byte>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerByte New()
	{
		return new BinaryHandlerByte();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerByte()
	{
		super(Byte.class, defineValueType(byte.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static byte instanceState(final Byte instance)
	{
		return instance.byteValue();
	}
	
	private static byte binaryState(final Binary data)
	{
		return data.read_byte(0);
	}

	@Override
	public void store(final Binary data, final Byte instance, final long objectId, final PersistenceStoreHandler handler)
	{
		data.storeByte(this.typeId(), objectId, instance.byteValue());
	}

	@Override
	public Byte create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildByte();
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Byte                   instance,
		final PersistenceLoadHandler handler
	)
	{
		final byte instanceState = instanceState(instance);
		final byte binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
