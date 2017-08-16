package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XSet;

public interface TraversalReferenceHandlerProvider
{
	public AbstractReferenceHandler provideReferenceHandler(
		final XSet<Object>          alreadyHandled   ,
		final TypeTraverserProvider traverserProvider,
		final Predicate<Object>     isHandleable     ,
		final Predicate<Object>     isFull           ,
		final Predicate<Object>     isNode           ,
		final Predicate<Object>     isLeaf           ,
		final TraversalAcceptor     traversalAcceptor,
		final TraversalMutator      traversalMutator ,
		final MutationListener      mutationListener
	);
	
	
	
	public static TraversalReferenceHandlerProvider New()
	{
		return new TraversalReferenceHandlerProvider.Implementation();
	}
	
	public class Implementation implements TraversalReferenceHandlerProvider
	{
		@Override
		public AbstractReferenceHandler provideReferenceHandler(
			final XSet<Object>          alreadyHandled   ,
			final TypeTraverserProvider traverserProvider,
			final Predicate<Object>     isHandleable     ,
			final Predicate<Object>     isFull           ,
			final Predicate<Object>     isNode           ,
			final Predicate<Object>     isLeaf           ,
			final TraversalAcceptor     traversalAcceptor,
			final TraversalMutator      traversalMutator ,
			final MutationListener      mutationListener
		)
		{
			// (16.08.2017 TM)FIXME: fix leaf
			if(traversalMutator != null)
			{
				return traversalAcceptor != null
					? new ReferenceHandlerAcceptingMutating(
						traverserProvider,
						alreadyHandled   ,
						isHandleable     ,
						isNode           ,
						isFull           ,
						traversalAcceptor,
						traversalMutator ,
						mutationListener
					)
					: new ReferenceHandlerMutating(
						traverserProvider,
						alreadyHandled   ,
						isHandleable     ,
						isNode           ,
						isFull           ,
						traversalMutator ,
						mutationListener
					)
				;
			}
			
			final TraversalAcceptor effectiveAcceptor = traversalAcceptor != null
				? traversalAcceptor
				: (i, p) ->
					true
			;
					
			return new ReferenceHandlerAccepting(
				traverserProvider,
				alreadyHandled   ,
				isHandleable     ,
				isNode           ,
				isFull           ,
				effectiveAcceptor
			);
		}
		
	}
}
