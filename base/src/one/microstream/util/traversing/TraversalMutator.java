package one.microstream.util.traversing;

import java.util.function.Function;
import java.util.function.Predicate;

public interface TraversalMutator extends TraversalHandler
{
	public Object mutateReference(Object instance, Object parent);
	
		
	public static TraversalMutator New(final Function<Object, ?> logic)
	{
		return new TraversalMutator.Implementation(logic);
	}
		
	public static TraversalMutator New(final Predicate<Object> condition, final Function<Object, ?> logic)
	{
		return new TraversalMutator.ImplementationConditional(condition, logic);
	}
		
	public final class Implementation implements TraversalMutator
	{
		private final Function<Object, ?> logic;

		Implementation(final Function<Object, ?> logic)
		{
			super();
			this.logic = logic;
		}

		@Override
		public final Object mutateReference(final Object instance, final Object parent)
		{
			return this.logic.apply(instance);
		}
		
	}

	public final class ImplementationConditional implements TraversalMutator
	{
		private final Predicate<Object>   condition;
		private final Function<Object, ?> logic    ;

		ImplementationConditional(final Predicate<Object> condition, final Function<Object, ?> logic)
		{
			super();
			this.condition = condition;
			this.logic     = logic    ;
		}

		@Override
		public final Object mutateReference(final Object instance, final Object parent)
		{
			if(this.condition.test(instance))
			{
				return this.logic.apply(instance);
			}
			return instance;
		}
		
	}
	
}
