package net.jadoth.swizzling.types;

import net.jadoth.traversal.AbstractTraversalSkipSignal;
import net.jadoth.traversal.TraversalAcceptor;
import net.jadoth.traversal.TraversalEnqueuer;
import net.jadoth.traversal.TraversalMutator;
import net.jadoth.traversal.TypeTraverser;

public final class TraverserLazy implements TypeTraverser<Lazy<?>>
{
	@Override
	public final void traverseReferences(
		final Lazy<?>           instance,
		final TraversalAcceptor acceptor,
		final TraversalEnqueuer enqueuer
	)
	{
		final Lazy<?> lazy = instance;
		final boolean wasNull = lazy.peek() == null;
		
		try
		{
			final Object current;
			 acceptor.acceptReference(current = lazy.get(), lazy, enqueuer);
				
			// note: if the current (now prior) value has to be enqueued, the acceptor can do that internally
			enqueuer.enqueue(current);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		finally
		{
			if(wasNull)
			{
				lazy.clear();
			}
		}
	}
	
	@Override
	public void traverseReferences(final Lazy<?> instance, final TraversalMutator mutator, final TraversalEnqueuer enqueuer)
	{
		final Lazy<?> lazy = instance;
		final boolean wasNull = lazy.peek() == null;
		
		try
		{
			final Object current, returned;
			if((returned = mutator.mutateReference(current = lazy.get(), lazy, enqueuer)) != current)
			{
				// (30.06.2017 TM)FIXME: problem: Lazy references are effectively final
				throw new UnsupportedOperationException();
			}
				
			// note: if the current (now prior) value has to be enqueued, the acceptor can do that internally
			enqueuer.enqueue(returned);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		finally
		{
			if(wasNull)
			{
				lazy.clear();
			}
		}
	}
	
}
