package one.microstream.util.traversing;

import java.util.function.Predicate;

import one.microstream.collections.types.XSet;

public interface TraversalReferenceHandlerProvider
{
	public AbstractReferenceHandler provideReferenceHandler(
		final XSet<Object>           alreadyHandled   ,
		final TypeTraverserProvider  traverserProvider,
		final TraversalPredicateSkip predicateSkip    ,
		final TraversalPredicateNode predicateNode    ,
		final TraversalPredicateLeaf predicateLeaf    ,
		final TraversalPredicateFull predicateFull    ,
		final Predicate<Object>      predicateHandle  ,
		final TraversalAcceptor      traversalAcceptor,
		final TraversalMutator       traversalMutator ,
		final MutationListener       mutationListener
	);
	
	
	
	public static TraversalReferenceHandlerProvider New()
	{
		return new TraversalReferenceHandlerProvider.Default();
	}
	
	public class Default implements TraversalReferenceHandlerProvider
	{
		@Override
		public AbstractReferenceHandler provideReferenceHandler(
			final XSet<Object>           alreadyHandled   ,
			final TypeTraverserProvider  traverserProvider,
			final TraversalPredicateSkip predicateSkip    ,
			final TraversalPredicateNode predicateNode    ,
			final TraversalPredicateLeaf predicateLeaf    ,
			final TraversalPredicateFull predicateFull    ,
			final Predicate<Object>      predicateHandle  ,
			final TraversalAcceptor      traversalAcceptor,
			final TraversalMutator       traversalMutator ,
			final MutationListener       mutationListener
		)
		{
			if(traversalMutator != null)
			{
				return traversalAcceptor != null
					? new ReferenceHandlerAcceptingMutating(
						traverserProvider,
						alreadyHandled   ,
						predicateSkip    ,
						predicateNode    ,
						predicateLeaf    ,
						predicateFull    ,
						predicateHandle  ,
						traversalAcceptor,
						traversalMutator ,
						mutationListener
					)
					: new ReferenceHandlerMutating(
						traverserProvider,
						alreadyHandled   ,
						predicateSkip    ,
						predicateNode    ,
						predicateLeaf    ,
						predicateFull    ,
						predicateHandle  ,
						traversalMutator ,
						mutationListener
					)
				;
			}
			
			final TraversalAcceptor effectiveAcceptor = traversalAcceptor != null
				? traversalAcceptor
				: (i, p) ->
					true
			;
					
			return new ReferenceHandlerAccepting(
				traverserProvider,
				alreadyHandled   ,
				predicateSkip    ,
				predicateNode    ,
				predicateLeaf    ,
				predicateFull    ,
				predicateHandle  ,
				effectiveAcceptor
			);
		}
		
	}
}
