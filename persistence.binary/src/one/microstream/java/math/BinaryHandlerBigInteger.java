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
			pseudoFields(
				bytes("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final BigInteger instance, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_byte(this.typeId(), oid, instance.toByteArray());
	}

	@Override
	public BigInteger create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new BigInteger(bytes.buildArray_byte());
	}
	
}
