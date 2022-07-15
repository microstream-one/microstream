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
import java.util.RandomAccess;
import java.util.function.Consumer;

import one.microstream.functional.Aggregator;

/**
 * Extended List interface with additional list procedures like distinction between identity and equality
 * element comparison, procedure range specification, higher order (functional) procedures, proper toArray() methods,
 * etc.<br>
 * <br>
 * All {@link XList} implementations have to have {@link RandomAccess} behavior.<br>
 * Intelligent implementations make non-random-access implementations like simple linked lists obsolete.
 *
 * @param <E> type of contained elements
 * 
 *
 */
public interface XList<E> extends XBasicList<E>, XIncreasingList<E>, XDecreasingList<E>, XSequence<E>
{
	public interface Creator<E> extends XBasicList.Creator<E>, XIncreasingList.Creator<E>, XDecreasingList.Creator<E>
	{
		@Override
		public XList<E> newInstance();
	}


	@Override
	public default Aggregator<E, ? extends XList<E>> collector()
	{
		return new Aggregator<E, XList<E>>()
		{
			@Override
			public void accept(final E element)
			{
				XList.this.add(element);
			}

			@Override
			public XList<E> yield()
			{
				return XList.this;
			}
		};
	}



	@SuppressWarnings("unchecked")
	@Override
	public XList<E> addAll(E... elements);

	@Override
	public XList<E> addAll(E[] elements, int offset, int length);

	@Override
	public XList<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XList<E> putAll(E... elements);

	@Override
	public XList<E> putAll(E[] elements, int offset, int length);

	@Override
	public XList<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XList<E> prependAll(E... elements);

	@Override
	public XList<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XList<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XList<E> preputAll(E... elements);

	@Override
	public XList<E> preputAll(E[] elements, int offset, int length);

	@Override
	public XList<E> preputAll(XGettingCollection<? extends E> elements);

	@Override
	public XList<E> setAll(long index, @SuppressWarnings("unchecked") E... elements);

	@Override
	public XList<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XList<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XList<E> swap(long indexA, long indexB);

	@Override
	public XList<E> swap(long indexA, long indexB, long length);

	@Override
	public XList<E> retainRange(long offset, long length);

	@Override
	public XList<E> copy();

	@Override
	public XList<E> toReversed();

	@Override
	public XList<E> reverse();

	@Override
	public XList<E> range(long fromIndex, long toIndex);

	@Override
	public XList<E> fill(long offset, long length, E element);

	@Override
	public XList<E> sort(Comparator<? super E> comparator);

	@Override
	public XList<E> shiftTo(long sourceIndex, long targetIndex);

	@Override
	public XList<E> shiftTo(long sourceIndex, long targetIndex, long length);

	@Override
	public XList<E> shiftBy(long sourceIndex, long distance);

	@Override
	public XList<E> shiftBy(long sourceIndex, long distance, long length);

	@Override
	public <P extends Consumer<? super E>> P iterate(P procedure);

}
