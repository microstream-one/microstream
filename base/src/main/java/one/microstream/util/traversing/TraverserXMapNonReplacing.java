package one.microstream.util.traversing;

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingMap;


public final class TraverserXMapNonReplacing implements TypeTraverser<XGettingMap<Object, Object>>
{
	@Override
	public final void traverseReferences(
		final XGettingMap<Object, Object> instance,
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
		final XGettingMap<Object, Object> instance,
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
		final XGettingMap<Object, Object> instance        ,
		final TraversalEnqueuer    enqueuer        ,
		final TraversalMutator     mutator         ,
		final MutationListener     mutationListener
	)
	{
		final Consumer<Object> mapper = current ->
		{
			enqueuer.enqueue(current);
			if(mutator.mutateReference(current, instance) != current)
			{
				throw new UnsupportedOperationException();
			}
		};
		
		try
		{
			instance.keys().iterate(mapper);
			instance.values().iterate(mapper);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XGettingMap<Object, Object> instance        ,
		final TraversalEnqueuer    enqueuer        ,
		final TraversalAcceptor    acceptor        ,
		final TraversalMutator     mutator         ,
		final MutationListener     mutationListener
	)
	{
		final Consumer<Object> mapper = current ->
		{
			if(acceptor.acceptReference(current, instance))
			{
				enqueuer.enqueue(current);
			}
			if(mutator.mutateReference(current, instance) != current)
			{
				throw new UnsupportedOperationException();
			}
		};
		
		try
		{
			instance.keys().iterate(mapper);
			instance.values().iterate(mapper);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}

	
	@Override
	public final void traverseReferences(
		final XGettingMap<Object, Object> instance,
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
		final XGettingMap<Object, Object> instance        ,
		final TraversalMutator     mutator         ,
		final MutationListener     mutationListener
	)
	{
		final Consumer<Object> mapper = current ->
		{
			if(mutator.mutateReference(current, instance) != current)
			{
				throw new UnsupportedOperationException();
			}
		};
		
		try
		{
			instance.keys().iterate(mapper);
			instance.values().iterate(mapper);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XGettingMap<Object, Object> instance        ,
		final TraversalAcceptor    acceptor        ,
		final TraversalMutator     mutator         ,
		final MutationListener     mutationListener
	)
	{
		final Consumer<Object> mapper = current ->
		{
			acceptor.acceptReference(current, instance);
			if(mutator.mutateReference(current, instance) != current)
			{
				throw new UnsupportedOperationException();
			}
		};
		try
		{
			instance.keys().iterate(mapper);
			instance.values().iterate(mapper);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
}
