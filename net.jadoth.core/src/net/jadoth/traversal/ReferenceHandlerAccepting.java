package net.jadoth.traversal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;
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
		final TypeTraverserProvider      traverserProvider      ,
		final XSet<Object>               alreadyHandled         ,
		final XGettingSet<Class<?>>      skippedTypes           ,
		final XGettingSequence<Class<?>> skippedTypesPolymorphic,
		final TraversalAcceptor          traversalAcceptor
	)
	{
		super(traverserProvider, alreadyHandled, skippedTypes, skippedTypesPolymorphic);
		this.traversalAcceptor = traversalAcceptor;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
											
	@Override
	final <T> void handle(final T instance)
	{
		this.handle(instance, this.traversalAcceptor);
	}
}
