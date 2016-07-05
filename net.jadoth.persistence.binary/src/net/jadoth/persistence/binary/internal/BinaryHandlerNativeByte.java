package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeByte extends AbstractBinaryHandlerNativeCustom<Byte>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeByte(final long tid)
	{
		super(tid, Byte.class, defineValueType(byte.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

//	@Override
//	public long getFixedBinaryContentLength()
//	{
//		return 1L;
//	}

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return false;
	}

	@Override
	public boolean hasVariableBinaryLengthInstances()
	{
		return false;
	}

	@Override
	public void store(final Binary bytes, final Byte instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeByte(bytes, this.typeId(), oid, instance.byteValue());
	}

	@Override
	public Byte create(final Binary bytes)
	{
		return BinaryPersistence.buildByte(bytes);
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

}
