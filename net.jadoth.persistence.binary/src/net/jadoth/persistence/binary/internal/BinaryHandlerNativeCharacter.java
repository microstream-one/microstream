package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeCharacter extends AbstractBinaryHandlerNativeCustom<Character>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeCharacter(final long tid)
	{
		super(tid, Character.class, defineValueType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

//	@Override
//	public long getFixedBinaryContentLength()
//	{
//		return 2L;
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
	public void store(final Binary bytes, final Character instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeCharacter(bytes, this.typeId(), oid, instance.charValue());
	}

	@Override
	public Character create(final Binary bytes)
	{
		return BinaryPersistence.buildCharacter(bytes);
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

}
