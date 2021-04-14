package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerByte extends AbstractBinaryHandlerCustomValueFixedLength<Byte, Byte>
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
	public void store(
		final Binary                          data    ,
		final Byte                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler)
	{
		data.storeByte(this.typeId(), objectId, instance.byteValue());
	}

	@Override
	public Byte create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildByte();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Byte getValidationStateFromInstance(final Byte instance)
	{
		// well, lol
		return instance;
	}

	// actually never called, just to satisfy the interface
	@Override
	public Byte getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
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
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
