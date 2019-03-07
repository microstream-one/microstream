package net.jadoth.entity;

public interface TransactionContext extends EntityLayerProviderProvider
{
	public <E extends Entity<E>> E lookupData(Committable<E> entity);
	
	public <E extends Entity<E>> E ensureData(Committable<E> entity);
	
	public <E extends Entity<E>> E updateData(Committable<E> entity, E newData);

	@Override
	public default <E extends Entity<E>> EntityLayerProvider<E> provideEntityLayerProvider()
	{
		return e ->
			new EntityLayerTransactional<>(e, this)
		;
	}
	
	
	
	public interface Committable<E extends Entity<E>> extends Entity<E>
	{
		public E actualData();
		
		public void commit();
	}
	
}
