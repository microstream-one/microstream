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

import one.microstream.collections.old.AbstractBridgeXSet;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XImmutableSet;
import one.microstream.collections.types.XSet;
import one.microstream.concurrency.Synchronized;
import one.microstream.equality.Equalator;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.SynchronizedIterator;


/**
 * Synchronization wrapper class that wraps an {@link XSet} instance in public synchronized delegate methods.
 */
public final class SynchSet<E> implements XSet<E>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XSet<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link SynchSet} that wraps the passed {@link XSet} instance.
	 *
	 * @param set the {@link XSet} instance to be wrapped (synchronized).
	 */
	public SynchSet(final XSet<E> set)
	{
		super();
		this.subject = set;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public synchronized E get()
	{
		return this.subject.get();
	}

	@Override
	public synchronized Equalator<? super E> equality()
	{
		return this.subject.equality();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized SynchSet<E> addAll(final E... elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public synchronized boolean nullAdd()
	{
		return this.subject.nullAdd();
	}

	@Override
	public synchronized boolean add(final E e)
	{
		return this.subject.add(e);
	}

	@Override
	public synchronized SynchSet<E> addAll(final E[] elements, final int offset, final int length)
	{
		this.subject.addAll(elements, offset, length);
		return this;
	}

	@Override
	public synchronized SynchSet<E> addAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public synchronized boolean nullPut()
	{
		return this.subject.nullPut();
	}

	@Override
	public synchronized void accept(final E e)
	{
		this.subject.accept(e);
	}

	@Override
	public synchronized boolean put(final E e)
	{
		return this.subject.put(e);
	}

	@Override
	public synchronized E addGet(final E e)
	{
		return this.subject.addGet(e);
	}

	@Override
	public synchronized E deduplicate(final E e)
	{
		return this.subject.deduplicate(e);
	}

	@Override
	public synchronized E putGet(final E e)
	{
		return this.subject.putGet(e);
	}

	@Override
	public synchronized E replace(final E e)
	{
		return this.subject.replace(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized SynchSet<E> putAll(final E... elements)
	{
		this.subject.putAll(elements);
		return this;
	}

	@Override
	public synchronized SynchSet<E> putAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.putAll(elements);
		return this;
	}

	@Override
	public synchronized SynchSet<E> putAll(final E[] elements, final int offset, final int length)
	{
		this.subject.putAll(elements, offset, length);
		return this;
	}

	@Override
	public synchronized boolean containsSearched(final Predicate<? super E> predicate)
	{
		return this.subject.containsSearched(predicate);
	}

	@Override
	public synchronized boolean applies(final Predicate<? super E> predicate)
	{
		return this.subject.applies(predicate);
	}

	@Override
	public synchronized void clear()
	{
		this.subject.clear();
	}

	@Override
	public synchronized long consolidate()
	{
		return this.subject.consolidate();
	}

	@Override
	public synchronized boolean contains(final E element)
	{
		return this.subject.contains(element);
	}

	@Override
	public synchronized boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.containsAll(elements);
	}

	@Override
	public synchronized boolean containsId(final E element)
	{
		return this.subject.containsId(element);
	}

	@Override
	public synchronized boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public synchronized boolean nullContained()
	{
		return this.subject.nullContained();
	}

	@Override
	public synchronized <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.filterTo(target, predicate);
	}

	@Override
	public synchronized <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return this.subject.copyTo(target);
	}

	@Override
	public synchronized long count(final E element)
	{
		return this.subject.count(element);
	}

	@Override
	public synchronized long countBy(final Predicate<? super E> predicate)
	{
		return this.subject.countBy(predicate);
	}

	@Override
	public synchronized <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return this.subject.distinct(target, equalator);
	}

	@Override
	public synchronized <C extends Consumer<? super E>> C distinct(final C target)
	{
		return this.subject.distinct(target);
	}

	@Override
	public synchronized SynchSet<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		this.subject.ensureFreeCapacity(minimalFreeCapacity);
		return this;
	}

	@Override
	public synchronized SynchSet<E> ensureCapacity(final long minimalCapacity)
	{
		this.subject.ensureCapacity(minimalCapacity);
		return this;
	}

	@Deprecated
	@Override
	public synchronized boolean equals(final Object o)
	{
		return this.subject.equals(o);
	}

	@Override
	public synchronized boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equals(samples, equalator);
	}

	@Override
	public synchronized boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equalsContent(samples, equalator);
	}

	@Override
	public synchronized <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.except(other, equalator, target);
	}


	@Override
	public synchronized <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		this.subject.iterate(procedure);
		return procedure;
	}

	@Override
	public final synchronized <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
	}

	@Deprecated
	@Override
	public synchronized int hashCode()
	{
		return this.subject.hashCode();
	}

	@Override
	public synchronized boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public synchronized <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.intersect(other, equalator, target);
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public synchronized Iterator<E> iterator()
	{
		return new SynchronizedIterator<>(this.subject.iterator());
	}

	@Override
	public synchronized E max(final Comparator<? super E> comparator)
	{
		return this.subject.max(comparator);
	}

	@Override
	public synchronized E min(final Comparator<? super E> comparator)
	{
		return this.subject.min(comparator);
	}

	@Override
	public synchronized <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.moveTo(target, predicate);
	}

	@Override
	public final synchronized <P extends Consumer<? super E>> P process(final P procedure)
	{
		return this.subject.process(procedure);
	}

	@Override
	public synchronized E fetch()
	{
		return this.subject.fetch();
	}

	@Override
	public synchronized E pinch()
	{
		return this.subject.pinch();
	}

	@Override
	public synchronized long removeBy(final Predicate<? super E> predicate)
	{
		return this.subject.removeBy(predicate);
	}

	@Override
	public synchronized E retrieve(final E element)
	{
		return this.subject.retrieve(element);
	}

	@Override
	public synchronized E retrieveBy(final Predicate<? super E> predicate)
	{
		return this.subject.retrieveBy(predicate);
	}

	@Override
	public synchronized boolean removeOne(final E element)
	{
		return this.subject.removeOne(element);
	}

	@Override
	public synchronized long remove(final E element)
	{
		return this.subject.remove(element);
	}


	@Override
	public synchronized long removeAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.removeAll(elements);
	}

	@Override
	public synchronized long removeDuplicates(final Equalator<? super E> equalator)
	{
		return this.subject.removeDuplicates(equalator);
	}

	@Override
	public synchronized long removeDuplicates()
	{
		return this.subject.removeDuplicates();
	}

	@Override
	public synchronized long nullRemove()
	{
		return this.subject.nullRemove();
	}

	@Override
	public synchronized long retainAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.retainAll(elements);
	}

	@Override
	public E seek(final E sample)
	{
		return this.subject.seek(sample);
	}

	@Override
	public synchronized E search(final Predicate<? super E> predicate)
	{
		return this.subject.search(predicate);
	}

	@Override
	public synchronized long optimize()
	{
		return this.subject.optimize();
	}

	@Override
	public synchronized long size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public synchronized Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public synchronized E[] toArray(final Class<E> type)
	{
		return this.subject.toArray(type);
	}

	@Override
	public synchronized void truncate()
	{
		this.subject.truncate();
	}

	@Override
	public synchronized <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.union(other, equalator, target);
	}

	@Override
	public synchronized long currentCapacity()
	{
		return this.subject.currentCapacity();
	}

	@Override
	public synchronized long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public synchronized boolean isFull()
	{
		return this.subject.isFull();
	}

	@Override
	public synchronized long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public synchronized SynchSet<E> copy()
	{
		return new SynchSet<>(this.subject.copy());
	}

	@Override
	public synchronized XImmutableSet<E> immure()
	{
		return this.subject.immure();
	}

	@Override
	public synchronized SetView<E> view()
	{
		return new SetView<>(this);
	}

	@Override
	public OldSynchSet<E> old()
	{
		return new OldSynchSet<>(this);
	}

	public static final class OldSynchSet<E> extends AbstractBridgeXSet<E>
	{
		OldSynchSet(final SynchSet<E> set)
		{
			super(set);
		}

		@Override
		public SynchSet<E> parent()
		{
			return (SynchSet<E>)super.parent();
		}

	}

}
