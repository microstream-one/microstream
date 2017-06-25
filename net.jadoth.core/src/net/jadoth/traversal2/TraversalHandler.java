package net.jadoth.traversal2;

public interface TraversalHandler<T>
{
	public void handleReferences(T instance, TraversalAcceptor acceptor, TraversalEnqueuer enquueer);
	
	public ReferenceAccessor<T> accessor();
}
