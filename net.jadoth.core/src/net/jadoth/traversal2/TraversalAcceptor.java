package net.jadoth.traversal2;

public interface TraversalAcceptor
{
	public Object acceptInstance(Object instance, Object parent, TraversalEnqueuer enqueuer);
}
