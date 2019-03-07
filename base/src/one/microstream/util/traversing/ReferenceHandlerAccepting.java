package one.microstream.util.traversing;

import java.util.function.Predicate;

import one.microstream.collections.types.XSet;

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
		final Predicate<Object>      predicateHandle  ,
		final TraversalAcceptor      traversalAcceptor
	)
	{
		super(traverserProvider, alreadyHandled, predicateSkip, predicateNode, predicateLeaf, predicateFull, predicateHandle);
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
