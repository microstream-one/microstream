package one.microstream.storage.restadapter.types;

import one.microstream.collections.types.XGettingTable;
import one.microstream.hashing.HashStatistics;
import one.microstream.persistence.types.PersistenceAcceptor;
import one.microstream.persistence.types.PersistenceObjectRegistry;

/**
 * Implements a disabled {@link PersistenceObjectRegistry}.
 */
public class ViewerObjectRegistryDisabled implements PersistenceObjectRegistry
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ViewerObjectRegistryDisabled()
	{
		super();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public PersistenceObjectRegistry Clone()
	{
		return null;
	}

	@Override
	public long lookupObjectId(final Object object)
	{
		return 0;
	}

	@Override
	public Object lookupObject(final long objectId)
	{
		return null;
	}

	@Override
	public boolean containsObjectId(final long objectId)
	{
		return false;
	}

	@Override
	public <A extends PersistenceAcceptor> A iterateEntries(final A acceptor)
	{
		return null;
	}

	@Override
	public long size()
	{
		return 0;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public int hashRange()
	{
		return 0;
	}

	@Override
	public float hashDensity()
	{
		return 0;
	}

	@Override
	public long minimumCapacity()
	{
		return 0;
	}

	@Override
	public long capacity()
	{
		return 0;
	}

	@Override
	public boolean setHashDensity(final float hashDensity)
	{
		return false;
	}

	@Override
	public boolean setMinimumCapacity(final long minimumCapacity)
	{
		return false;
	}

	@Override
	public boolean setConfiguration(final float hashDensity, final long minimumCapacity)
	{
		return false;
	}

	@Override
	public boolean ensureCapacity(final long capacity)
	{
		return false;
	}

	@Override
	public boolean registerObject(final long objectId, final Object object)
	{
		return false;
	}

	@Override
	public Object optionalRegisterObject(final long objectId, final Object object)
	{
		return object;
	}

	@Override
	public boolean registerConstant(final long objectId, final Object constant)
	{
		return false;
	}

	@Override
	public boolean consolidate()
	{
		return false;
	}

	@Override
	public void clear()
	{
		// no-op
	}

	@Override
	public void clearAll()
	{
		// no-op
	}

	@Override
	public void truncate()
	{
		// no-op
	}

	@Override
	public void truncateAll()
	{
		// no-op
	}

	@Override
	public XGettingTable<String, ? extends HashStatistics> createHashStatistics()
	{
		return null;
	}

	@Override
	public boolean isValid(final long objectId, final Object object)
	{
		return true;
	}

	@Override
	public void validate(final long objectId, final Object object)
	{
		// no-op
	}

}
