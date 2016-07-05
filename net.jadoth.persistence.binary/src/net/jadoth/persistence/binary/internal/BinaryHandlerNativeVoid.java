package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.X;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNativeVoid extends AbstractBinaryHandlerNativeCustom<Void>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeVoid(final long tid)
	{
		super(tid, Void.class, X.<PersistenceTypeDescriptionMemberPseudoField>empty());
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
	public void store(final Binary bytes, final Void instance, final long oid, final SwizzleStoreLinker linker)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Void create(final Binary bytes)
	{
		throw new UnsupportedOperationException();
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
