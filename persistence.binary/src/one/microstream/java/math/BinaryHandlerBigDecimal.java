package one.microstream.java.math;

import java.math.BigDecimal;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBigDecimal extends AbstractBinaryHandlerCustomValueVariableLength<BigDecimal>
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

	@Override
	public void store(final Binary bytes, final BigDecimal instance, final long objectId, final PersistenceStoreHandler handler)
	{
		// there's a char[] constructor but no char[] utility method, so there's no other option than this
		bytes.storeStringValue(this.typeId(), objectId, instance.toString());
	}

	@Override
	public BigDecimal create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new BigDecimal(bytes.buildArray_char());
	}

}
