package net.jadoth.traversal2;

import net.jadoth.collections.types.XReplacingBag;


public final class TraverserXCollectionMutable implements TraversalHandler
{
	@SuppressWarnings("unchecked")
	@Override
	public final void traverseReferences(
		final Object            instance,
		final TraversalAcceptor acceptor,
		final TraversalEnqueuer enqueuer
	)
	{
		try
		{
			((XReplacingBag<Object>)instance).substitute(current ->
			{
				final Object returned = acceptor.acceptInstance(current, instance, enqueuer);
				
				// note: if the current (now prior) value has to be enqueued, the acceptor can do that internally
				enqueuer.enqueue(returned);
				
				return returned;
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		
	}
	
}
