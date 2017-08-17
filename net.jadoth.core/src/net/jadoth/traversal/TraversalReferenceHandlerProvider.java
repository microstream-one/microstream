package net.jadoth.traversal;

import net.jadoth.collections.types.XSet;

public interface TraversalReferenceHandlerProvider
{
	public AbstractReferenceHandler provideReferenceHandler(
		final XSet<Object>           alreadyHandled   ,
		final TypeTraverserProvider  traverserProvider,
		final TraversalPredicateSkip predicateSkip    ,
		final TraversalPredicateNode predicateNode    ,
		final TraversalPredicateLeaf predicateLeaf    ,
		final TraversalPredicateFull predicateFull    ,
		final TraversalAcceptor      traversalAcceptor,
		final TraversalMutator       traversalMutator ,
		final MutationListener       mutationListener
	);
	
	
	
	public static TraversalReferenceHandlerProvider New()
	{
		return new TraversalReferenceHandlerProvider.Implementation();
	}
	
	public class Implementation implements TraversalReferenceHandlerProvider
	{
		@Override
		public AbstractReferenceHandler provideReferenceHandler(
			final XSet<Object>           alreadyHandled   ,
			final TypeTraverserProvider  traverserProvider,
			final TraversalPredicateSkip predicateSkip    ,
			final TraversalPredicateNode predicateNode    ,
			final TraversalPredicateLeaf predicateLeaf    ,
			final TraversalPredicateFull predicateFull    ,
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
				effectiveAcceptor
			);
		}
		
	}
}
