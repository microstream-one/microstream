package one.microstream.entity;

public interface EntityLayerProviderProvider
{
	public <E extends Entity<E>> EntityLayerProvider<E> provideEntityLayerProvider();
}
