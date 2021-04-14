package one.microstream.util.traversing;

import java.util.function.Predicate;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingSet;

@FunctionalInterface
public interface TraversalPredicateLeaf extends TraversalPredicate
{
	public boolean isLeaf(Object instance);

	
	
	public static TraversalPredicateLeaf New(
		final XGettingSet<Object>        explicitInstances,
		final Predicate<Object>          customPredicate  ,
		final XGettingSet<Class<?>>      positiveTypes    ,
		final XGettingSequence<Class<?>> typesPolymorphic
	)
	{
		return new TraversalPredicateLeaf.Default(
			explicitInstances,
			customPredicate  ,
			positiveTypes    ,
			typesPolymorphic
		);
	}
	
	public final class Default extends AbstractHandlingPredicate implements TraversalPredicateLeaf
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XGettingSet<Object> explicitInstances;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
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
		public final boolean isLeaf(final Object instance)
		{
			if(this.explicitInstances != null && this.explicitInstances.contains(instance))
			{
				return true;
			}
			return this.test(instance);
		}
		
	}
	
}
