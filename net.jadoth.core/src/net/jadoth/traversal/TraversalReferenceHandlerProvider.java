package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingSet;
import net.jadoth.collections.types.XSet;

public interface TraversalReferenceHandlerProvider
{
	public AbstractReferenceHandler provideReferenceHandler(
		final XSet<Object>                 alreadyHandled          ,
		final XGettingSet<Class<?>>        skippedTypes            ,
		final XGettingCollection<Class<?>> skippedTypesPolymorphic ,
		final TypeTraverserProvider        traverserProvider       ,
		final Predicate<Object>            handlingPredicate       ,
		final TraversalAcceptor            traversalAcceptor       ,
		final TraversalMutator             traversalMutator        ,
		final MutationListener.Provider    mutationListenerProvider
	);
}
