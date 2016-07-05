package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeDouble extends AbstractBinaryHandlerNativeCustom<Double>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeDouble(final long tid)
	{
		super(tid, Double.class, defineValueType(double.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

//	@Override
//	public long getFixedBinaryContentLength()
//	{
//		return 8L;
//	}

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return false;
	}

	@Override
	public void store(final Binary bytes, final Double instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeDouble(bytes, this.typeId(), oid, instance.doubleValue());
	}

	@Override
	public Double create(final Binary bytes)
	{
		return BinaryPersistence.buildDouble(bytes);
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public boolean hasVariableBinaryLengthInstances()
	{
		return false;
	}

}
