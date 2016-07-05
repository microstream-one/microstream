package net.jadoth.persistence.binary.internal;

import java.math.BigDecimal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerBigDecimal extends AbstractBinaryHandlerNativeCustom<BigDecimal>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerBigDecimal(final long tid)
	{
		super(tid, BigDecimal.class, pseudoFields(
			chars("value")
		));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final BigDecimal instance, final long oid, final SwizzleStoreLinker linker)
	{
		// there's a char[] constructor but no char[] utility method, so there's no other option than this
		BinaryPersistence.storeStringValue(bytes, this.typeId(), oid, instance.toString());
	}

	@Override
	public BigDecimal create(final Binary bytes)
	{
		return new BigDecimal(BinaryPersistence.buildArray_char(bytes));
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return true;
	}

	@Override
	public boolean hasVariableBinaryLengthInstances()
	{
		return false;
	}

}
