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

public interface XIncreasingList<E> extends XInputtingList<E>, XSettingList<E>, XIncreasingSequence<E>
{
	public interface Creator<E>
	extends XInputtingList.Factory<E>, XSettingList.Creator<E>, XIncreasingSequence.Creator<E>
	{
		@Override
		public XIncreasingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> addAll(E... elements);

	@Override
	public XIncreasingList<E> addAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> putAll(E... elements);

	@Override
	public XIncreasingList<E> putAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> prependAll(E... elements);

	@Override
	public XIncreasingList<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> preputAll(E... elements);

	@Override
	public XIncreasingList<E> preputAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> preputAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> setAll(long index, E... elements);

	@Override
	public XIncreasingList<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XIncreasingList<E> swap(long indexA, long indexB);

	@Override
	public XIncreasingList<E> swap(long indexA, long indexB, long length);

	@Override
	public XIncreasingList<E> copy();

	@Override
	public XIncreasingList<E> toReversed();
	@Override
	public XIncreasingList<E> reverse();

	@Override
	public XIncreasingList<E> range(long fromIndex, long toIndex);

	@Override
	public XIncreasingList<E> fill(long offset, long length, E element);

	@Override
	public XIncreasingList<E> sort(Comparator<? super E> comparator);

}
