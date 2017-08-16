package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XSet;

public final class ReferenceHandlerAcceptingMutating extends AbstractReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final TraversalAcceptor traversalAcceptor;
	private final TraversalMutator  traversalMutator ;
	private final MutationListener  mutationListener ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	ReferenceHandlerAcceptingMutating(
		final TypeTraverserProvider traverserProvider,
		final XSet<Object>          alreadyHandled   ,
		final Predicate<Object>     isHandleable     ,
		final Predicate<Object>     isNode           ,
		final Predicate<Object>     isFull           ,
		final TraversalAcceptor     traversalAcceptor,
		final TraversalMutator      traversalMutator ,
		final MutationListener      mutationListener
		
	)
	{
		super(traverserProvider, alreadyHandled, isHandleable, isNode, isFull);
		this.traversalAcceptor = traversalAcceptor;
		this.traversalMutator  = traversalMutator ;
		this.mutationListener  = mutationListener ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	final <T> void handle(final T instance, final TypeTraverser<T> traverser)
	{
		traverser.traverseReferences(instance, this, this.traversalAcceptor, this.traversalMutator, this.mutationListener);
	}
	
}
