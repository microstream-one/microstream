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

import static one.microstream.X.notNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.interfaces.CapacityExtendable;
import one.microstream.collections.types.XCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XImmutableMap;
import one.microstream.collections.types.XIterable;
import one.microstream.collections.types.XMap;
import one.microstream.collections.types.XSet;
import one.microstream.concurrency.Synchronized;
import one.microstream.equality.Equalator;
import one.microstream.functional.Aggregator;
import one.microstream.typing.KeyValue;

public final class LockedMap<K, V> implements XMap<K, V>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static <K, V> LockedMap<K, V> New(final XMap<K, V> subject)
	{
		return New(subject, subject);
	}

	public static <K, V> LockedMap<K, V> New(final XMap<K, V> subject, final Object lock)
	{
		return new LockedMap<>(
			notNull(subject),
			notNull(lock)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final XMap<K, V> subject;
	final Object     lock   ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	LockedMap(final XMap<K, V> subject, final Object lock)
	{
		super();
		this.subject = subject;
		this.lock    = lock   ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final long maximumCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.maximumCapacity();
		}
	}

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P process(final P processor)
	{
		synchronized(this.lock)
		{
			return this.subject.process(processor);
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
	public final <A> A join(final BiConsumer<? super KeyValue<K, V>, ? super A> joiner, final A aggregate)
	{
		synchronized(this.lock)
		{
			return this.subject.join(joiner, aggregate);
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
	public final long optimize()
	{
		synchronized(this.lock)
		{
			return this.subject.optimize();
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
	public final void truncate()
	{
		synchronized(this.lock)
		{
			this.subject.truncate();
		}
	}

	@Override
	public final V removeFor(final K key)
	{
		synchronized(this.lock)
		{
			return this.subject.removeFor(key);
		}
	}

	@Override
	public final KeyValue<K, V> addGet(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.addGet(element);
		}
	}

	@Override
	public final KeyValue<K, V> deduplicate(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.deduplicate(element);
		}
	}

	@Override
	public final KeyValue<K, V> putGet(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.putGet(element);
		}
	}

	@Override
	public final KeyValue<K, V> replace(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.replace(element);
		}
	}

	@Override
	public final KeyValue<K, V> addGet(final K key, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.addGet(key, value);
		}
	}
	
	@Override
	public KeyValue<K, V> substitute(final K key, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.substitute(key, value);
		}
	}
	
	@Override
	public KeyValue<K, V> replace(final K key, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.replace(key, value);
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
	public final XMap.Keys<K, V> keys()
	{
		synchronized(this.lock)
		{
			return this.subject.keys();
		}
	}

	@Override
	public final V ensure(final K key, final Function<? super K, V> valueProvider)
	{
		synchronized(this.lock)
		{
			return this.subject.ensure(key, valueProvider);
		}
	}

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P iterate(final P procedure)
	{
		synchronized(this.lock)
		{
			return this.subject.iterate(procedure);
		}
	}

	@Override
	public final Aggregator<KeyValue<K, V>, ? extends XCollection<KeyValue<K, V>>> collector()
	{
		synchronized(this.lock)
		{
			return this.subject.collector();
		}
	}

	@Override
	public final XSet<KeyValue<K, V>> putAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.putAll(elements);
		}
	}

	@Override
	public final XMap.Values<K, V> values()
	{
		synchronized(this.lock)
		{
			return this.subject.values();
		}
	}

	@Override
	public final CapacityExtendable ensureCapacity(final long minimalCapacity)
	{
		synchronized(this.lock)
		{
			return this.subject.ensureCapacity(minimalCapacity);
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
	public final XMap.EntriesBridge<K, V> old()
	{
		synchronized(this.lock)
		{
			return this.subject.old();
		}
	}

	@Override
	public final XMap.Bridge<K, V> oldMap()
	{
		synchronized(this.lock)
		{
			return this.subject.oldMap();
		}
	}

	@Override
	public final CapacityExtendable ensureFreeCapacity(final long minimalFreeCapacity)
	{
		synchronized(this.lock)
		{
			return this.subject.ensureFreeCapacity(minimalFreeCapacity);
		}
	}

	@Override
	public final XMap<K, V> copy()
	{
		synchronized(this.lock)
		{
			return this.subject.copy();
		}
	}

	@Override
	public final <C extends Consumer<? super V>> C query(final XIterable<? extends K> keys, final C collector)
	{
		synchronized(this.lock)
		{
			return this.subject.query(keys, collector);
		}
	}

	@Override
	public final boolean put(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.put(element);
		}
	}

	@Override
	public final boolean nullKeyAllowed()
	{
		synchronized(this.lock)
		{
			return this.subject.nullKeyAllowed();
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
	public final boolean isFull()
	{
		synchronized(this.lock)
		{
			return this.subject.isFull();
		}
	}

	@Override
	public final boolean nullValuesAllowed()
	{
		synchronized(this.lock)
		{
			return this.subject.nullValuesAllowed();
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
	public final KeyValue<K, V> get()
	{
		synchronized(this.lock)
		{
			return this.subject.get();
		}
	}

	@Override
	public final boolean add(final K key, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.add(key, value);
		}
	}

	@Override
	public final KeyValue<K, V> fetch()
	{
		synchronized(this.lock)
		{
			return this.subject.fetch();
		}
	}

	@Override
	public final XGettingMap<K, V> view()
	{
		synchronized(this.lock)
		{
			return this.subject.view();
		}
	}

	@Override
	public final void forEach(final Consumer<? super KeyValue<K, V>> action)
	{
		synchronized(this.lock)
		{
			this.subject.forEach(action);
		}
	}

	@Override
	public final KeyValue<K, V> pinch()
	{
		synchronized(this.lock)
		{
			return this.subject.pinch();
		}
	}

	@Override
	public final boolean put(final K key, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.put(key, value);
		}
	}

	@Override
	public final Iterator<KeyValue<K, V>> iterator()
	{
		synchronized(this.lock)
		{
			return this.subject.iterator();
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
	public final KeyValue<K, V>[] toArray(final Class<KeyValue<K, V>> type)
	{
		synchronized(this.lock)
		{
			return this.subject.toArray(type);
		}
	}

	@Override
	public final KeyValue<K, V> retrieve(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.retrieve(element);
		}
	}

	@Override
	public final KeyValue<K, V> retrieveBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.retrieveBy(predicate);
		}
	}

	@Override
	public final long currentFreeCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.currentFreeCapacity();
		}
	}

	@Override
	public final boolean set(final K key, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.set(key, value);
		}
	}

	@Override
	public final long removeDuplicates(final Equalator<? super KeyValue<K, V>> equalator)
	{
		synchronized(this.lock)
		{
			return this.subject.removeDuplicates(equalator);
		}
	}

	@Override
	public final void accept(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			this.subject.accept(element);
		}
	}

	@Override
	public final long removeBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.removeBy(predicate);
		}
	}

	@Override
	public final boolean add(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.add(element);
		}
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C moveTo(
		final C                                 target   ,
		final Predicate<? super KeyValue<K, V>> predicate
	)
	{
		synchronized(this.lock)
		{
			return this.subject.moveTo(target, predicate);
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
	public final KeyValue<K, V> putGet(final K key, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.putGet(key, value);
		}
	}

	@Override
	public final KeyValue<K, V> setGet(final K key, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.setGet(key, value);
		}
	}

	@Override
	public final Spliterator<KeyValue<K, V>> spliterator()
	{
		synchronized(this.lock)
		{
			return this.subject.spliterator();
		}
	}

	@Override
	public final boolean valuePut(final K sampleKey, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.valuePut(sampleKey, value);
		}
	}

	@Override
	public final long size()
	{
		synchronized(this.lock)
		{
			return this.subject.size();
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
	public final Equalator<? super KeyValue<K, V>> equality()
	{
		synchronized(this.lock)
		{
			return this.subject.equality();
		}
	}

	@Override
	public final boolean equals(
		final XGettingCollection<? extends KeyValue<K, V>> samples,
		final Equalator<? super KeyValue<K, V>> equalator
	)
	{
		synchronized(this.lock)
		{
			return this.subject.equals(samples, equalator);
		}
	}

	@Override
	public final boolean valueSet(final K sampleKey, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.valueSet(sampleKey, value);
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
	public final boolean removeOne(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.removeOne(element);
		}
	}

	@Override
	public final V valuePutGet(final K sampleKey, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.valuePutGet(sampleKey, value);
		}
	}

	@Override
	public final boolean equalsContent(
		final XGettingCollection<? extends KeyValue<K, V>> samples,
		final Equalator<? super KeyValue<K, V>> equalator
	)
	{
		synchronized(this.lock)
		{
			return this.subject.equalsContent(samples, equalator);
		}
	}

	@Override
	public final long remove(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.remove(element);
		}
	}

	@Override
	public final long removeAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.removeAll(elements);
		}
	}

	@Override
	public final V valueSetGet(final K sampleKey, final V value)
	{
		synchronized(this.lock)
		{
			return this.subject.valueSetGet(sampleKey, value);
		}
	}

	@Override
	public final long retainAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.retainAll(elements);
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
	public final V get(final K key)
	{
		synchronized(this.lock)
		{
			return this.subject.get(key);
		}
	}
	
	@Override
	public final KeyValue<K, V> lookup(final K key)
	{
		synchronized(this.lock)
		{
			return this.subject.lookup(key);
		}
	}

	@Override
	public final V searchValue(final Predicate<? super K> keyPredicate)
	{
		synchronized(this.lock)
		{
			return this.subject.searchValue(keyPredicate);
		}
	}

	@Override
	public final XImmutableMap<K, V> immure()
	{
		synchronized(this.lock)
		{
			return this.subject.immure();
		}
	}

	@SafeVarargs
	@Override
	public final XMap<K, V> putAll(final KeyValue<K, V>... elements)
	{
		synchronized(this.lock)
		{
			return this.subject.putAll(elements);
		}
	}

	@Override
	public final XMap<K, V> putAll(final KeyValue<K, V>[] elements, final int srcStartIndex, final int srcLength)
	{
		synchronized(this.lock)
		{
			return this.subject.putAll(elements, srcStartIndex, srcLength);
		}
	}

	@SafeVarargs
	@Override
	public final XMap<K, V> addAll(final KeyValue<K, V>... elements)
	{
		synchronized(this.lock)
		{
			return this.subject.addAll(elements);
		}
	}

	@Override
	public final XMap<K, V> addAll(final KeyValue<K, V>[] elements, final int srcStartIndex, final int srcLength)
	{
		synchronized(this.lock)
		{
			return this.subject.addAll(elements, srcStartIndex, srcLength);
		}
	}

	@Override
	public final XMap<K, V> addAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.addAll(elements);
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
	public final boolean containsId(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.containsId(element);
		}
	}

	@Override
	public final boolean contains(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.contains(element);
		}
	}

	@Override
	public final boolean containsSearched(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.containsSearched(predicate);
		}
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.containsAll(elements);
		}
	}

	@Override
	public final boolean applies(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.applies(predicate);
		}
	}

	@Override
	public final long count(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.count(element);
		}
	}

	@Override
	public final long countBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.countBy(predicate);
		}
	}

	@Override
	public final KeyValue<K, V> search(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.search(predicate);
		}
	}

	@Override
	public final KeyValue<K, V> seek(final KeyValue<K, V> sample)
	{
		synchronized(this.lock)
		{
			return this.subject.seek(sample);
		}
	}

	@Override
	public final KeyValue<K, V> max(final Comparator<? super KeyValue<K, V>> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.max(comparator);
		}
	}

	@Override
	public final KeyValue<K, V> min(final Comparator<? super KeyValue<K, V>> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.min(comparator);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T distinct(final T target)
	{
		synchronized(this.lock)
		{
			return this.subject.distinct(target);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T distinct(
		final T target,
		final Equalator<? super KeyValue<K, V>> equalator
	)
	{
		synchronized(this.lock)
		{
			return this.subject.distinct(target, equalator);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T copyTo(final T target)
	{
		synchronized(this.lock)
		{
			return this.subject.copyTo(target);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T filterTo(
		final T target,
		final Predicate<? super KeyValue<K, V>> predicate
	)
	{
		synchronized(this.lock)
		{
			return this.subject.filterTo(target, predicate);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T union(
		final XGettingCollection<? extends KeyValue<K, V>> other    ,
		final Equalator<? super KeyValue<K, V>>            equalator,
		final T                                            target
	)
	{
		synchronized(this.lock)
		{
			return this.subject.union(other, equalator, target);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T intersect(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final T target
	)
	{
		synchronized(this.lock)
		{
			return this.subject.intersect(other, equalator, target);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T except(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final T target
	)
	{
		synchronized(this.lock)
		{
			return this.subject.except(other, equalator, target);
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

	@Deprecated
	@Override
	public final int hashCode()
	{
		synchronized(this.lock)
		{
			return this.subject.hashCode();
		}
	}

}
