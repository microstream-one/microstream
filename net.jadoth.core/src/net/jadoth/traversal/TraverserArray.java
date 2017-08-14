package net.jadoth.traversal;

public final class TraverserArray implements TypeTraverser<Object[]>
{
	@Override
	public final void traverseReferences(
		final Object[]          instance,
		final TraversalEnqueuer enqueuer,
		final TraversalAcceptor acceptor
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
		final Object[]          instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
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
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final Object[]          instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
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
	
}
