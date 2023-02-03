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
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.Singleton.OldSingleton;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XReferencing;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;

public class SingletonView<E> implements XReferencing<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Singleton<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SingletonView(final Singleton<E> subject)
	{
		super();
		this.subject = subject;
	}



	@Override
	public int hashCode()
	{
		return this.subject.hashCode();
	}

	@Override
	public E get()
	{
		return this.subject.get();
	}

	@Override
	public SingletonView<E> copy()
	{
		return new SingletonView<>(this.subject);
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		return this.subject.iterate(procedure);
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		return this.subject.iterateIndexed(procedure);
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
	}

	@Override
	public Constant<E> immure()
	{
		return this.subject.immure();
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return this.subject.listIterator();
	}

	@Override
	public boolean equals(final Object obj)
	{
		return this.subject.equals(obj);
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		return this.subject.listIterator(index);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public OldSingleton old()
	{
		return this.subject.old();
	}

	@Override
	public SingletonView<E> range(final long fromIndex, final long toIndex)
	{
		if(fromIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		if(toIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		return this;
	}

	@Override
	public SingletonView<E> toReversed()
	{
		return this;
	}

	@Override
	public <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		return this.subject.copySelection(target, indices);
	}

	@Override
	public E first()
	{
		return this.subject.first();
	}

	@Override
	public E at(final long index)
	{
		return this.subject.at(index);
	}

	@Override
	public long indexOf(final E element)
	{
		return this.subject.indexOf(element);
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return this.subject.indexBy(predicate);
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return this.subject.isSorted(comparator);
	}

	@Override
	public E last()
	{
		return this.subject.last();
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return this.subject.lastIndexOf(element);
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return this.subject.lastIndexBy(predicate);
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return this.subject.maxIndex(comparator);
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return this.subject.minIndex(comparator);
	}

	@Override
	public E peek()
	{
		return this.subject.peek();
	}

	@Override
	public E poll()
	{
		return this.subject.poll();
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return this.subject.scan(predicate);
	}

	@Override
	public SingletonView<E> view()
	{
		return this;
	}

	@Override
	public SingletonView<E> view(final long lowIndex, final long highIndex)
	{
		if(lowIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		if(highIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		
		return this;
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return this.subject.containsSearched(predicate);
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return this.subject.applies(predicate);
	}

	@Override
	public boolean contains(final E element)
	{
		return this.subject.contains(element);
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.containsAll(elements);
	}

	@Override
	public boolean containsId(final E element)
	{
		return this.subject.containsId(element);
	}

	@Override
	public <T extends Consumer<? super E>> T copyTo(final T target)
	{
		return this.subject.copyTo(target);
	}

	@Override
	public <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		return this.subject.filterTo(target, predicate);
	}

	@Override
	public long count(final E element)
	{
		return this.subject.count(element);
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return this.subject.countBy(predicate);
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target)
	{
		return this.subject.distinct(target);
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		return this.subject.distinct(target, equalator);
	}

	@Override
	public String toString()
	{
		return this.subject.toString();
	}

	@Override
	public Equalator<? super E> equality()
	{
		return this.subject.equality();
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equals(samples, equalator);
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equalsContent(samples, equalator);
	}

	@Override
	public <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		return this.subject.except(other, equalator, target);
	}

//	@Override
//	public boolean hasDistinctValues()
//	{
//		return this.subject.hasDistinctValues();
//	}
//
//	@Override
//	public boolean hasDistinctValues(final Equalator<? super E> equalator)
//	{
//		return this.subject.hasDistinctValues(equalator);
//	}

	@Override
	public boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		return this.subject.intersect(other, equalator, target);
	}

	@Override
	public boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public Iterator<E> iterator()
	{
		return this.subject.iterator();
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return this.subject.max(comparator);
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return this.subject.min(comparator);
	}

	@Override
	public boolean nullContained()
	{
		return this.subject.nullContained();
	}

	@Override
	public E seek(final E sample)
	{
		return this.subject.seek(sample);
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return this.subject.search(predicate);
	}

	@Override
	public long size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		return this.subject.toArray(type);
	}

	@Override
	public <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		return this.subject.union(other, equalator, target);
	}

	@Override
	public boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public boolean isFull()
	{
		return this.subject.isFull();
	}

	@Override
	public long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

}
