package one.microstream.entity;

public abstract class EntityData implements Entity
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
		this.entity = entity.$entity();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Entity $entity()
	{
		return this.entity;
	}
	
	@Override
	public final Entity $data()
	{
		return (Entity)this;
	}
	
	@Override
	public final boolean $updateData(final Entity newData)
	{
		// updating an entity's data means to replace the data instance, not mutate it. Data itself is immutable.
		return false;
	}
	
	@Override
	public abstract boolean equals(Object obj);
		
	@Override
	public abstract int hashCode();
}
