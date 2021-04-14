package one.microstream.persistence.binary.java.math;

import java.math.BigInteger;
import java.util.Arrays;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBigInteger extends AbstractBinaryHandlerCustomValueVariableLength<BigInteger, byte[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerBigInteger New()
	{
		return new BinaryHandlerBigInteger();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerBigInteger()
	{
		super(
			BigInteger.class,
			CustomFields(
				bytes("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static byte[] instanceState(final BigInteger instance)
	{
		return instance.toByteArray();
	}
	
	private static byte[] binaryState(final Binary data)
	{
		return data.build_bytes();
	}

	@Override
	public void store(
		final Binary                          data    ,
		final BigInteger                      instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_bytes(this.typeId(), objectId, instance.toByteArray());
	}

	@Override
	public BigInteger create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new BigInteger(data.build_bytes());
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public byte[] getValidationStateFromInstance(final BigInteger instance)
	{
		return instanceState(instance);
	}

	// actually never called, just to satisfy the interface
	@Override
	public byte[] getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final BigInteger             instance,
		final PersistenceLoadHandler handler
	)
	{
		final byte[] instanceState = instanceState(instance);
		final byte[] binaryState   = binaryState(data);
		
		if(Arrays.equals(instanceState, binaryState))
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, Arrays.toString(instanceState), Arrays.toString(binaryState));
	}
	
}
