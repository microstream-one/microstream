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

import one.microstream.collections.old.BridgeXCollection;
import one.microstream.collections.types.XCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XImmutableCollection;
import one.microstream.concurrency.Synchronized;
import one.microstream.equality.Equalator;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.SynchronizedIterator;


/**
 * Synchronization wrapper class that wraps an {@link XCollection} instance in public synchronized delegate methods.
 *
 */
public final class SynchCollection<E> implements XCollection<E>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XCollection<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link SynchCollection} that wraps the passed {@link XCollection} instance.
	 *
	 * @param collection the {@link XCollection} instance to be wrapped (synchronized).
	 */
	public SynchCollection(final XCollection<E> collection)
	{
		super();
		this.subject = collection;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final synchronized E get()
	{
		return this.subject.get();
	}

	@Override
	public final synchronized Equalator<? super E> equality()
	{
		return this.subject.equality();
	}

	@SafeVarargs
	@Override
	public final synchronized SynchCollection<E> addAll(final E... elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public final synchronized boolean nullAdd()
	{
		return this.subject.nullAdd();
	}

	@Override
	public final synchronized boolean add(final E e)
	{
		return this.subject.add(e);
	}

	@Override
	public final synchronized SynchCollection<E> addAll(final E[] elements, final int offset, final int length)
	{
		this.subject.addAll(elements, offset, length);
		return this;
	}

	@Override
	public final synchronized SynchCollection<E> addAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public final synchronized boolean nullPut()
	{
		return this.subject.nullPut();
	}

	@Override
	public final synchronized void accept(final E e)
	{
		this.subject.accept(e);
	}

	@Override
	public final synchronized boolean put(final E e)
	{
		return this.subject.put(e);
	}

	@SafeVarargs
	@Override
	public final synchronized SynchCollection<E> putAll(final E... elements)
	{
		this.subject.putAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchCollection<E> putAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.putAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchCollection<E> putAll(final E[] elements, final int offset, final int length)
	{
		this.subject.putAll(elements, offset, length);
		return this;
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
	public final synchronized SynchCollection<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		this.subject.ensureFreeCapacity(minimalFreeCapacity);
		return this;
	}

	@Override
	public final synchronized SynchCollection<E> ensureCapacity(final long minimalCapacity)
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
	public final synchronized boolean equals(
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator
	)
	{
		return this.subject.equals(this.subject, equalator);
	}

	@Override
	public final synchronized boolean equalsContent(
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator
	)
	{
		return this.subject.equalsContent(this.subject, equalator);
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
		return this.subject.iterate(procedure);
	}

	@Override
	public final synchronized <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
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
	public final synchronized Iterator<E> iterator()
	{
		return new SynchronizedIterator<>(this.subject.iterator());
	}

	@Override
	public final synchronized E max(final Comparator<? super E> comparator)
	{
		return this.subject.max(comparator);
	}

	@Override
	public final synchronized E min(final Comparator<? super E> comparator)
	{
		return this.subject.min(comparator);
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
	public final synchronized E fetch()
	{
		return this.subject.fetch();
	}

	@Override
	public final synchronized E pinch()
	{
		return this.subject.pinch();
	}

	@Override
	public final synchronized long removeBy(final Predicate<? super E> predicate)
	{
		return this.subject.removeBy(predicate);
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
	public final synchronized boolean removeOne(final E element)
	{
		return this.subject.removeOne(element);
	}


	@Override
	public final synchronized long remove(final E element)
	{
		return this.subject.remove(element);
	}


	@Override
	public final synchronized long removeAll(final XGettingCollection<? extends E> samples)
	{
		return this.subject.removeAll(this.subject);
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
	public final synchronized long retainAll(final XGettingCollection<? extends E> samples)
	{
		return this.subject.retainAll(this.subject);
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
	public final synchronized SynchCollection<E> copy()
	{
		return new SynchCollection<>(this.subject.copy());
	}

	@Override
	public final synchronized XImmutableCollection<E> immure()
	{
		return this.subject.immure();
	}

	@Override
	public final View<E> view()
	{
		return new View<>(this);
	}


	@Override
	public final OldSynchCollection<E> old()
	{
		return new OldSynchCollection<>(this);
	}

	public static final class OldSynchCollection<E> extends BridgeXCollection<E>
	{
		OldSynchCollection(final SynchCollection<E> set)
		{
			super(set);
		}

		@Override
		public final SynchCollection<E> parent()
		{
			return (SynchCollection<E>)super.parent();
		}

	}

}
