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

import one.microstream.collections.sorting.SortableProcedure;

public interface XIncreasingSequence<E> extends XInputtingSequence<E>, XSortableSequence<E>, SortableProcedure<E>
{
	public interface Creator<E> extends XInputtingSequence.Creator<E>, XSortableSequence.Creator<E>
	{
		@Override
		public XIncreasingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingSequence<E> addAll(E... elements);

	@Override
	public XIncreasingSequence<E> addAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingSequence<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingSequence<E> putAll(E... elements);

	@Override
	public XIncreasingSequence<E> putAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingSequence<E> prependAll(E... elements);

	@Override
	public XIncreasingSequence<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingSequence<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingSequence<E> preputAll(E... elements);

	@Override
	public XIncreasingSequence<E> preputAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingSequence<E> preputAll(XGettingCollection<? extends E> elements);

	@Override
	public XIncreasingSequence<E> swap(long indexA, long indexB);

	@Override
	public XIncreasingSequence<E> swap(long indexA, long indexB, long length);

	@Override
	public XIncreasingSequence<E> copy();

	@Override
	public XIncreasingSequence<E> toReversed();
	@Override
	public XIncreasingSequence<E> reverse();

	@Override
	public XIncreasingSequence<E> range(long fromIndex, long toIndex);

	@Override
	public XIncreasingSequence<E> sort(Comparator<? super E> comparator);

}
