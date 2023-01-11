
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


public final class LockedCollection<E> implements XCollection<E>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XCollection<E> subject;
	private final Object         lock   ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LockedCollection(final XCollection<E> collection)
	{
		super();
		this.subject = collection;
		this.lock   = collection;
	}

	public LockedCollection(final XCollection<E> collection, final Object lock)
	{
		super();
		this.subject = collection;
		this.lock    = lock      ;
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

	@SafeVarargs
	@Override
	public final LockedCollection<E> addAll(final E... elements)
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

	@Override
	public final boolean add(final E e)
	{
		synchronized(this.lock)
		{
			return this.subject.add(e);
		}
	}

	@Override
	public final LockedCollection<E> addAll(final E[] elements, final int offset, final int length)
	{
		synchronized(this.lock)
		{
			this.subject.addAll(elements, offset, length);
			return this;
		}
	}

	@Override
	public final LockedCollection<E> addAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			this.subject.addAll(elements);
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

	@Override
	public final void accept(final E e)
	{
		synchronized(this.lock)
		{
			this.subject.accept(e);
		}
	}

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
	public final LockedCollection<E> putAll(final E... elements)
	{
		synchronized(this.lock)
		{
			this.subject.putAll(elements);
			return this;
		}
	}

	@Override
	public final LockedCollection<E> putAll(final E[] elements, final int offset, final int length)
	{
		synchronized(this.lock)
		{
			this.subject.putAll(elements, offset, length);
			return this;
		}
	}

	@Override
	public final LockedCollection<E> putAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			this.subject.putAll(elements);
			return this;
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
	public final LockedCollection<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		synchronized(this.lock)
		{
			this.subject.ensureFreeCapacity(minimalFreeCapacity);
			return this;
		}
	}

	@Override
	public final LockedCollection<E> ensureCapacity(final long minimalCapacity)
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
			return this.subject.equals(this.subject, equalator);
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
			return this.subject.equalsContent(this.subject, equalator);
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
	public final Iterator<E> iterator()
	{
		synchronized(this.lock)
		{
			return new SynchronizedIterator<>(this.subject.iterator());
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
	public final E min(final Comparator<? super E> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.min(comparator);
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
	public final E fetch()
	{
		synchronized(this.lock)
		{
			return this.subject.fetch();
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
	public final long removeBy(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.removeBy(predicate);
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
	public final E retrieveBy(final Predicate<? super E> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.retrieveBy(predicate);
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
	public final long remove(final E element)
	{
		synchronized(this.lock)
		{
			return this.subject.remove(element);
		}
	}

	@Override
	public final long removeAll(final XGettingCollection<? extends E> samples)
	{
		synchronized(this.lock)
		{
			return this.subject.removeAll(this.subject);
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
	public final long retainAll(final XGettingCollection<? extends E> samples)
	{
		synchronized(this.lock)
		{
			return this.subject.retainAll(this.subject);
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
	public final LockedCollection<E> copy()
	{
		synchronized(this.lock)
		{
			return new LockedCollection<>(this.subject.copy(), new Object());
		}
	}

	@Override
	public final XImmutableCollection<E> immure()
	{
		synchronized(this.lock)
		{
			return this.subject.immure();
		}
	}

	@Override
	public final View<E> view()
	{
		return new View<>(this);
	}

	@Override
	public final OldMutexCollection<E> old()
	{
		return new OldMutexCollection<>(this);
	}

	public static final class OldMutexCollection<E> extends BridgeXCollection<E>
	{
		OldMutexCollection(final LockedCollection<E> set)
		{
			super(set);
		}

		@Override
		public final LockedCollection<E> parent()
		{
			return (LockedCollection<E>)super.parent();
		}

	}

}
