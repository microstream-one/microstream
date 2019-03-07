package one.microstream.entity;

public abstract class EntityData<E extends Entity<E>> implements Entity<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final E entity;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected EntityData(final E entity)
	{
		super();
		this.entity = entity.$entity();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final E $entity()
	{
		return this.entity;
	}
			
	@SuppressWarnings("unchecked")
	@Override
	public final E $data()
	{
		return (E)this;
	}
	
	@Override
	public final boolean $updateData(final E newData)
	{
		// updating an entity's data means to replace the data instance, not mutate it. Data itself is immutable.
		return false;
	}
	
}
