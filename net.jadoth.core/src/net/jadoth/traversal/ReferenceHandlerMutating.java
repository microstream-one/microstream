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
		final XSet<Object>          alreadyHandled   ,
		final TypeTraverserProvider traverserProvider,
		final TraversalMutator      traversalMutator ,
		final MutationListener      mutationListener
		
	)
	{
		super(traverserProvider, alreadyHandled);
		this.traversalMutator  = traversalMutator ;
		this.mutationListener  = mutationListener ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	final <T> void handle(final T instance)
	{
		this.handle(instance, this.traversalMutator, this.mutationListener);
	}
	
}
