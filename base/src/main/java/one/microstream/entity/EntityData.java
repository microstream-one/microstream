package one.microstream.entity;

/**
 * Immutable entity data layer.
 * 
 * , FH
 */
public abstract class EntityData extends Entity.AbstractAccessible
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Entity entity;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected EntityData(final Entity entity)
	{
		super();
		this.entity = Entity.identity(entity);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected final Entity entityIdentity()
	{
		return this.entity;
	}
	
	@Override
	protected final Entity entityData()
	{
		return this;
	}
	
	@Override
	protected final void entityCreated()
	{
		// nothing to do here
	}
		
	@Override
	protected final boolean updateEntityData(final Entity newData)
	{
		// updating an entity's data means to replace the data instance, not mutate it. Data itself is immutable.
		return false;
	}
}
