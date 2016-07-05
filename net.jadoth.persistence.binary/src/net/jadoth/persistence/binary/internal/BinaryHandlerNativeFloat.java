package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeFloat extends AbstractBinaryHandlerNativeCustom<Float>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeFloat(final long tid)
	{
		super(tid, Float.class, defineValueType(float.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

//	@Override
//	public long getFixedBinaryContentLength()
//	{
//		return 4L;
//	}

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return false;
	}

	@Override
	public void store(final Binary bytes, final Float instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeFloat(bytes, this.typeId(), oid, instance.floatValue());
	}

	@Override
	public Float create(final Binary bytes)
	{
		return BinaryPersistence.buildFloat(bytes);
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
