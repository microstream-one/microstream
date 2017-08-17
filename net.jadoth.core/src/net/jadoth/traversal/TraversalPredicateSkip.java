package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;

@FunctionalInterface
public interface TraversalPredicateSkip extends TraversalPredicate
{
	public boolean skip(Object instance);
	
	
	
	public static TraversalPredicateSkip New(
		final Predicate<Object>          customPredicate ,
		final XGettingSet<Class<?>>      positiveTypes   ,
		final XGettingSequence<Class<?>> typesPolymorphic
	)
	{
		return new TraversalPredicateSkip.Implementation(
			customPredicate ,
			positiveTypes   ,
			typesPolymorphic
		);
	}
	
	public final class Implementation extends AbstractHandlingPredicate implements TraversalPredicateSkip
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
			final Predicate<Object>          customPredicate  ,
			final XGettingSet<Class<?>>      positiveTypes    ,
			final XGettingSequence<Class<?>> typesPolymorphic
		)
		{
			super(customPredicate, positiveTypes, typesPolymorphic);
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean skip(final Object instance)
		{
			return this.test(instance);
		}
		
	}
	
}
