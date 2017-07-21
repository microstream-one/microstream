package net.jadoth.traversal;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface TraversalAcceptor extends TraversalHandler
{
	
	public void acceptReference(Object instance, Object parent, TraversalEnqueuer enqueuer);
	
	
	public static TraversalAcceptor New(final Consumer<Object> logic)
	{
		return new TraversalAcceptor.Implementation(logic);
	}
	
	public static TraversalAcceptor New(final Predicate<Object> condition, final Consumer<Object> logic)
	{
		return new TraversalAcceptor.ImplementationConditional(condition, logic);
	}
		
	public final class Implementation implements TraversalAcceptor
	{
		private final Consumer<Object> logic;

		Implementation(final Consumer<Object> logic)
		{
			super();
			this.logic = logic;
		}

		@Override
		public final void acceptReference(final Object instance, final Object parent, final TraversalEnqueuer enqueuer)
		{
			this.logic.accept(instance);
		}
		
	}
	
	public final class ImplementationConditional implements TraversalAcceptor
	{
		private final Predicate<Object> condition;
		private final Consumer<Object>  logic    ;

		ImplementationConditional(final Predicate<Object> condition, final Consumer<Object> logic)
		{
			super();
			this.condition = condition;
			this.logic     = logic    ;
		}

		@Override
		public final void acceptReference(final Object instance, final Object parent, final TraversalEnqueuer enqueuer)
		{
			if(!this.condition.test(instance))
			{
				return;
			}
			this.logic.accept(instance);
		}
		
	}
		
}
