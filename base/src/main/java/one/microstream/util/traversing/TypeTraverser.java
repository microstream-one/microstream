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

public interface TypeTraverser<T>
{
	public void traverseReferences(
		T                 instance,
		TraversalEnqueuer enqueuer
	);
	
	public void traverseReferences(
		T                 instance,
		TraversalEnqueuer enqueuer,
		TraversalAcceptor acceptor
	);
	
	public void traverseReferences(
		T                 instance        ,
		TraversalEnqueuer enqueuer        ,
		TraversalMutator  mutator         ,
		MutationListener  mutationListener
	);

	public void traverseReferences(
		T                 instance        ,
		TraversalEnqueuer enqueuer        ,
		TraversalAcceptor acceptor        ,
		TraversalMutator  mutator         ,
		MutationListener  mutationListener
	);
	
	public void traverseReferences(
		T                 instance,
		TraversalAcceptor acceptor
	);
	
	public void traverseReferences(
		T                instance        ,
		TraversalMutator mutator         ,
		MutationListener mutationListener
	);

	public void traverseReferences(
		T                 instance        ,
		TraversalAcceptor acceptor        ,
		TraversalMutator  mutator         ,
		MutationListener  mutationListener
	);
	

	
	public interface Creator
	{
		public <T> TypeTraverser<T> createTraverser(Class<T> type);
						
	}
	
}
