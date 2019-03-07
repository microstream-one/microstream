package net.jadoth.entity;

public abstract class EntityLayer<E extends Entity<E>> implements Entity<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private Entity<E> inner;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public EntityLayer(final Entity<E> inner)
	{
		super();
		this.inner = inner; // may be null in case of delayed initialization.
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public E $entity()
	{
		// the data instance (and only that) has a back-reference to the actual entity instance it belongs to.
		return this.inner.$entity();
	}
	
	@Override
	public E $data()
	{
		return this.inner.$data();
	}
	
	protected Entity<E> $inner()
	{
		return this.inner;
	}
	
	protected void $validateNewData(final E newData)
	{
		// empty default implementation
		if(newData.$entity() != this.$entity())
		{
			// (10.12.2017 TM)EXCP: proper exception
			throw new RuntimeException("Entity identity mismatch.");
		}
	}
	
	protected void $updateDataValidating(final E newData)
	{
		final E actualNewData = newData.$data();
		this.$validateNewData(actualNewData);
		this.$setInner(actualNewData);
	}
	
	protected void $setInner(final Entity<E> inner)
	{
		this.inner = inner;
	}
	
	@Override
	public boolean $updateData(final E newData)
	{
		/*
		 *  if the inner layer instance reports success, it is an intermediate layer.
		 *  Otherwise, it is the data itself and needs to be replaced.
		 */
		if(!this.inner.$updateData(newData))
		{
			this.$updateDataValidating(newData);
		}
		
		return true;
	}
}
