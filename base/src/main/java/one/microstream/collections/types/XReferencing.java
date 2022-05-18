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
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.Constant;
import one.microstream.collections.old.OldList;
import one.microstream.equality.Equalator;
import one.microstream.reference.Referencing;

// (15.01.2013 TM)FIXME: Rename to XSingle or so
public interface XReferencing<E> extends XGettingList<E>, XGettingEnum<E>, Referencing<E>
{
	@Override
	public E get();



	@Override
	public E at(long index);

	@Override
	public E first();

	@Override
	public E last();

	@Override
	public E poll();

	@Override
	public E peek();

	@Override
	public long maxIndex(Comparator<? super E> comparator);

	@Override
	public long minIndex(Comparator<? super E> comparator);

	@Override
	public long indexOf(E element);

	@Override
	public long indexBy(Predicate<? super E> predicate);

	@Override
	public long lastIndexOf(E element);

	@Override
	public long lastIndexBy(Predicate<? super E> predicate);

	@Override
	public long scan(Predicate<? super E> predicate);

	@Override
	public boolean isSorted(Comparator<? super E> comparator);

	@Override
	public <T extends Consumer<? super E>> T copySelection(T target, long... indices);

	@Override
	public Iterator<E> iterator();

	@Override
	public Object[] toArray();

	@Override
	public boolean hasVolatileElements();

	@Override
	public long size();

	@Override
	public boolean isEmpty();

	@Override
	public Equalator<? super E> equality();

	@Override
	public E[] toArray(Class<E> type);

	@Override
	public boolean equals(XGettingCollection<? extends E> samples, Equalator<? super E> equalator);

	@Override
	public boolean equalsContent(XGettingCollection<? extends E> samples, Equalator<? super E> equalator);

	@Override
	public boolean nullContained();

	@Override
	public boolean containsId(E element);

	@Override
	public boolean contains(E element);

	@Override
	public boolean containsSearched(Predicate<? super E> predicate);

	@Override
	public boolean containsAll(XGettingCollection<? extends E> elements);

	@Override
	public boolean applies(Predicate<? super E> predicate);

	@Override
	public long count(E element);

	@Override
	public long countBy(Predicate<? super E> predicate);

	@Override
	public E search(Predicate<? super E> predicate);

	@Override
	public E seek(E sample);

	@Override
	public E max(Comparator<? super E> comparator);

	@Override
	public E min(Comparator<? super E> comparator);

	@Override
	public <T extends Consumer<? super E>> T distinct(T target);

	@Override
	public <T extends Consumer<? super E>> T distinct(T target, Equalator<? super E> equalator);

	@Override
	public <T extends Consumer<? super E>> T copyTo(T target);

	@Override
	public <T extends Consumer<? super E>> T filterTo(T target, Predicate<? super E> predicate);

	@Override
	public <T extends Consumer<? super E>> T union(XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

	@Override
	public <T extends Consumer<? super E>> T intersect(XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

	@Override
	public <T extends Consumer<? super E>> T except(XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

	@Override
	public boolean nullAllowed();

	@Override
	public long maximumCapacity();

	@Override
	public long remainingCapacity();

	@Override
	public boolean isFull();

	@Override
	public Constant<E> immure();

	@Override
	public ListIterator<E> listIterator();

	@Override
	public ListIterator<E> listIterator(long index);

	@Override
	public OldList<E> old();

	@Override
	public XReferencing<E> copy();

	@Override
	public XReferencing<E> toReversed();

	@Override
	public XReferencing<E> view();

	@Override
	public XReferencing<E> view(long lowIndex, long highIndex);

	@Override
	public XReferencing<E> range(long fromIndex, long toIndex);

}
