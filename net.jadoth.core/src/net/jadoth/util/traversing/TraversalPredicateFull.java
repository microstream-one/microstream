package net.jadoth.util.traversing;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;

@FunctionalInterface
public interface TraversalPredicateFull extends TraversalPredicate
{
	public boolean isFull(Object instance);
	
	
	
	public static TraversalPredicateFull New(
		final XGettingSet<Object>        explicitInstances,
		final Predicate<Object>          customPredicate  ,
		final XGettingSet<Class<?>>      positiveTypes    ,
		final XGettingSequence<Class<?>> typesPolymorphic
	)
	{
		return new TraversalPredicateFull.Implementation(
			explicitInstances,
			customPredicate  ,
			positiveTypes    ,
			typesPolymorphic
		);
	}
	
	public final class Implementation extends AbstractHandlingPredicate implements TraversalPredicateFull
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XGettingSet<Object> explicitInstances;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
			final XGettingSet<Object>        explicitInstances,
			final Predicate<Object>          customPredicate  ,
			final XGettingSet<Class<?>>      positiveTypes    ,
			final XGettingSequence<Class<?>> typesPolymorphic
		)
		{
			super(customPredicate, positiveTypes, typesPolymorphic);
			this.explicitInstances = explicitInstances;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean isFull(final Object instance)
		{
			if(this.explicitInstances != null && this.explicitInstances.contains(instance))
			{
				return true;
			}
			return this.test(instance);
		}
		
	}
	
}
