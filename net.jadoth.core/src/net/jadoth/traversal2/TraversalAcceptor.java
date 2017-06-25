package net.jadoth.traversal2;

public interface TraversalAcceptor
{
	public <T> void handleInstance(T instance, Object parent, ReferenceAccessor<T> accessor, TraversalEnqueuer enqueuer);
}
