package one.microstream.collections;

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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.old.OldCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;

public class SubView<E> implements XGettingSequence<E>
{
	/* (12.07.2012 TM)FIXME: implement SubView
	 * (see all not implemented errors in method stubs)
	 */

	@Override
	public XGettingSequence<E> copy()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME SubView#iterate()
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME SubView#iterate()
	}

	@Override
	public <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME SubView#join()
	}

	@Override
	public E get()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E first()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E at(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XImmutableSequence<E> immure()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long indexOf(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E last()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long lastIndexOf(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E peek()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E poll()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XGettingSequence<E> range(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XGettingSequence<E> toReversed()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XGettingSequence<E> view()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XGettingSequence<E> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean contains(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean containsId(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T copyTo(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long count(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public Equalator<? super E> equality()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean hasVolatileElements()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean isEmpty()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public Iterator<E> iterator()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean nullContained()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public OldCollection<E> old()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E seek(final E sample)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long size()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public Object[] toArray()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean nullAllowed()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long remainingCapacity()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean isFull()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long maximumCapacity()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

}
