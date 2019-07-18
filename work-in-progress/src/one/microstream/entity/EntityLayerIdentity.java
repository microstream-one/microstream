package one.microstream.entity;

public abstract class EntityLayerIdentity extends EntityLayer
{
	protected EntityLayerIdentity()
	{
		super(null);
	}
	
	@Override
	public final Entity $entityIdentity()
	{
		return this;
	}
	
	@Override
	public boolean $updateEntityData(final Entity newData)
	{
		// the passed data instance must be validated before it gets passed to any other layer logic.
		this.$validateNewData(Entity.data(newData));
		return super.$updateEntityData(newData);
	}
}
