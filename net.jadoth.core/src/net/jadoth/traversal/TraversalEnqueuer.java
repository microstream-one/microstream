package net.jadoth.traversal;

public interface TraversalEnqueuer
{
	public boolean skip(Object instance);
	
	public void enqueue(Object instance);
}
