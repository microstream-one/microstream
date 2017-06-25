package net.jadoth.traversal;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingSet;

/**
 * "There is no problem that cannot be solved by one more level of indirection."
 *
 * @author TM
 */
@FunctionalInterface
public interface TraversalHandlingLogicProviderProvider
{
	public TraversalHandlingLogicProvider provideHandlingLogicProvider(XGettingSet<Class<?>> excludedTypes);
	
	
	
	
	public static TraversalHandlingLogicProviderProvider New(final Predicate<Object> logic)
	{
		return new TraversalHandlingLogicProviderProvider.Implementation(
			notNull(logic)
		);
	}
	
	public final class Implementation implements TraversalHandlingLogicProviderProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Predicate<Object> logic;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final Predicate<Object> logic)
		{
			super();
			this.logic = logic;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public TraversalHandlingLogicProvider provideHandlingLogicProvider(final XGettingSet<Class<?>> excludedTypes)
		{
			return TraversalHandlingLogicProvider.New(excludedTypes, this.logic);
		}
		
	}

}
