package net.jadoth.traversal2;

import net.jadoth.collections.types.XReplacingBag;


public final class TraverserXCollection implements TraversalHandler
{
	@SuppressWarnings("unchecked")
	@Override
	public void traverseReferences(final Object instance, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
	{
		((XReplacingBag<Object>)instance).substitute(current ->
		{
			return acceptor.acceptInstance(current, instance, enqueuer);
		});
	}
}
