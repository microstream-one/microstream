package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerFloat extends AbstractBinaryHandlerCustomValueFixedLength<Float>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerFloat New()
	{
		return new BinaryHandlerFloat();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerFloat()
	{
		super(Float.class, defineValueType(float.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static float instanceState(final Float instance)
	{
		return instance.floatValue();
	}
	
	private static float binaryState(final Binary data)
	{
		return data.read_float(0);
	}

	@Override
	public void store(final Binary data, final Float instance, final long objectId, final PersistenceStoreHandler handler)
	{
		data.storeFloat(this.typeId(), objectId, instance.floatValue());
	}

	@Override
	public Float create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildFloat();
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Float                  instance,
		final PersistenceLoadHandler handler
	)
	{
		final float instanceState = instanceState(instance);
		final float binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
