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

import java.util.Comparator;

import one.microstream.collections.sorting.Sortable;

public interface XSortableSequence<E> extends XGettingSequence<E>, Sortable<E>, XOrderingSequence<E>
{
	// (01.12.2011 TM)XXX: what about XOrderingList? At least for content behavior insurance.

	public interface Creator<E> extends XGettingSequence.Factory<E>
	{
		@Override
		public XGettingSequence<E> newInstance();
	}



	@Override
	public XSortableSequence<E> shiftTo(long sourceIndex, long targetIndex);
	
	@Override
	public XSortableSequence<E> shiftTo(long sourceIndex, long targetIndex, long length);
	
	@Override
	public XSortableSequence<E> shiftBy(long sourceIndex, long distance);
	
	@Override
	public XSortableSequence<E> shiftBy(long sourceIndex, long distance, long length);

	@Override
	public XSortableSequence<E> swap(long indexA, long indexB);
	
	@Override
	public XSortableSequence<E> swap(long indexA, long indexB, long length);

	/**
	 * {@inheritDoc}
	 * <p>
	 * Unlike the {@link #toReversed()} method, this method does not create a new collection,
	 * but changes the order of its own elements.
	 */
	@Override
	public XSortableSequence<E> reverse();

	@Override
	public XSortableSequence<E> sort(Comparator<? super E> comparator);

	@Override
	public XSortableSequence<E> copy();

	/**
	 * {@inheritDoc}
	 * <p>
	 * Unlike the {@link #reverse()} method, this method creates a new collection and does not change the
	 * existing collection.
	 */
	@Override
	public XSortableSequence<E> toReversed();

}
