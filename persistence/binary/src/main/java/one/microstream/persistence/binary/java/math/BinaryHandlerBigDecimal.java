package one.microstream.persistence.binary.java.math;

import java.math.BigDecimal;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBigDecimal extends AbstractBinaryHandlerCustomValueVariableLength<BigDecimal, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerBigDecimal New()
	{
		return new BinaryHandlerBigDecimal();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerBigDecimal()
	{
		super(
			BigDecimal.class,
			CustomFields(
				chars("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final BigDecimal instance)
	{
		return instance.toString();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public void store(
		final Binary                          data    ,
		final BigDecimal                      instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// there's a char[] constructor but no char[] utility method, so there's no other option than this
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public BigDecimal create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new BigDecimal(binaryState(data));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final BigDecimal instance)
	{
		return instanceState(instance);
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
