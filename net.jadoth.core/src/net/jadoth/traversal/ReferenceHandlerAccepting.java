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
		final XSet<Object>          alreadyHandled   ,
		final TypeTraverserProvider traverserProvider,
		final TraversalAcceptor     traversalAcceptor
	)
	{
		super(traverserProvider, alreadyHandled);
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
