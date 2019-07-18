package one.microstream.entity;

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
	protected final Entity $entityIdentity()
	{
		return this.entity;
	}
	
	@Override
	protected final Entity $entityData()
	{
		return this;
	}
		
	@Override
	protected final boolean $updateEntityData(final Entity newData)
	{
		// updating an entity's data means to replace the data instance, not mutate it. Data itself is immutable.
		return false;
	}
	
	/* (18.07.2019 TM)FIXME: dangerous value-type equality for entity data
	 * It is dangerous or actually simply wrong to implement the data instance to use value-type equality
	 * but leave the actual entity identity implementation at identity equality.
	 * That would cause hashing logic to behave differently depending on which instance of an entity
	 * they encounter.
	 * Equality would have to be implemented equally accross all entity instances.
	 * But then again, except for real value types, equality of an entity depends on the context,
	 * not the implementation. That is why HashEqualators exist.
	 * If done at all, implementation-hardcoded equality should be generated optionally
	 */
	
	@Override
	public abstract boolean equals(Object obj);
		
	@Override
	public abstract int hashCode();
}
