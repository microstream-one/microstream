package one.microstream.util.traversing;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.function.Predicate;

import one.microstream.collections.types.XSet;

public interface TraversalReferenceHandlerProvider
{
	public AbstractReferenceHandler provideReferenceHandler(
		final XSet<Object>           alreadyHandled   ,
		final TypeTraverserProvider  traverserProvider,
		final TraversalPredicateSkip predicateSkip    ,
		final TraversalPredicateNode predicateNode    ,
		final TraversalPredicateLeaf predicateLeaf    ,
		final TraversalPredicateFull predicateFull    ,
		final Predicate<Object>      predicateHandle  ,
		final TraversalAcceptor      traversalAcceptor,
		final TraversalMutator       traversalMutator ,
		final MutationListener       mutationListener
	);
	
	
	
	public static TraversalReferenceHandlerProvider New()
	{
		return new TraversalReferenceHandlerProvider.Default();
	}
	
	public class Default implements TraversalReferenceHandlerProvider
	{
		@Override
		public AbstractReferenceHandler provideReferenceHandler(
			final XSet<Object>           alreadyHandled   ,
			final TypeTraverserProvider  traverserProvider,
			final TraversalPredicateSkip predicateSkip    ,
			final TraversalPredicateNode predicateNode    ,
			final TraversalPredicateLeaf predicateLeaf    ,
			final TraversalPredicateFull predicateFull    ,
			final Predicate<Object>      predicateHandle  ,
			final TraversalAcceptor      traversalAcceptor,
			final TraversalMutator       traversalMutator ,
			final MutationListener       mutationListener
		)
		{
			if(traversalMutator != null)
			{
				return traversalAcceptor != null
					? new ReferenceHandlerAcceptingMutating(
						traverserProvider,
						alreadyHandled   ,
						predicateSkip    ,
						predicateNode    ,
						predicateLeaf    ,
						predicateFull    ,
						predicateHandle  ,
						traversalAcceptor,
						traversalMutator ,
						mutationListener
					)
					: new ReferenceHandlerMutating(
						traverserProvider,
						alreadyHandled   ,
						predicateSkip    ,
						predicateNode    ,
						predicateLeaf    ,
						predicateFull    ,
						predicateHandle  ,
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
				predicateSkip    ,
				predicateNode    ,
				predicateLeaf    ,
				predicateFull    ,
				predicateHandle  ,
				effectiveAcceptor
			);
		}
		
	}
}
