package net.jadoth.traversal2;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface TraversalAcceptor
{
	// (30.06.2017 TM)TODO: add type parameter to ease custom implementations?
	
	public Object acceptInstance(Object instance, Object parent, TraversalEnqueuer enqueuer);
	
	
	public static TraversalAcceptor Wrap(final Consumer<Object> logic)
	{
		return new TraversalAcceptor.ImplementationConsumer(logic);
	}
	
	public static TraversalAcceptor Wrap(final Function<Object, Object> logic)
	{
		return new TraversalAcceptor.ImplementationFunction(logic);
	}
	
	public static TraversalAcceptor Wrap(final Predicate<Object> condition, final Consumer<Object> logic)
	{
		return new TraversalAcceptor.ImplementationConditionalConsumer(condition, logic);
	}
	
	public static TraversalAcceptor Wrap(final Predicate<Object> condition, final Function<Object, Object> logic)
	{
		return new TraversalAcceptor.ImplementationConditionalFunction(condition, logic);
	}
	
	public final class ImplementationConsumer implements TraversalAcceptor
	{
		private final Consumer<Object> logic;

		ImplementationConsumer(final Consumer<Object> logic)
		{
			super();
			this.logic = logic;
		}

		@Override
		public final Object acceptInstance(final Object instance, final Object parent, final TraversalEnqueuer enqueuer)
		{
			this.logic.accept(instance);
			return instance;
		}
		
	}
	
	public final class ImplementationFunction implements TraversalAcceptor
	{
		private final Function<Object, Object> logic;

		ImplementationFunction(final Function<Object, Object> logic)
		{
			super();
			this.logic = logic;
		}

		@Override
		public final Object acceptInstance(final Object instance, final Object parent, final TraversalEnqueuer enqueuer)
		{
			return this.logic.apply(instance);
		}
		
	}
	
	public final class ImplementationConditionalConsumer implements TraversalAcceptor
	{
		private final Predicate<Object> condition;
		private final Consumer<Object>  logic    ;

		ImplementationConditionalConsumer(final Predicate<Object> condition, final Consumer<Object> logic)
		{
			super();
			this.condition = condition;
			this.logic     = logic    ;
		}

		@Override
		public final Object acceptInstance(final Object instance, final Object parent, final TraversalEnqueuer enqueuer)
		{
			if(this.condition.test(instance))
			{
				this.logic.accept(instance);
			}
			return instance;
		}
		
	}
	
	public final class ImplementationConditionalFunction implements TraversalAcceptor
	{
		private final Predicate<Object>        condition;
		private final Function<Object, Object> logic    ;

		ImplementationConditionalFunction(final Predicate<Object> condition, final Function<Object, Object> logic)
		{
			super();
			this.condition = condition;
			this.logic     = logic    ;
		}

		@Override
		public final Object acceptInstance(final Object instance, final Object parent, final TraversalEnqueuer enqueuer)
		{
			if(this.condition.test(instance))
			{
				return this.logic.apply(instance);
			}
			return instance;
		}
		
	}
	
}
