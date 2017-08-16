package net.jadoth.traversal;

import java.util.function.Predicate;

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
		final TypeTraverserProvider traverserProvider,
		final XSet<Object>          alreadyHandled   ,
		final Predicate<Object>     isHandleable     ,
		final Predicate<Object>     isNode           ,
		final Predicate<Object>     isFull           ,
		final TraversalMutator      traversalMutator ,
		final MutationListener      mutationListener
		
	)
	{
		super(traverserProvider, alreadyHandled, isHandleable, isNode, isFull);
		this.traversalMutator  = traversalMutator ;
		this.mutationListener  = mutationListener ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	final <T> void handle(final T instance, final TypeTraverser<T> traverser)
	{
		traverser.traverseReferences(instance, this, this.traversalMutator, this.mutationListener);
	}
	
}
