package net.jadoth.traversal;

import net.jadoth.collections.types.XSet;

public final class ReferenceHandlerMutating extends AbstractReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final TraversalMutator  traversalMutator ;
	private final MutationListener  mutationListener ;
	
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	ReferenceHandlerMutating(
		final TypeTraverserProvider  traverserProvider,
		final XSet<Object>           alreadyHandled   ,
		final TraversalPredicateSkip predicateSkip    ,
		final TraversalPredicateNode predicateNode    ,
		final TraversalPredicateLeaf predicateLeaf    ,
		final TraversalPredicateFull predicateFull    ,
		final TraversalMutator       traversalMutator ,
		final MutationListener       mutationListener
		
	)
	{
		super(traverserProvider, alreadyHandled, predicateSkip, predicateNode, predicateLeaf, predicateFull);
		this.traversalMutator = traversalMutator;
		this.mutationListener = mutationListener;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	final <T> void handleFull(final T instance, final TypeTraverser<T> traverser)
	{
		traverser.traverseReferences(instance, this, this.traversalMutator, this.mutationListener);
	}
	
	@Override
	final <T> void handleLeaf(final T instance, final TypeTraverser<T> traverser)
	{
		traverser.traverseReferences(instance, this.traversalMutator, this.mutationListener);
	}
	
}
