package net.jadoth.swizzling.types;

import net.jadoth.traversal2.AbstractTraversalSkipSignal;
import net.jadoth.traversal2.TraversalAcceptor;
import net.jadoth.traversal2.TraversalEnqueuer;
import net.jadoth.traversal2.TraverserAccepting;

public final class TraverserLazy implements TraverserAccepting
{
	@Override
	public final void traverseReferences(
		final Object            instance,
		final TraversalAcceptor acceptor,
		final TraversalEnqueuer enqueuer
	)
	{
		final Lazy<?> lazy = (Lazy<?>)instance;
		final boolean wasNull = lazy.peek() == null;
		
		try
		{
			final Object current, returned;
			if((returned = acceptor.acceptInstance(current = lazy.get(), lazy, enqueuer)) != current)
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
