package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XSet;

public final class ReferenceHandlerAcceptingTesting extends AbstractReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Predicate<Object> handlingPredicate;
	private final TraversalAcceptor traversalAcceptor;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	ReferenceHandlerAcceptingTesting(
		final XSet<Object>          alreadyHandled   ,
		final TypeTraverserProvider traverserProvider,
		final Predicate<Object>     handlingPredicate,
		final TraversalAcceptor     traversalAcceptor
	)
	{
		super(traverserProvider, alreadyHandled);
		this.handlingPredicate = handlingPredicate;
		this.traversalAcceptor = traversalAcceptor;
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
		
		this.handle(instance, this.traversalAcceptor);
	}
	
}
