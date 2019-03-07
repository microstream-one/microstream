package one.microstream.util.traversing;

import java.util.function.Function;

import one.microstream.collections.types.XMap;


// (18.08.2017 TM)FIXME: TraverserXMapMutable and required collection interface enhancements
public final class TraverserXMapReplacing implements TypeTraverser<XMap<Object, Object>>
{
	@Override
	public final void traverseReferences(
		final XMap<Object, Object> instance,
		final TraversalEnqueuer    enqueuer
	)
	{
		instance.iterate(e ->
		{
			enqueuer.enqueue(e.key());
			enqueuer.enqueue(e.value());
		});
	}
	
	@Override
	public final void traverseReferences(
		final XMap<Object, Object> instance,
		final TraversalEnqueuer    enqueuer,
		final TraversalAcceptor    acceptor
	)
	{
		try
		{
			instance.iterate(e ->
			{
				if(acceptor.acceptReference(e.key(), instance))
				{
					enqueuer.enqueue(e.key());
				}
				if(acceptor.acceptReference(e.value(), instance))
				{
					enqueuer.enqueue(e.value());
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
		final XMap<Object, Object> instance        ,
		final TraversalEnqueuer    enqueuer        ,
		final TraversalMutator     mutator         ,
		final MutationListener     mutationListener
	)
	{
		final Function<Object, Object> mapper = current ->
		{
			final Object returned;
			enqueuer.enqueue(current);
			if((returned = mutator.mutateReference(current, instance)) != current)
			{
				if(mutationListener != null)
				{
					if(mutationListener.registerChange(instance, current, returned))
					{
						enqueuer.enqueue(returned);
					}
				}
			}
			return returned;
		};
		
		try
		{
			instance.keys().substitute(mapper);
			instance.values().substitute(mapper);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XMap<Object, Object> instance        ,
		final TraversalEnqueuer    enqueuer        ,
		final TraversalAcceptor    acceptor        ,
		final TraversalMutator     mutator         ,
		final MutationListener     mutationListener
	)
	{
		final Function<Object, Object> mapper = current ->
		{
			final Object returned;
			if(acceptor.acceptReference(current, instance))
			{
				enqueuer.enqueue(current);
			}
			if((returned = mutator.mutateReference(current, instance)) != current)
			{
				if(mutationListener != null)
				{
					if(mutationListener.registerChange(instance, current, returned))
					{
						enqueuer.enqueue(returned);
					}
				}
			}
			return returned;
		};
		
		try
		{
			instance.keys().substitute(mapper);
			instance.values().substitute(mapper);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}

	
	@Override
	public final void traverseReferences(
		final XMap<Object, Object> instance,
		final TraversalAcceptor    acceptor
	)
	{
		try
		{
			instance.iterate(e ->
			{
				acceptor.acceptReference(e.key(), instance);
				acceptor.acceptReference(e.value(), instance);
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XMap<Object, Object> instance        ,
		final TraversalMutator     mutator         ,
		final MutationListener     mutationListener
	)
	{
		final Function<Object, Object> mapper = current ->
		{
			final Object returned;
			if((returned = mutator.mutateReference(current, instance)) != current)
			{
				if(mutationListener != null)
				{
					mutationListener.registerChange(instance, current, returned);
				}
			}
			return returned;
		};
		
		try
		{
			instance.keys().substitute(mapper);
			instance.values().substitute(mapper);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XMap<Object, Object> instance        ,
		final TraversalAcceptor    acceptor        ,
		final TraversalMutator     mutator         ,
		final MutationListener     mutationListener
	)
	{
		final Function<Object, Object> mapper = current ->
		{
			final Object returned;
			acceptor.acceptReference(current, instance);
			if((returned = mutator.mutateReference(current, instance)) != current)
			{
				if(mutationListener != null)
				{
					mutationListener.registerChange(instance, current, returned);
				}
			}
			return returned;
		};
		try
		{
			instance.keys().substitute(mapper);
			instance.values().substitute(mapper);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
}
