package net.jadoth.traversal2;

public interface TraversalAcceptor
{
	public void acceptInstance(Object instance, Object parent, ReferenceAccessor accessor, TraversalEnqueuer enqueuer);
}
