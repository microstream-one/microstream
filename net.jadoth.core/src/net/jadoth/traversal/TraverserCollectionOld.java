package net.jadoth.traversal;

import java.util.Collection;


public final class TraverserCollectionOld implements TypeTraverser<Collection<?>>
{
	@Override
	public final void traverseReferences(
		final Collection<?>     instance        ,
		final TraversalMutator  mutator         ,
		final TraversalEnqueuer enqueuer        ,
		final MutationListener  mutationListener
	)
	{
		try
		{
			instance.forEach(current ->
			{
				final Object returned;
				if((returned = mutator.mutateReference(current, instance, enqueuer)) != current)
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

	@Override
	public void traverseReferences(
		final Collection<?>     instance,
		final TraversalAcceptor acceptor,
		final TraversalEnqueuer enqueuer
	)
	{
		try
		{
			instance.forEach(current ->
			{
				acceptor.acceptReference(current, instance, enqueuer);
				enqueuer.enqueue(current);
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
}
