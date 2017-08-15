package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;
import net.jadoth.collections.types.XSet;

public final class ReferenceHandlerAcceptingMutatingTesting extends AbstractReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Predicate<Object> handlingPredicate;
	private final TraversalAcceptor traversalAcceptor;
	private final TraversalMutator  traversalMutator ;
	private final MutationListener  mutationListener ;
	
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	ReferenceHandlerAcceptingMutatingTesting(
		final TypeTraverserProvider      traverserProvider      ,
		final XSet<Object>               alreadyHandled         ,
		final XGettingSet<Class<?>>      skippedTypes           ,
		final XGettingSequence<Class<?>> skippedTypesPolymorphic,
		final Predicate<Object>          handlingPredicate      ,
		final TraversalAcceptor          traversalAcceptor      ,
		final TraversalMutator           traversalMutator       ,
		final MutationListener           mutationListener
		
	)
	{
		super(traverserProvider, alreadyHandled, skippedTypes, skippedTypesPolymorphic);
		this.handlingPredicate = handlingPredicate;
		this.traversalAcceptor = traversalAcceptor;
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
		
		this.handle(instance, this.traversalAcceptor, this.traversalMutator, this.mutationListener);
	}
	
}
