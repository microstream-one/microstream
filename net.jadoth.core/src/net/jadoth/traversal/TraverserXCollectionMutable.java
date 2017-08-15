package net.jadoth.traversal;

import net.jadoth.collections.types.XReplacingBag;


public final class TraverserXCollectionMutable implements TypeTraverser<XReplacingBag<Object>>
{
	@Override
	public final void traverseReferences(
		final XReplacingBag<Object> instance,
		final TraversalEnqueuer     enqueuer,
		final TraversalAcceptor     acceptor
	)
	{
		try
		{
			instance.iterate(current ->
			{
				if(acceptor.acceptReference(current, instance))
				{
					enqueuer.enqueue(current);
				}
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XReplacingBag<Object> instance        ,
		final TraversalEnqueuer     enqueuer        ,
		final TraversalMutator      mutator         ,
		final MutationListener      mutationListener
	)
	{
		try
		{
			instance.substitute(current ->
			{
				final Object returned = mutator.mutateReference(current, instance);
				if(mutationListener != null)
				{
					try
					{
						mutationListener.registerChange(instance, current, returned);
					}
					catch(final TraversalSignalSkipEnqueueReference s)
					{
						return returned; // skip enqueue call (clever! 8-))
					}
				}
				enqueuer.enqueue(returned);
				
				return returned;
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XReplacingBag<Object> instance        ,
		final TraversalEnqueuer     enqueuer        ,
		final TraversalAcceptor     acceptor        ,
		final TraversalMutator      mutator         ,
		final MutationListener      mutationListener
	)
	{
		try
		{
			instance.substitute(current ->
			{
				if(acceptor.acceptReference(current, instance))
				{
					enqueuer.enqueue(current);
				}
				
				final Object returned = mutator.mutateReference(current, instance);
				if(mutationListener != null)
				{
					try
					{
						mutationListener.registerChange(instance, current, returned);
					}
					catch(final TraversalSignalSkipEnqueueReference s)
					{
						return returned; // skip enqueue call (clever! 8-))
					}
				}
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
