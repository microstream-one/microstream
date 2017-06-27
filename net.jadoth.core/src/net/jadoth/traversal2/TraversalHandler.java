package net.jadoth.traversal2;

public interface TraversalHandler
{
	public void traverseReferences(Object instance, TraversalAcceptor acceptor, TraversalEnqueuer enqueuer);
	
}
