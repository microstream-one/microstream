package one.microstream.entity;

public interface TransactionContext extends EntityLayerProviderProvider
{
	public Entity lookupData(Committable entity);
	
	public Entity ensureData(Committable entity);
	
	public Entity updateData(Committable entity, Entity newData);

	@Override
	public default EntityLayerProvider provideEntityLayerProvider()
	{
		return e ->
			new EntityLayerTransactional(e, this)
		;
	}
	
	
	
	public interface Committable extends Entity
	{
		public Entity actualData();
		
		public void commit();
	}
	
}
