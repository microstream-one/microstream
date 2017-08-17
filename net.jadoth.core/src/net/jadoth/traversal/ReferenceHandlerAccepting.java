package net.jadoth.traversal;

import net.jadoth.collections.types.XSet;

public final class ReferenceHandlerAccepting extends AbstractReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final TraversalAcceptor traversalAcceptor;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	ReferenceHandlerAccepting(
		final TypeTraverserProvider  traverserProvider,
		final XSet<Object>           alreadyHandled   ,
		final TraversalPredicateSkip predicateSkip    ,
		final TraversalPredicateNode predicateNode    ,
		final TraversalPredicateLeaf predicateLeaf    ,
		final TraversalPredicateFull predicateFull    ,
		final TraversalAcceptor      traversalAcceptor
	)
	{
		super(traverserProvider, alreadyHandled, predicateSkip, predicateNode, predicateLeaf, predicateFull);
		this.traversalAcceptor = traversalAcceptor;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
											
	@Override
	final <T> void handleFull(final T instance, final TypeTraverser<T> traverser)
	{
		traverser.traverseReferences(instance, this, this.traversalAcceptor);
	}
	
	@Override
	final <T> void handleLeaf(final T instance, final TypeTraverser<T> traverser)
	{
		traverser.traverseReferences(instance, this.traversalAcceptor);
	}
	
}
