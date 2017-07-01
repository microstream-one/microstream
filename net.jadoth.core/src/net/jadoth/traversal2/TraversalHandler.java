package net.jadoth.traversal2;

public interface TraversalHandler<T>
{
	public void traverseReferences(T instance, TraversalAcceptor acceptor, TraversalEnqueuer enqueuer);
	
}
