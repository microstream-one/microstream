package one.microstream.entity;

public interface EntityLayerProvider<E extends Entity<E>>
{
	public Entity<E> provideEntityLayer(Entity<E> innerInstance);
}
