package net.jadoth.entity;

public interface EntityLayerProvider<E extends Entity<E>>
{
	public Entity<E> provideEntityLayer(Entity<E> innerInstance);
}
