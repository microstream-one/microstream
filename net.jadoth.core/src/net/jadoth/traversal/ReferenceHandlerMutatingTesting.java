package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XSet;

public final class ReferenceHandlerMutatingTesting extends AbstractReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Predicate<Object> handlingPredicate;
	private final TraversalMutator  traversalMutator ;
	private final MutationListener  mutationListener ;
	
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	ReferenceHandlerMutatingTesting(
		final XSet<Object>          alreadyHandled   ,
		final TypeTraverserProvider traverserProvider,
		final Predicate<Object>     handlingPredicate,
		final TraversalMutator      traversalMutator ,
		final MutationListener      mutationListener
		
	)
	{
		super(traverserProvider, alreadyHandled);
		this.handlingPredicate = handlingPredicate;
		this.traversalMutator  = traversalMutator ;
		this.mutationListener  = mutationListener ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	final <T> void handle(final T instance)
	{
		if(!this.handlingPredicate.test(instance))
		{
			return;
		}
		
		this.handle(instance, this.traversalMutator, this.mutationListener);
	}
	
}
