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
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XList;
import one.microstream.concurrency.Synchronized;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.SynchronizedIterator;
import one.microstream.util.iterables.SynchronizedListIterator;


/**
 * Synchronization wrapper class that wraps an {@link XList} instance in public synchronized delegate methods.
 *
 */
public final class SynchList<E> implements XList<E>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/**
	 * The {@link XList} instance to be wrapped (synchronized).
	 */
	private final XList<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link SynchList} that wraps the passed {@link XList} instance.
	 *
	 * @param list the {@link XList} instance to be wrapped (synchronized).
	 */
	public SynchList(final XList<E> list)
	{
		super();
		this.subject = list;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final synchronized Equalator<? super E> equality()
	{
		return this.subject.equality();
	}



	///////////////////////////////////////////////////////////////////////////
	// adding //
	///////////

	@Override
	public final synchronized void accept(final E e)
	{
		this.subject.accept(e);
	}

	@SafeVarargs
	@Override
	public final synchronized SynchList<E> addAll(final E... elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public final synchronized boolean add(final E e)
	{
		return this.subject.add(e);
	}

	@Override
	public final synchronized SynchList<E> addAll(final E[] elements, final int offset, final int length)
	{
		this.subject.addAll(elements, offset, length);
		return this;
	}

	@Override
	public final synchronized SynchList<E> addAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public final synchronized boolean nullAdd()
	{
		return this.subject.nullAdd();
	}



	///////////////////////////////////////////////////////////////////////////
	// putting //
	////////////

	@Override
	public final synchronized boolean put(final E e)
	{
		return this.subject.put(e);
	}

	@SafeVarargs
	@Override
	public final synchronized SynchList<E> putAll(final E... elements)
	{
		this.subject.putAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchList<E> putAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.putAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchList<E> putAll(final E[] elements, final int offset, final int length)
	{
		this.subject.putAll(elements, offset, length);
		return this;
	}

	@Override
	public final synchronized boolean nullPut()
	{
		return this.subject.nullPut();
	}



	///////////////////////////////////////////////////////////////////////////
	// prepending //
	///////////////

	@Override
	public final synchronized boolean prepend(final E element)
	{
		return this.subject.prepend(element);
	}

	@SafeVarargs
	@Override
	public final synchronized SynchList<E> prependAll(final E... elements)
	{
		this.subject.prependAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchList<E> prependAll(final E[] elements, final int offset, final int length)
	{
		this.subject.prependAll(elements, offset, length);
		return this;
	}

	@Override
	public final synchronized SynchList<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.prependAll(elements);
		return this;
	}

	@Override
	public final synchronized boolean nullPrepend()
	{
		return this.subject.nullPrepend();
	}



	///////////////////////////////////////////////////////////////////////////
	// preputting //
	///////////////

	@Override
	public final synchronized boolean preput(final E element)
	{
		return this.subject.preput(element);
	}

	@SafeVarargs
	@Override
	public final synchronized SynchList<E> preputAll(final E... elements)
	{
		this.subject.preputAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchList<E> preputAll(final E[] elements, final int offset, final int length)
	{
		this.subject.preputAll(elements, offset, length);
		return this;
	}

	@Override
	public final synchronized SynchList<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.preputAll(elements);
		return this;
	}

	@Override
	public final synchronized boolean nullPreput()
	{
		return this.subject.nullPreput();
	}



	///////////////////////////////////////////////////////////////////////////
	// inserting //
	//////////////

	@Override
	public final synchronized boolean insert(final long index, final E element)
	{
		return this.subject.insert(index, element);
	}

	@SafeVarargs
	@Override
	public final synchronized long insertAll(final long index, final E... elements)
	{
		return this.subject.insertAll(index, elements);
	}

	@Override
	public final synchronized long insertAll(final long index, final E[] elements, final int offset, final int length)
	{
		return this.subject.insertAll(index, elements, offset, length);
	}

	@Override
	public final synchronized long insertAll(final long index, final XGettingCollection<? extends E> elements)
	{
		return this.subject.insertAll(index, elements);
	}

	@Override
	public final synchronized boolean nullInsert(final long index)
	{
		return this.subject.nullInsert(index);
	}



	///////////////////////////////////////////////////////////////////////////
	// inputting //
	//////////////

	@Override
	public final synchronized boolean input(final long index, final E element)
	{
		return this.subject.input(index, element);
	}

	@SafeVarargs
	@Override
	public final synchronized long inputAll(final long index, final E... elements)
	{
		return this.subject.inputAll(index, elements);
	}

	@Override
	public final synchronized long inputAll(final long index, final E[] elements, final int offset, final int length)
	{
		return this.subject.inputAll(index, elements, offset, length);
	}

	@Override
	public final synchronized long inputAll(final long index, final XGettingCollection<? extends E> elements)
	{
		return this.subject.inputAll(index, elements);
	}

	@Override
	public final synchronized boolean nullInput(final long index)
	{
		return this.subject.nullInput(index);
	}

	@Override
	public final synchronized boolean containsSearched(final Predicate<? super E> predicate)
	{
		return this.subject.containsSearched(predicate);
	}

	@Override
	public final synchronized boolean applies(final Predicate<? super E> predicate)
	{
		return this.subject.applies(predicate);
	}

	@Override
	public final synchronized void clear()
	{
		this.subject.clear();
	}

	@Override
	public final synchronized long consolidate()
	{
		return this.subject.consolidate();
	}

	@Override
	public final synchronized boolean contains(final E element)
	{
		return this.subject.contains(element);
	}

	@Override
	public final synchronized boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.containsAll(elements);
	}

	@Override
	public final synchronized boolean containsId(final E element)
	{
		return this.subject.containsId(element);
	}

	@Override
	public final synchronized SynchList<E> copy()
	{
		return new SynchList<>(this.subject);
	}

	@Override
	public final synchronized XImmutableList<E> immure()
	{
		return this.subject.immure();
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return this.subject.copySelection(target, indices);
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.filterTo(target, predicate);
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return this.subject.copyTo(target);
	}

	@Override
	public final synchronized long count(final E element)
	{
		return this.subject.count(element);
	}

	@Override
	public final synchronized long countBy(final Predicate<? super E> predicate)
	{
		return this.subject.countBy(predicate);
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return this.subject.distinct(target, equalator);
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C distinct(final C target)
	{
		return this.subject.distinct(target);
	}

	@Override
	public final synchronized SynchList<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		this.subject.ensureFreeCapacity(minimalFreeCapacity);
		return this;
	}

	@Override
	public final synchronized SynchList<E> ensureCapacity(final long minimalCapacity)
	{
		this.subject.ensureCapacity(minimalCapacity);
		return this;
	}

	@Deprecated
	@Override
	public final synchronized boolean equals(final Object o)
	{
		return this.subject.equals(o);
	}

	@Override
	public final synchronized boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equals(samples, equalator);
	}

	@Override
	public final synchronized boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equalsContent(samples, equalator);
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.except(other, equalator, target);
	}

	@Override
	public final synchronized <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		this.subject.iterate(procedure);
		return procedure;
	}

	@Override
	public final synchronized <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		this.subject.iterateIndexed(procedure);
		return procedure;
	}

	@Override
	public final synchronized <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
	}

	@Override
	public final synchronized SynchList<E> fill(final long offset, final long length, final E element)
	{
		this.subject.fill(offset, length, element);
		return this;
	}

	@Override
	public final synchronized E at(final long index)
	{
		return this.subject.at(index);
	}

	@Override
	public final synchronized E get()
	{
		return this.subject.get();
	}

	@Override
	public final synchronized E first()
	{
		return this.subject.first();
	}

	@Override
	public final synchronized E last()
	{
		return this.subject.last();
	}

	@Override
	public final synchronized E poll()
	{
		return this.subject.poll();
	}

	@Override
	public final synchronized E peek()
	{
		return this.subject.peek();
	}

	@Deprecated
	@Override
	public final synchronized int hashCode()
	{
		return this.subject.hashCode();
	}

	@Override
	public final synchronized boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public final synchronized long indexOf(final E element)
	{
		return this.subject.indexOf(element);
	}

	@Override
	public final synchronized long indexBy(final Predicate<? super E> predicate)
	{
		return this.subject.indexBy(predicate);
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.intersect(other, equalator, target);
	}

	@Override
	public final synchronized boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public final synchronized boolean isSorted(final Comparator<? super E> comparator)
	{
		return this.subject.isSorted(comparator);
	}

	@Override
	public final synchronized Iterator<E> iterator()
	{
		return new SynchronizedIterator<>(this.subject.iterator());
	}

	@Override
	public final synchronized long lastIndexBy(final Predicate<? super E> predicate)
	{
		return this.subject.lastIndexBy(predicate);
	}

	@Override
	public final synchronized long lastIndexOf(final E element)
	{
		return this.subject.lastIndexOf(element);
	}

	@Override
	public final synchronized ListIterator<E> listIterator()
	{
		return new SynchronizedListIterator<>(this.subject.listIterator());
	}

	@Override
	public final synchronized ListIterator<E> listIterator(final long index)
	{
		return new SynchronizedListIterator<>(this.subject.listIterator(index));
	}

	@Override
	public final synchronized E max(final Comparator<? super E> comparator)
	{
		return this.subject.max(comparator);
	}

	@Override
	public final synchronized long maxIndex(final Comparator<? super E> comparator)
	{
		return this.subject.maxIndex(comparator);
	}

	@Override
	public final synchronized E min(final Comparator<? super E> comparator)
	{
		return this.subject.min(comparator);
	}

	@Override
	public final synchronized long minIndex(final Comparator<? super E> comparator)
	{
		return this.subject.minIndex(comparator);
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		return this.subject.moveSelection(target, indices);
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.moveTo(target, predicate);
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		return this.subject.process(procedure);
	}

	@Override
	public final synchronized long removeBy(final Predicate<? super E> predicate)
	{
		return this.subject.removeBy(predicate);
	}

	@Override
	public final synchronized long remove(final E element)
	{
		return this.subject.remove(element);
	}

	@Override
	public final synchronized E removeAt(final long index)
	{
		return this.subject.removeAt(index);
	}

	@Override
	public final synchronized long removeAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.removeAll(elements);
	}

	@Override
	public final synchronized long removeDuplicates(final Equalator<? super E> equalator)
	{
		return this.subject.removeDuplicates(equalator);
	}

	@Override
	public final synchronized long removeDuplicates()
	{
		return this.subject.removeDuplicates();
	}

	@Override
	public final synchronized E fetch()
	{
		return this.subject.fetch();
	}

	@Override
	public final synchronized E pop()
	{
		return this.subject.pop();
	}

	@Override
	public final synchronized E pinch()
	{
		return this.subject.pinch();
	}

	@Override
	public final synchronized E pick()
	{
		return this.subject.pick();
	}

	@Override
	public final synchronized boolean removeOne(final E element)
	{
		return this.subject.removeOne(element);
	}

	@Override
	public final synchronized E retrieve(final E element)
	{
		return this.subject.retrieve(element);
	}

	@Override
	public final synchronized E retrieveBy(final Predicate<? super E> predicate)
	{
		return this.subject.retrieveBy(predicate);
	}

	@Override
	public final synchronized SynchList<E> removeRange(final long startIndex, final long length)
	{
		this.subject.removeRange(startIndex, length);
		return this;
	}

	@Override
	public final synchronized SynchList<E> retainRange(final long startIndex, final long length)
	{
		this.subject.retainRange(startIndex, length);
		return this;
	}

	@Override
	public final synchronized long removeSelection(final long[] indices)
	{
		return this.subject.removeSelection(indices);
	}

	@Override
	public final synchronized long replace(final E element, final E replacement)
	{
		return this.subject.replace(element, replacement);
	}

	@Override
	public final synchronized long replace(final Predicate<? super E> predicate, final E substitute)
	{
		return this.subject.replace(predicate, substitute);
	}

	@Override
	public final synchronized long substitute(final Function<? super E, ? extends E> mapper)
	{
		return this.subject.substitute(mapper);
	}

	@Override
	public final long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		return this.subject.substitute(predicate, mapper);
	}

	@Override
	public final synchronized long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		return this.subject.replaceAll(elements, replacement);
	}

	@Override
	public final synchronized boolean replaceOne(final E element, final E replacement)
	{
		return this.subject.replaceOne(element, replacement);
	}

	@Override
	public final synchronized boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		return this.subject.replaceOne(predicate, substitute);
	}

	@Override
	public final synchronized long retainAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.retainAll(elements);
	}

	@Override
	public final synchronized SynchList<E> reverse()
	{
		this.subject.reverse();
		return this;
	}

	@Override
	public final synchronized long scan(final Predicate<? super E> predicate)
	{
		return this.subject.scan(predicate);
	}

	@Override
	public final synchronized E seek(final E sample)
	{
		return this.subject.seek(sample);
	}

	@Override
	public final synchronized E search(final Predicate<? super E> predicate)
	{
		return this.subject.search(predicate);
	}

	@SafeVarargs
	@Override
	public final synchronized SynchList<E> setAll(final long offset, final E... elements)
	{
		this.subject.setAll(offset, elements);
		return this;
	}

	@Override
	public final synchronized boolean set(final long index, final E element)
	{
		return this.subject.set(index, element);
	}

	@Override
	public final synchronized E setGet(final long index, final E element)
	{
		return this.subject.setGet(index, element);
	}

	@Override
	public final synchronized SynchList<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		this.subject.set(offset, src, srcIndex, srcLength);
		return this;
	}

	@Override
	public final synchronized SynchList<E> set(final long offset, final XGettingSequence<? extends E> elements, final long elementsOffset, final long elementsLength)
	{
		this.subject.set(offset, elements, elementsOffset, elementsLength);
		return this;
	}

	@Override
	public final synchronized void setFirst(final E element)
	{
		this.subject.setFirst(element);
	}

	@Override
	public final synchronized void setLast(final E element)
	{
		this.subject.setLast(element);
	}

	@Override
	public final synchronized long optimize()
	{
		return this.subject.optimize();
	}

	@Override
	public final synchronized long size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public final synchronized SynchList<E> sort(final Comparator<? super E> comparator)
	{
		this.subject.sort(comparator);
		return this;
	}

	@Override
	public final synchronized XList<E> range(final long fromIndex, final long toIndex)
	{
		// (28.01.2011 TM)NOTE: not sure if this is sufficient
		return new SynchList<>(this.subject.range(fromIndex, toIndex));
	}

	@Override
	public final synchronized SynchList<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		this.subject.shiftTo(sourceIndex, targetIndex);
		return this;
	}

	@Override
	public final synchronized SynchList<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		this.subject.shiftTo(sourceIndex, targetIndex, length);
		return this;
	}

	@Override
	public final synchronized SynchList<E> shiftBy(final long sourceIndex, final long distance)
	{
		this.subject.shiftTo(sourceIndex, distance);
		return this;
	}

	@Override
	public final synchronized SynchList<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		this.subject.shiftTo(sourceIndex, distance, length);
		return this;
	}

	@Override
	public final synchronized SynchList<E> swap(final long indexA, final long indexB, final long length)
	{
		this.subject.swap(indexA, indexB, length);
		return this;
	}

	@Override
	public final synchronized SynchList<E> swap(final long indexA, final long indexB)
	{
		this.subject.swap(indexA, indexB);
		return this;
	}

	@Override
	public final synchronized Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public final synchronized E[] toArray(final Class<E> type)
	{
		return this.subject.toArray(type);
	}

	@Override
	public final synchronized SynchList<E> toReversed()
	{
		this.subject.toReversed();
		return this;
	}

	@Override
	public final synchronized void truncate()
	{
		this.subject.truncate();
	}

	@Override
	public final synchronized <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.union(other, equalator, target);
	}

	@Override
	public final synchronized long currentCapacity()
	{
		return this.subject.currentCapacity();
	}

	@Override
	public final synchronized long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public final synchronized boolean isFull()
	{
		return this.subject.isFull();
	}

	@Override
	public final synchronized long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public final synchronized boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public final synchronized boolean nullContained()
	{
		return this.subject.nullContained();
	}

	@Override
	public final synchronized long nullRemove()
	{
		return this.subject.nullRemove();
	}

	@Override
	public final synchronized ListView<E> view()
	{
		return new ListView<>(this);
	}

	@Override
	public final synchronized SubListView<E> view(final long fromIndex, final long toIndex)
	{
		return new SubListView<>(this, fromIndex, toIndex);
	}



	@Override
	public final OldSynchList<E> old()
	{
		return new OldSynchList<>(this);
	}

	public static final class OldSynchList<E> extends AbstractBridgeXList<E>
	{
		OldSynchList(final SynchList<E> list)
		{
			super(list);
		}

		@Override
		public final SynchList<E> parent()
		{
			return (SynchList<E>)super.parent();
		}

	}

}
