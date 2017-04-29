package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeBoolean extends AbstractBinaryHandlerNativeCustomValueFixedLength<Boolean>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeBoolean()
	{
		super(Boolean.class, defineValueType(boolean.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final Boolean instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeBoolean(bytes, this.typeId(), oid, instance.booleanValue());
	}

	@Override
	public Boolean create(final Binary bytes)
	{
		return BinaryPersistence.buildBoolean(bytes);
	}

}
