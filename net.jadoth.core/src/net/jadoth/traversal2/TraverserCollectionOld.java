package net.jadoth.traversal2;

import java.util.Collection;


public final class TraverserCollectionOld implements TraversalHandler<Collection<?>>
{
	@Override
	public final void traverseReferences(
		final Collection<?>     instance,
		final TraversalAcceptor acceptor,
		final TraversalEnqueuer enqueuer
	)
	{
		try
		{
			instance.forEach(current ->
			{
				final Object returned;
				if((returned = acceptor.acceptInstance(current, instance, enqueuer)) != current)
				{
					throw new UnsupportedOperationException();
				}
				
				// note: if the current (now prior) value has to be enqueued, the acceptor can do that internally
				enqueuer.enqueue(returned);
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		
	}
	
}
