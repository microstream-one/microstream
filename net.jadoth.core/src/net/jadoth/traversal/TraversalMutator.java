package net.jadoth.traversal;

import java.util.function.Function;
import java.util.function.Predicate;

public interface TraversalMutator
{
	public Object mutateInstance(Object instance, Object parent, TraversalEnqueuer enqueuer);
	
		
	public static TraversalMutator Wrap(final Function<Object, Object> logic)
	{
		return new TraversalMutator.Implementation(logic);
	}
		
	public static TraversalMutator Wrap(final Predicate<Object> condition, final Function<Object, Object> logic)
	{
		return new TraversalMutator.ImplementationConditional(condition, logic);
	}
		
	public final class Implementation implements TraversalMutator
	{
		private final Function<Object, Object> logic;

		Implementation(final Function<Object, Object> logic)
		{
			super();
			this.logic = logic;
		}

		@Override
		public final Object mutateInstance(final Object instance, final Object parent, final TraversalEnqueuer enqueuer)
		{
			return this.logic.apply(instance);
		}
		
	}

	public final class ImplementationConditional implements TraversalMutator
	{
		private final Predicate<Object>        condition;
		private final Function<Object, Object> logic    ;

		ImplementationConditional(final Predicate<Object> condition, final Function<Object, Object> logic)
		{
			super();
			this.condition = condition;
			this.logic     = logic    ;
		}

		@Override
		public final Object mutateInstance(final Object instance, final Object parent, final TraversalEnqueuer enqueuer)
		{
			if(this.condition.test(instance))
			{
				return this.logic.apply(instance);
			}
			return instance;
		}
		
	}
	
}
