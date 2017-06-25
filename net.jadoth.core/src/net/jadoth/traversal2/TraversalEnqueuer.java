package net.jadoth.traversal2;

public interface TraversalEnqueuer
{
	public boolean skip(Object instance);
	
	public void enqueue(Object instance);
}
