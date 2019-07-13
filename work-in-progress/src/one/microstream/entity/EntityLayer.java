package one.microstream.entity;

public abstract class EntityLayer implements Entity
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private Entity inner;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public EntityLayer(final Entity inner)
	{
		super();
		this.inner = inner; // may be null in case of delayed initialization.
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Entity $entity()
	{
		// the data instance (and only that) has a back-reference to the actual entity instance it belongs to.
		return this.inner.$entity();
	}
	
	@Override
	public Entity $data()
	{
		return this.inner.$data();
	}
	
	protected Entity $inner()
	{
		return this.inner;
	}
	
	protected void $validateNewData(final Entity newData)
	{
		// empty default implementation
		if(newData.$entity() != this.$entity())
		{
			// (10.12.2017 TM)EXCP: proper exception
			throw new RuntimeException("Entity identity mismatch.");
		}
	}
	
	protected void $updateDataValidating(final Entity newData)
	{
		final Entity actualNewData = newData.$data();
		this.$validateNewData(actualNewData);
		this.$setInner(actualNewData);
	}
	
	protected void $setInner(final Entity inner)
	{
		this.inner = inner;
	}
	
	@Override
	public boolean $updateData(final Entity newData)
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
