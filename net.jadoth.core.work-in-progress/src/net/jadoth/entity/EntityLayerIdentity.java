package net.jadoth.entity;

public abstract class EntityLayerIdentity<E extends Entity<E>> extends EntityLayer<E>
{
	protected EntityLayerIdentity()
	{
		super(null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final E $entity()
	{
		// the safety of this cast has to be guaranteed by the extending implementation.
		return (E)this;
	}
	
	@Override
	public boolean $updateData(final E newData)
	{
		// the passed data instance must be validated before it gets passed to any other layer logic.
		this.$validateNewData(newData.$data());
		return super.$updateData(newData);
	}
}
