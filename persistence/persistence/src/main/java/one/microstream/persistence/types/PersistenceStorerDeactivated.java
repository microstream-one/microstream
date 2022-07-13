package one.microstream.persistence.types;

import one.microstream.persistence.exceptions.PersistenceExceptionStorerDeactivated;

/**
 * A {@link one.microstream.persistence.types.PersistenceStorer PersistenceStorer} implementation
 * that always throws {@link one.microstream.persistence.exceptions.PersistenceExceptionStorerDeactivated PersistenceExceptionStorerDeactivated}.
 */
public class PersistenceStorerDeactivated implements PersistenceStorer
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Constructor
	 */
	public PersistenceStorerDeactivated()
	{
		super();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Object commit()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public void clear()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public boolean skipMapped(final Object instance, final long objectId)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public boolean skip(final Object instance)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public boolean skipNulled(final Object instance)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long size()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long currentCapacity()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long maximumCapacity()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long store(final Object instance)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long[] storeAll(final Object... instances)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public void storeAll(final Iterable<?> instances)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public PersistenceStorer reinitialize()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public PersistenceStorer reinitialize(final long initialCapacity)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public PersistenceStorer ensureCapacity(final long desiredCapacity)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

}
