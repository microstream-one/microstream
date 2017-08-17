package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;

@FunctionalInterface
public interface TraversalPredicateNode
{
	public boolean isNode(Object instance);

	

	public static TraversalPredicateNode New(
		final XGettingSet<Object>        explicitInstances,
		final Predicate<Object>          customPredicate  ,
		final XGettingSet<Class<?>>      positiveTypes    ,
		final XGettingSequence<Class<?>> typesPolymorphic
	)
	{
		return new TraversalPredicateNode.Implementation(
			explicitInstances,
			customPredicate  ,
			positiveTypes    ,
			typesPolymorphic
		);
	}
	
	public final class Implementation extends AbstractHandlingPredicate implements TraversalPredicateNode
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
		public final boolean isNode(final Object instance)
		{
			if(this.explicitInstances != null && this.explicitInstances.contains(instance))
			{
				return true;
			}
			return this.test(instance);
		}
		
	}
	
}
