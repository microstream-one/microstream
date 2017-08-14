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
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				if(acceptor.acceptReference(instance[i], instance))
				{
					enqueuer.enqueue(instance[i]);
				}
			}
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
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current, returned;
				if((returned = mutator.mutateReference(current = instance[i], instance)) != current)
				{
					instance[i] = returned; // must be BEFORE registerChange
					if(mutationListener != null)
					{
						mutationListener.registerChange(instance, current, returned);
					}
				}
				
				enqueuer.enqueue(returned);
			}
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
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current, returned;
				if(acceptor.acceptReference(current = instance[i], instance))
				{
					enqueuer.enqueue(current);
				}
					
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					instance[i] = returned; // must be BEFORE registerChange in case it throws a SkipEnqueue
					if(mutationListener != null)
					{
						try
						{
							mutationListener.registerChange(instance, current, returned);
						}
						catch(final TraversalSignalSkipEnqueueReference s)
						{
							continue; // skip enqueue call (clever! 8-))
						}
					}
					enqueuer.enqueue(returned);
				}
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	
	
	
	
	
	@Override
	public final void traverseReferences(
		final XReplacingBag<Object> instance        ,
		final TraversalMutator      mutator         ,
		final TraversalEnqueuer     enqueuer        ,
		final MutationListener      mutationListener
	)
	{
		try
		{
			instance.substitute(current ->
			{
				final Object returned = mutator.mutateReference(current, instance, enqueuer);

				if(mutationListener != null)
				{
					mutationListener.registerChange(instance, current, returned);
				}
				
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

	@Override
	public final void traverseReferences(
		final XReplacingBag<Object> instance,
		final TraversalAcceptor     acceptor,
		final TraversalEnqueuer     enqueuer
	)
	{
		try
		{
			instance.iterate(current ->
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
