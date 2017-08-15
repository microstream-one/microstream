package net.jadoth.traversal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;
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
		final TypeTraverserProvider      traverserProvider      ,
		final XSet<Object>               alreadyHandled         ,
		final XGettingSet<Class<?>>      skippedTypes           ,
		final XGettingSequence<Class<?>> skippedTypesPolymorphic,
		final TraversalMutator           traversalMutator       ,
		final MutationListener           mutationListener
		
	)
	{
		super(traverserProvider, alreadyHandled, skippedTypes, skippedTypesPolymorphic);
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
