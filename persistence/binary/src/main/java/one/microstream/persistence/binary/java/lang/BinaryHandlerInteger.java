package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerInteger extends AbstractBinaryHandlerCustomValueFixedLength<Integer, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerInteger New()
	{
		return new BinaryHandlerInteger();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerInteger()
	{
		super(Integer.class, defineValueType(int.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static int instanceState(final Integer instance)
	{
		return instance.intValue();
	}
	
	private static int binaryState(final Binary data)
	{
		return data.read_int(0);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final Integer                         instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeInteger(this.typeId(), objectId, instance.intValue());
	}

	@Override
	public Integer create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildInteger();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Integer getValidationStateFromInstance(final Integer instance)
	{
		// well, lol
		return instance;
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
		final Integer                instance,
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
