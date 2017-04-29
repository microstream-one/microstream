package net.jadoth.persistence.binary.internal;

import java.math.BigInteger;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

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
	public void store(final Binary bytes, final BigInteger instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeArray_byte(bytes, this.typeId(), oid, instance.toByteArray());
	}

	@Override
	public BigInteger create(final Binary bytes)
	{
		return new BigInteger(BinaryPersistence.buildArray_byte(bytes));
	}
	
}
