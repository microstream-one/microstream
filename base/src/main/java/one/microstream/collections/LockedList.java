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
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.old.AbstractBridgeXList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XList;
import one.microstream.concurrency.Synchronized;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.SynchronizedIterator;
import one.microstream.util.iterables.SynchronizedListIterator;


public final class LockedList<E> implements XList<E>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XList<E> subject;
	private final Object   lock;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LockedList(final XList<E> list)
	{
		super();
		this.subject = list;
		this.lock    = list;
	}

	public LockedList(final XList<E> list, final Object lock)
	{
		super();
		this.subject = list;
		this.lock    = lock;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final E get()
	{
		return this.subject.get();
	}

	@Override
	public final Equalator<? super E> equality()
	{
		return this.subject.equality();
	}



	///////////////////////////////////////////////////////////////////////////
	// adding //
	///////////

	@Override
	public final void accept(final E e)
	{
		synchronized(this.lock)
		{
			this.subject.accept(e);
		}
	}

	@Override
	public final boolean add(final E e)
	{
		synchronized(this.lock)
		{
			return this.subject.add(e);
		}
	}

	@SafeVarargs
	@Override
	public final LockedList<E> addAll(final E... elements)
	{
		synchronized(this.lock)
		{
			this.subject.addAll(elements);
			return this;
		}
	}

	@Override
	public final LockedList<E> addAll(final E[] elements, final int offset, final int length)
	{
		synchronized(this.lock)
		{
			this.subject.addAll(elements, offset, length);
			return this;
		}
	}

	@Override
	public final LockedList<E> addAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			this.subject.addAll(elements);
			return this;
		}
	}

	@Override
	public final boolean nullAdd()
	{
		synchronized(this.lock)
		{
			return this.subject.nullAdd();
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// putting //
	////////////

	@Override
	public final boolean put(final E e)
	{
		synchronized(this.lock)
		{
			return this.subject.put(e);
		}
	}

	@SafeVarargs
	@Override
	public final LockedList<E> putAll(final E... elements)
	{
		synchronized(this.lock)
		{
			this.subject.putAll(elements);
			return this;
		}
	}

	@Override
	public final LockedList<E> putAll(final E[] elements, final int offset, final int length)
	{
		synchronized(this.lock)
		{
			this.subject.putAll(elements, offset, length);
			return this;
		}
	}

	@Override
	public final LockedList<E> putAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			this.subject.putAll(elements);
			return this;
		}
	}

	@Override
	public final boolean nullPut()
	{
		synchronized(this.lock)
		{
			return this.subject.nullPut();
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// prepending //
	///////////////

	@Override
	public final boolean prepend(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.prepend(element);
		}
	}

	@SafeVarargs
	@Override
	public final LockedList<E> prependAll(final E... elements)
	{
		synchronized(this.lock)
		{
			this.subject.prependAll(elements);
		}
		return this;
	}

	@Override
	public final LockedList<E> prependAll(final E[] elements, final int offset, final int length)
	{
		synchronized(this.lock)
		{
			this.subject.prependAll(elements, offset, length);
		}
		return this;
	}

	@Override
	public final LockedList<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			this.subject.prependAll(elements);
		}
		return this;
	}

	@Override
	public final boolean nullPrepend()
	{
		synchronized(this.lock)
		{
			return this.subject.nullPrepend();
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// preputting //
	///////////////

	@Override
	public final boolean preput(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.preput(element);
		}
	}

	@SafeVarargs
	@Override
	public final LockedList<E> preputAll(final E... elements)
	{
		synchronized(this.lock)
		{
			this.subject.preputAll(elements);
		}
		return this;
	}

	@Override
	public final LockedList<E> preputAll(final E[] elements, final int offset, final int length)
	{
		synchronized(this.lock)
		{
			this.subject.preputAll(elements, offset, length);
		}
		return this;
	}

	@Override
	public final LockedList<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			this.subject.preputAll(elements);
		}
		return this;
	}

	@Override
	public final boolean nullPreput()
	{
		synchronized(this.lock)
		{
			return this.subject.nullPreput();
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// inserting //
	//////////////

	@Override
	public final boolean insert(final long index, final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.insert(index, element);
		}
	}

	@SafeVarargs
	@Override
	public final long insertAll(final long index, final E... elements)
	{
		synchronized(this.lock)
		{
			return this.subject.insertAll(index, elements);
		}
	}

	@Override
	public final long insertAll(final long index, final E[] elements, final int offset, final int length)
	{
		synchronized(this.lock)
		{
			return this.subject.insertAll(index, elements, offset, length);
		}
	}

	@Override
	public final long insertAll(final long index, final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.insertAll(index, elements);
		}
	}

	@Override
	public final boolean nullInsert(final long index)
	{
		synchronized(this.lock)
		{
			return this.subject.nullInsert(index);
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// inputting //
	//////////////

	@Override
	public final boolean input(final long index, final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.input(index, element);
		}
	}

	@SafeVarargs
	@Override
	public final long inputAll(final long index, final E... elements)
	{
		synchronized(this.lock)
		{
			return this.subject.inputAll(index, elements);
		}
	}

	@Override
	public final long inputAll(final long index, final E[] elements, final int offset, final int length)
	{
		synchronized(this.lock)
		{
			return this.subject.inputAll(index, elements, offset, length);
		}
	}

	@Override
	public final long inputAll(final long index, final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.inputAll(index, elements);
		}
	}

	@Override
	public final boolean nullInput(final long index)
	{
		synchronized(this.lock)
		{
			return this.subject.nullInput(index);
		}
	}

	@Override
	public final boolean containsSearched(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.containsSearched(predicate);
		}
	}

	@Override
	public final boolean applies(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.applies(predicate);
		}
	}

	@Override
	public final void clear()
	{
		synchronized(this.lock)
		{
			this.subject.clear();
		}
	}

	@Override
	public final long consolidate()
	{
		synchronized(this.lock)
		{
			return this.subject.consolidate();
		}
	}

	@Override
	public final boolean contains(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.contains(element);
		}
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.containsAll(elements);
		}
	}

	@Override
	public final boolean containsId(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.containsId(element);
		}
	}

	@Override
	public final LockedList<E> copy()
	{
		synchronized(this.lock)
		{
			return new LockedList<>(this.subject.copy(), new Object());
		}
	}

	@Override
	public final XImmutableList<E> immure()
	{
		synchronized(this.lock)
		{
			return this.subject.immure();
		}
	}

	@Override
	public final ListView<E> view()
	{
		return new ListView<>(this);
	}

	@Override
	public final <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		synchronized(this.lock)
		{
			return this.subject.copySelection(target, indices);
		}
	}

	@Override
	public final <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.filterTo(target, predicate);
		}
	}

	@Override
	public final <C extends Consumer<? super E>> C copyTo(final C target)
	{
		synchronized(this.lock)
		{
			return this.subject.copyTo(target);
		}
	}

	@Override
	public final long count(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.count(element);
		}
	}

	@Override
	public final long countBy(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.countBy(predicate);
		}
	}

	@Override
	public final <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		synchronized(this.lock)
		{
			return this.subject.distinct(target, equalator);
		}
	}

	@Override
	public final <C extends Consumer<? super E>> C distinct(final C target)
	{
		synchronized(this.lock)
		{
			return this.subject.distinct(target);
		}
	}

	@Override
	public final LockedList<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		synchronized(this.lock)
		{
			this.subject.ensureFreeCapacity(minimalFreeCapacity);
			return this;
		}
	}

	@Override
	public final LockedList<E> ensureCapacity(final long minimalCapacity)
	{
		synchronized(this.lock)
		{
			this.subject.ensureCapacity(minimalCapacity);
			return this;
		}
	}

	@Deprecated
	@Override
	public final boolean equals(final Object o)
	{
		synchronized(this.lock)
		{
			return this.subject.equals(o);
		}
	}

	@Override
	public final boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		synchronized(this.lock)
		{
			return this.subject.equals(samples, equalator);
		}
	}

	@Override
	public final boolean equalsContent(
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator
	)
	{
		synchronized(this.lock)
		{
			return this.subject.equalsContent(samples, equalator);
		}
	}

	@Override
	public final <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		synchronized(this.lock)
		{
			return this.subject.except(other, equalator, target);
		}
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		synchronized(this.lock)
		{
			return this.subject.iterate(procedure);
		}
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		synchronized(this.lock)
		{
			return this.subject.join(joiner, aggregate);
		}
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		synchronized(this.lock)
		{
			return this.subject.iterateIndexed(procedure);
		}
	}

	@Override
	public final LockedList<E> fill(final long offset, final long length, final E element)
	{
		synchronized(this.lock)
		{
			this.subject.fill(offset, length, element);
			return this;
		}
	}

	@Override
	public final E at(final long index)
	{
		synchronized(this.lock)
		{
			return this.subject.at(index);
		}
	}

	@Override
	public final E first()
	{
		synchronized(this.lock)
		{
			return this.subject.first();
		}
	}

	@Override
	public final E last()
	{
		synchronized(this.lock)
		{
			return this.subject.last();
		}
	}

	@Override
	public final E poll()
	{
		synchronized(this.lock)
		{
			return this.subject.poll();
		}
	}

	@Override
	public final E peek()
	{
		synchronized(this.lock)
		{
			return this.subject.peek();
		}
	}

	@Deprecated
	@Override
	public final int hashCode()
	{
		synchronized(this.lock)
		{
			return this.subject.hashCode();
		}
	}

	@Override
	public final boolean hasVolatileElements()
	{
		synchronized(this.lock)
		{
			return this.subject.hasVolatileElements();
		}
	}

	@Override
	public final long indexOf(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.indexOf(element);
		}
	}

	@Override
	public final long indexBy(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.indexBy(predicate);
		}
	}

	@Override
	public final <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		synchronized(this.lock)
		{
			return this.subject.intersect(other, equalator, target);
		}
	}

	@Override
	public final boolean isEmpty()
	{
		synchronized(this.lock)
		{
			return this.subject.isEmpty();
		}
	}

	@Override
	public final boolean isSorted(final Comparator<? super E> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.isSorted(comparator);
		}
	}

	@Override
	public final Iterator<E> iterator()
	{
		synchronized(this.lock)
		{
			return new SynchronizedIterator<>(this.subject.iterator());
		}
	}

	@Override
	public final long lastIndexBy(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.lastIndexBy(predicate);
		}
	}

	@Override
	public final long lastIndexOf(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.lastIndexOf(element);
		}
	}

	@Override
	public final ListIterator<E> listIterator()
	{
		synchronized(this.lock)
		{
			return new SynchronizedListIterator<>(this.subject.listIterator());
		}
	}

	@Override
	public final ListIterator<E> listIterator(final long index)
	{
		synchronized(this.lock)
		{
			return new SynchronizedListIterator<>(this.subject.listIterator(index));
		}
	}

	@Override
	public final E max(final Comparator<? super E> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.max(comparator);
		}
	}

	@Override
	public final long maxIndex(final Comparator<? super E> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.maxIndex(comparator);
		}
	}

	@Override
	public final E min(final Comparator<? super E> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.min(comparator);
		}
	}

	@Override
	public final long minIndex(final Comparator<? super E> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.minIndex(comparator);
		}
	}

	@Override
	public final <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		synchronized(this.lock)
		{
			return this.subject.moveSelection(target, indices);
		}
	}

	@Override
	public final <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.moveTo(target, predicate);
		}
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		synchronized(this.lock)
		{
			return this.subject.process(procedure);
		}
	}

	@Override
	public final long removeBy(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.removeBy(predicate);
		}
	}

	@Override
	public final long remove(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.remove(element);
		}
	}

	@Override
	public final E removeAt(final long index)
	{
		synchronized(this.lock)
		{
			return this.subject.removeAt(index);
		}
	}

	@Override
	public final long removeAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.removeAll(elements);
		}
	}

	@Override
	public final long removeDuplicates(final Equalator<? super E> equalator)
	{
		synchronized(this.lock)
		{
			return this.subject.removeDuplicates(equalator);
		}
	}

	@Override
	public final long removeDuplicates()
	{
		synchronized(this.lock)
		{
			return this.subject.removeDuplicates();
		}
	}

	@Override
	public final E fetch()
	{
		synchronized(this.lock)
		{
			return this.subject.fetch();
		}
	}

	@Override
	public final E pop()
	{
		synchronized(this.lock)
		{
			return this.subject.pop();
		}
	}

	@Override
	public final E pinch()
	{
		synchronized(this.lock)
		{
			return this.subject.pinch();
		}
	}

	@Override
	public final E pick()
	{
		synchronized(this.lock)
		{
			return this.subject.pick();
		}
	}

	@Override
	public final E retrieveBy(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.retrieveBy(predicate);
		}
	}

	@Override
	public final E retrieve(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.retrieve(element);
		}
	}

	@Override
	public final boolean removeOne(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.removeOne(element);
		}
	}

	@Override
	public final LockedList<E> removeRange(final long startIndex, final long length)
	{
		synchronized(this.lock)
		{
			this.subject.removeRange(startIndex, length);
			return this;
		}
	}

	@Override
	public final LockedList<E> retainRange(final long startIndex, final long length)
	{
		synchronized(this.lock)
		{
			this.subject.retainRange(startIndex, length);
			return this;
		}
	}

	@Override
	public final long removeSelection(final long[] indices)
	{
		synchronized(this.lock)
		{
			return this.subject.removeSelection(indices);
		}
	}

	@Override
	public final long replace(final E element, final E replacement)
	{
		synchronized(this.lock)
		{
			return this.subject.replace(element, replacement);
		}
	}

	@Override
	public final long replace(final Predicate<? super E> predicate, final E substitute)
	{
		synchronized(this.lock)
		{
			return this.subject.replace(predicate, substitute);
		}
	}

	@Override
	public final long substitute(final Function<? super E, ? extends E> mapper)
	{
		synchronized(this.lock)
		{
			return this.subject.substitute(mapper);
		}
	}

	@Override
	public final long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		synchronized(this.lock)
		{
			return this.subject.substitute(predicate, mapper);
		}
	}

	@Override
	public final long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		synchronized(this.lock)
		{
			return this.subject.replaceAll(elements, replacement);
		}
	}

	@Override
	public final boolean replaceOne(final E element, final E replacement)
	{
		synchronized(this.lock)
		{
			return this.subject.replaceOne(element, replacement);
		}
	}

	@Override
	public final boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		synchronized(this.lock)
		{
			return this.subject.replaceOne(predicate, substitute);
		}
	}

	@Override
	public final long retainAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.retainAll(elements);
		}
	}

	@Override
	public final LockedList<E> reverse()
	{
		synchronized(this.lock)
		{
			this.subject.reverse();
		}
		return this;
	}

	@Override
	public final long scan(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.scan(predicate);
		}
	}

	@Override
	public final E seek(final E sample)
	{
		synchronized(this.lock)
		{
			return this.subject.seek(sample);
		}
	}

	@Override
	public final E search(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.search(predicate);
		}
	}

	@SafeVarargs
	@Override
	public final LockedList<E> setAll(final long offset, final E... elements)
	{
		synchronized(this.lock)
		{
			this.subject.setAll(offset, elements);
			return this;
		}
	}

	@Override
	public final boolean set(final long index, final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.set(index, element);
		}
	}

	@Override
	public final E setGet(final long index, final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.setGet(index, element);
		}
	}

	@Override
	public final LockedList<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		synchronized(this.lock)
		{
			this.subject.set(offset, src, srcIndex, srcLength);
			return this;
		}
	}

	@Override
	public final LockedList<E> set(
		final long                           offset        ,
		final XGettingSequence<? extends E> elements      ,
		final long                           elementsOffset,
		final long                           elementsLength
	)
	{
		synchronized(this.lock)
		{
			this.subject.set(offset, elements, elementsOffset, elementsLength);
			return this;
		}
	}

	@Override
	public final void setFirst(final E element)
	{
		synchronized(this.lock)
		{
			this.subject.setFirst(element);
		}
	}

	@Override
	public final void setLast(final E element)
	{
		synchronized(this.lock)
		{
			this.subject.setLast(element);
		}
	}

	@Override
	public final long optimize()
	{
		synchronized(this.lock)
		{
			return this.subject.optimize();
		}
	}

	@Override
	public final long size()
	{
		synchronized(this.lock)
		{
			return XTypes.to_int(this.subject.size());
		}
	}

	@Override
	public final LockedList<E> sort(final Comparator<? super E> comparator)
	{
		synchronized(this.lock)
		{
			this.subject.sort(comparator);
			return this;
		}
	}

	@Override
	public final XList<E> range(final long fromIndex, final long toIndex)
	{
		synchronized(this.lock)
		{
			// (28.01.2011 TM)NOTE: not sure if this is sufficient ^^
			return new LockedList<>(this.subject.range(fromIndex, toIndex), this.lock);
		}
	}

	@Override
	public final LockedList<E> swap(final long indexA, final long indexB, final long length)
	{
		synchronized(this.lock)
		{
			this.subject.swap(indexA, indexB, length);
			return this;
		}
	}

	@Override
	public final LockedList<E> swap(final long indexA, final long indexB)
	{
		synchronized(this.lock)
		{
			this.subject.swap(indexA, indexB);
			return this;
		}
	}

	@Override
	public final Object[] toArray()
	{
		synchronized(this.lock)
		{
			return this.subject.toArray();
		}
	}

	@Override
	public final E[] toArray(final Class<E> type)
	{
		synchronized(this.lock)
		{
			return this.subject.toArray(type);
		}
	}

	@Override
	public final LockedList<E> toReversed()
	{
		synchronized(this.lock)
		{
			this.subject.toReversed();
			return this;
		}
	}

	@Override
	public final void truncate()
	{
		synchronized(this.lock)
		{
			this.subject.truncate();
		}
	}

	@Override
	public final <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		synchronized(this.lock)
		{
			return this.subject.union(other, equalator, target);
		}
	}

	@Override
	public final long currentCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.currentCapacity();
		}
	}

	@Override
	public final long maximumCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.maximumCapacity();
		}
	}

	@Override
	public final boolean isFull()
	{
		synchronized(this.lock)
		{
			return XTypes.to_int(this.subject.size()) >= this.subject.maximumCapacity();
		}
	}

	@Override
	public final long remainingCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.remainingCapacity();
		}
	}

	@Override
	public final boolean nullAllowed()
	{
		synchronized(this.lock)
		{
			return this.subject.nullAllowed();
		}
	}

	@Override
	public final boolean nullContained()
	{
		synchronized(this.lock)
		{
			return this.subject.nullContained();
		}
	}

	@Override
	public final long nullRemove()
	{
		synchronized(this.lock)
		{
			return this.subject.nullRemove();
		}
	}

	@Override
	public final XGettingList<E> view(final long fromIndex, final long toIndex)
	{
		synchronized(this.lock)
		{
			return this.subject.view(fromIndex, toIndex);
		}
	}

	@Override
	public final LockedList<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		synchronized(this.lock)
		{
			this.subject.shiftTo(sourceIndex, targetIndex);
		}
		return this;
	}

	@Override
	public final LockedList<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		synchronized(this.lock)
		{
			this.subject.shiftTo(sourceIndex, targetIndex, length);
		}
		return this;
	}

	@Override
	public final LockedList<E> shiftBy(final long sourceIndex, final long distance)
	{
		synchronized(this.lock)
		{
			this.subject.shiftTo(sourceIndex, distance);
		}
		return this;
	}

	@Override
	public final LockedList<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		synchronized(this.lock)
		{
			this.subject.shiftTo(sourceIndex, distance, length);
		}
		return this;
	}



	@Override
	public final OldMutexList<E> old()
	{
		return new OldMutexList<>(this);
	}

	public static final class OldMutexList<E> extends AbstractBridgeXList<E>
	{
		OldMutexList(final LockedList<E> list)
		{
			super(list);
		}

		@Override
		public final LockedList<E> parent()
		{
			return (LockedList<E>)super.parent();
		}

	}

}
