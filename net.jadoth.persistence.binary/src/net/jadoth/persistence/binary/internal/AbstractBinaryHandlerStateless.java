package net.jadoth.persistence.binary.internal;

import net.jadoth.X;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceBuildLinker;
import net.jadoth.persistence.types.PersistenceHandler;

public abstract class AbstractBinaryHandlerStateless<T> extends AbstractBinaryHandlerNativeCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerStateless(final Class<T> type)
	{
		super(type, X.empty());
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(final Binary bytes, final T instance, final long oid, final PersistenceHandler handler)
	{
		BinaryPersistence.storeStateless(bytes, this.typeId(), oid);
	}

	@Override
	public final void update(final Binary bytes, final T instance, final PersistenceBuildLinker builder)
	{
		// no-op
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

//	@Override
//	public final void copy(final T source, final T target)
//	{
//		// well it can be called, no problem, but it won't (can't) do anything.
//	}

}
