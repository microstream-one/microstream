package one.microstream.util.traversing;

import java.util.function.Predicate;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingSet;

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
		return new TraversalPredicateSkip.Default(
			customPredicate ,
			positiveTypes   ,
			typesPolymorphic
		);
	}
	
	public final class Default extends AbstractHandlingPredicate implements TraversalPredicateSkip
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
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
