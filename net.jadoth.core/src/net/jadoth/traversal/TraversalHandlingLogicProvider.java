package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingCollection;

public interface TraversalHandlingLogicProvider
{
	public <T> Predicate<? super T> provideHandlingLogic(Class<? extends T> type);



	public static TraversalHandlingLogicProvider New(final Predicate<Object> logic)
	{
		return new SimplePredicateProvider(logic);
	}
	
	public static TraversalHandlingLogicProvider New(
		final XGettingCollection<Class<?>> excludedTypes,
		final Predicate<Object>            logic
	)
	{
		// specialcasing for performance reasons
		return excludedTypes == null || excludedTypes.isEmpty()
			? new SimplePredicateProvider(logic)
			: new TypeExcludingPredicateProvider(
				excludedTypes.immure(),
				logic
			)
		;
	}

	public final class SimplePredicateProvider implements TraversalHandlingLogicProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Predicate<Object> logic;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		SimplePredicateProvider(final Predicate<Object> logic)
		{
			super();
			this.logic = logic;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		
		@Override
		public <T> Predicate<? super T> provideHandlingLogic(final Class<? extends T> type)
		{
			return this.logic;
		}
		
	}
	
	public final class TypeExcludingPredicateProvider implements TraversalHandlingLogicProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingCollection<Class<?>> excludedTypes;
		private final Predicate<Object>            logic        ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		TypeExcludingPredicateProvider(final XGettingCollection<Class<?>> excludedTypes, final Predicate<Object> logic)
		{
			super();
			this.excludedTypes = excludedTypes;
			this.logic         = logic        ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public <T> Predicate<? super T> provideHandlingLogic(final Class<? extends T> type)
		{
			// excludedTypes are checked as both concrete types and polymorphic super types
			if(this.excludedTypes.contains(type))
			{
				return null;
			}
			
			// cannot use a simple contains() for polymorphic check ("instance of" on type-level)
			for(final Class<?> excludedPolymorphicType : this.excludedTypes)
			{
				if(excludedPolymorphicType.isAssignableFrom(type))
				{
					return null;
				}
			}
			
			// viable type, return logic
			return this.logic;
		}
		
	}

}
