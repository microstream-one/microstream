package one.microstream.java.lang;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeVoid extends AbstractBinaryHandlerCustom<Void>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeVoid()
	{
		super(Void.class, X.empty());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Void instance, final long oid, final PersistenceStoreHandler handler)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Void create(final Binary bytes)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean hasPersistedReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

}
