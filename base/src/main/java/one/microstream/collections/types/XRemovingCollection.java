package one.microstream.collections.types;

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

import one.microstream.collections.interfaces.ConsolidatableCollection;
import one.microstream.collections.interfaces.ExtendedCollection;
import one.microstream.collections.interfaces.OptimizableCollection;
import one.microstream.collections.interfaces.ReleasingCollection;
import one.microstream.collections.interfaces.Truncateable;

public interface XRemovingCollection<E>
extends
ExtendedCollection<E>,
Truncateable,
ConsolidatableCollection,
OptimizableCollection,
ReleasingCollection<E>
{
	public interface Factory<E> extends XFactory<E>
	{
		@Override
		public XRemovingCollection<E> newInstance();
	}


	// removing procedures //

	/**
	 * Clears all elements from the collection while leaving the capacity as it is.
	 */
	@Override
	public void clear();

	/**
	 * Clears (and reinitializes if needed) this collection in the fastest possible way, i.e. by allocating a new and
	 * empty internal storage of default capacity. The collection will be empty after calling this method.
	 */
	@Override
	public void truncate();

	@Override
	public long consolidate();

	/**
	 * Optimizes internal memory usage by rebuilding the storage to only occupy as much memory as needed to store
	 * the currently contained elements in terms of the collection's current memory usage configuration
	 * (e.g. hash density).
	 * <p>
	 * If this is not possible or not needed in the concrete implementation, this method does nothing.
	 * <p>
	 * Note that this method can consume a considerable amount of time depending on the implementation and should
	 * only be called intentionally and accurately when reducing occupied memory is needed.
	 *
	 * @return the amount of elements that can be added before the internal storage has to be adjusted.
	 */
	@Override
	public long optimize();

	public long nullRemove();

	// (29.09.2012 TM)XXX: rename to removeFirst (first occurrence for non-sequence, first in order for sequence)
	// (29.09.2012 TM)XXX: add removeLast()? Would be more efficient for array storages to scan backwards.
	public boolean removeOne(E element);

	public long remove(E element);

	public long removeAll(XGettingCollection<? extends E> elements);

	/**
	 * Removing all elements except the ones contained in the given elements-collection.
	 * <p>
	 * Basically intersect this collection with the given collection and only keeping the resulting elements.
	 * 
	 * @param elements to retain
	 * @return Amount of deleted elements
	 */
	public long retainAll(XGettingCollection<? extends E> elements);

	public long removeDuplicates();

}
