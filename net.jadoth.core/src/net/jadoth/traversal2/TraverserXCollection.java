package net.jadoth.traversal2;

import java.util.function.Function;

import net.jadoth.collections.types.XReplacingBag;

public final class TraverserXCollection implements TraversalHandler
{
	@Override
	public void traverseReferences(final Object instance, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
	{
		@SuppressWarnings("unchecked")
		final XReplacingBag<Object> collection = (XReplacingBag<Object>)instance;
		
		collection.substitute(new IterationWrapper(collection, acceptor, enqueuer));
	}
	
	public static class IterationWrapper implements Function<Object, Object>, ReferenceAccessor
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XReplacingBag<Object> parent  ;
		final TraversalAcceptor     acceptor;
		final TraversalEnqueuer     enqueuer;
		
		Object current;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		IterationWrapper(final XReplacingBag<Object> parent, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
		{
			super();
			this.parent   = parent  ;
			this.acceptor = acceptor;
			this.enqueuer = enqueuer;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void set(final Object parent, final Object newValue)
		{
			this.current = newValue;
		}
		
		@Override
		public Object get(final Object parent)
		{
			return this.current;
		}
		
		@Override
		public Object apply(final Object t)
		{
			this.acceptor.acceptInstance(this.current = t, this.parent, this, this.enqueuer);
			return this.current;
		}
		
	}

}
