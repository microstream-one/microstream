package one.microstream.entity;

public abstract class EntityLayerIdentity extends EntityLayer
{
	protected EntityLayerIdentity()
	{
		super(null);
	}
	
	@Override
	public final Entity $entity()
	{
		return this;
	}
	
	@Override
	public boolean $updateData(final Entity newData)
	{
		// the passed data instance must be validated before it gets passed to any other layer logic.
		this.$validateNewData(newData.$data());
		return super.$updateData(newData);
	}
}
