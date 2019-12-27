package one.microstream.persistence.binary.internal;

import one.microstream.X;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

public abstract class AbstractBinaryHandlerStateless<T> extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerStateless(final Class<T> type)
	{
		super(type, X.empty());
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(
		final Binary                  bytes   ,
		final T                       instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeStateless(this.typeId(), objectId);
	}

	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
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
	
	@Override
	public final void iterateLoadableReferences(
		final Binary                      medium  ,
		final PersistenceReferenceLoader iterator
	)
	{
		// no state, no reference, no-op.
	}

}
