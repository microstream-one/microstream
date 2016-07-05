package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeString extends AbstractBinaryHandlerNativeCustom<String>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeString(final long typeId)
	{
		super(typeId, String.class, pseudoFields(
			chars("value")
		));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return true;
	}

	@Override
	public void store(final Binary bytes, final String instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeStringValue(bytes, this.typeId(), oid, instance);
	}

	@Override
	public String create(final Binary bytes)
	{
		return BinaryPersistence.buildString(bytes);
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
