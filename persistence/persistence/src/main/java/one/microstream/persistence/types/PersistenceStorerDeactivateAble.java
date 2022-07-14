package one.microstream.persistence.types;

/**
 * A {@link one.microstream.persistence.types.PersistenceStorer PersistenceStorer} implementation that allows
 * switching between the supplied {@code PersistenceStorer} instance and a
 * {@link one.microstream.persistence.types.PersistenceStorerDeactivated PersistenceStorerDeactivated}
 * instance.
 *
 */
public class PersistenceStorerDeactivateAble implements PersistenceStorer {

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private       PersistenceStorer actual;
	private final PersistenceStorer fullFeaturedStorer;
	private final PersistenceStorer noOpStorer;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public PersistenceStorerDeactivateAble(final PersistenceStorer persistenceStorer)
	{
		this.fullFeaturedStorer = persistenceStorer;
		this.noOpStorer = new PersistenceStorerDeactivated();
		this.actual = this.fullFeaturedStorer;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	/**
	 * Enable or disable writing support.
	 * 
	 * @param enabledWrites
	 */
	public void enableWrites(final boolean enabledWrites)
	{
		if(enabledWrites)
		{
			this.actual = this.fullFeaturedStorer;
		}
		else
		{
			this.actual = this.noOpStorer;
		}
	}
	
	/**
	 * Enable writing support.
	 */
	public void enableWrites()
	{
		this.actual = this.fullFeaturedStorer;
	}
	
	/**
	 * Disable writing support
	 */
	public void disableWrites()
	{
		this.actual = this.noOpStorer;
	}

	@Override
	public PersistenceStorer reinitialize()
	{
		return this.actual.reinitialize();
	}

	@Override
	public long store(final Object instance)
	{
		return this.actual.store(instance);
	}

	@Override
	public PersistenceStorer reinitialize(final long initialCapacity)
	{
		return this.actual.reinitialize(initialCapacity);
	}

	@Override
	public Object commit()
	{
		return this.actual.commit();
	}

	@Override
	public PersistenceStorer ensureCapacity(final long desiredCapacity)
	{
		return this.actual.ensureCapacity(desiredCapacity);
	}

	@Override
	public void clear()
	{
		this.actual.clear();
	}

	@Override
	public long[] storeAll(final Object... instances)
	{
		return this.actual.storeAll(instances);
	}

	@Override
	public boolean skipMapped(final Object instance, final long objectId)
	{
		return this.actual.skipMapped(instance, objectId);
	}

	@Override
	public void storeAll(final Iterable<?> instances)
	{
		this.actual.storeAll(instances);
	}

	@Override
	public boolean skip(final Object instance)
	{
		return this.actual.skip(instance);
	}

	@Override
	public boolean skipNulled(final Object instance)
	{
		return this.actual.skipNulled(instance);
	}

	@Override
	public long size()
	{
		return this.actual.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.actual.isEmpty();
	}

	@Override
	public long currentCapacity()
	{
		return this.actual.currentCapacity();
	}

	@Override
	public long maximumCapacity()
	{
		return this.actual.maximumCapacity();
	}

	

}
