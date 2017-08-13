package net.jadoth.traversal;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface TraversalAcceptor extends TraversalHandler
{
	
	public boolean acceptReference(Object instance, Object parent);
	
	
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
		public final boolean acceptReference(final Object instance, final Object parent)
		{
			this.logic.accept(instance);
			return true;
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
		public final boolean acceptReference(final Object instance, final Object parent)
		{
			if(this.condition.test(instance))
			{
				this.logic.accept(instance);
			}
			return true;
		}
		
	}
		
}
