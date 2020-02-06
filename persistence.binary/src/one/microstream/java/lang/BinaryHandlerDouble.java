package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerDouble extends AbstractBinaryHandlerCustomValueFixedLength<Double>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerDouble New()
	{
		return new BinaryHandlerDouble();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerDouble()
	{
		super(Double.class, defineValueType(double.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static double instanceState(final Double instance)
	{
		return instance.doubleValue();
	}
	
	private static double binaryState(final Binary data)
	{
		return data.read_double(0);
	}

	@Override
	public void store(final Binary data, final Double instance, final long objectId, final PersistenceStoreHandler handler)
	{
		data.storeDouble(this.typeId(), objectId, instance.doubleValue());
	}

	@Override
	public Double create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildDouble();
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Double                 instance,
		final PersistenceLoadHandler handler
	)
	{
		final double instanceState = instanceState(instance);
		final double binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
