package one.microstream.java.math;

import java.math.BigInteger;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBigInteger extends AbstractBinaryHandlerCustomValueVariableLength<BigInteger>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerBigInteger()
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

	@Override
	public void store(final Binary bytes, final BigInteger instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_byte(this.typeId(), objectId, instance.toByteArray());
	}

	@Override
	public BigInteger create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new BigInteger(bytes.buildArray_byte());
	}
	
}
