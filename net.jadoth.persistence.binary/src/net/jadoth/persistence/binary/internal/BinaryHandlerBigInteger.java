package net.jadoth.persistence.binary.internal;

import java.math.BigInteger;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceHandler;

public final class BinaryHandlerBigInteger extends AbstractBinaryHandlerNativeCustomValueVariableLength<BigInteger>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

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
	public void store(final Binary bytes, final BigInteger instance, final long oid, final PersistenceHandler handler)
	{
		BinaryPersistence.storeArray_byte(bytes, this.typeId(), oid, instance.toByteArray());
	}

	@Override
	public BigInteger create(final Binary bytes)
	{
		return new BigInteger(BinaryPersistence.buildArray_byte(bytes));
	}
	
}
