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

public final class TraverserNoOp<T> implements TypeTraverser<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> TraverserNoOp<T> New(final Class<T> type)
	{
		return new TraverserNoOp<>(type);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Class<T> type;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	TraverserNoOp(final Class<T> type)
	{
		super();
		this.type = type;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final Class<T> type()
	{
		return this.type;
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance,
		final TraversalEnqueuer enqueuer
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance,
		final TraversalEnqueuer enqueuer,
		final TraversalAcceptor acceptor
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	

	@Override
	public final void traverseReferences(
		final T                 instance,
		final TraversalAcceptor acceptor
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	
}
