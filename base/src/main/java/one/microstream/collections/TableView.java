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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableTable;
import one.microstream.collections.types.XIterable;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;

public final class TableView<K, V> implements XGettingTable<K, V>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final XGettingTable<K, V> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@Override
	public <A> A join(final BiConsumer<? super KeyValue<K, V>, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
	}

	@Override
	public <P extends IndexedAcceptor<? super KeyValue<K, V>>> P iterateIndexed(final P procedure)
	{
		return this.subject.iterateIndexed(procedure);
	}

	@Override
	public XGettingEnum<KeyValue<K, V>> toReversed()
	{
		return this.subject.toReversed();
	}

	@Override
	public XGettingEnum<KeyValue<K, V>> view(final long lowIndex, final long highIndex)
	{
		return this.subject.view(lowIndex, highIndex);
	}

	@Override
	public XGettingEnum<KeyValue<K, V>> range(final long lowIndex, final long highIndex)
	{
		return this.subject.range(lowIndex, highIndex);
	}

	@Override
	public KeyValue<K, V> get()
	{
		return this.subject.get();
	}
	
	@Override
	public KeyValue<K, V> lookup(final K key)
	{
		return this.subject.lookup(key);
	}

	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		return this.subject.equals(o);
	}

	@Override
	public KeyValue<K, V> at(final long index)
	{
		return this.subject.at(index);
	}

	@Override
	public KeyValue<K, V> first()
	{
		return this.subject.first();
	}

	@Override
	public KeyValue<K, V> last()
	{
		return this.subject.last();
	}

	@Override
	public KeyValue<K, V> poll()
	{
		return this.subject.poll();
	}

	@Override
	public KeyValue<K, V> peek()
	{
		return this.subject.peek();
	}

	@Override
	public long maxIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return this.subject.maxIndex(comparator);
	}

	@Override
	public long minIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return this.subject.minIndex(comparator);
	}

	@Override
	public long indexOf(final KeyValue<K, V> element)
	{
		return this.subject.indexOf(element);
	}

	@Override
	public long indexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.subject.indexBy(predicate);
	}

	@Override
	public long lastIndexOf(final KeyValue<K, V> element)
	{
		return this.subject.lastIndexOf(element);
	}

	@Override
	public long lastIndexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.subject.lastIndexBy(predicate);
	}

	@Override
	public long scan(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.subject.scan(predicate);
	}

	@Override
	public boolean isSorted(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return this.subject.isSorted(comparator);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T copySelection(final T target, final long... indices)
	{
		return this.subject.copySelection(target, indices);
	}

	@Deprecated
	@Override
	public int hashCode()
	{
		return this.subject.hashCode();
	}

	@Override
	public Iterator<KeyValue<K, V>> iterator()
	{
		return this.subject.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public KeyValue<K, V>[] toArray(final Class<KeyValue<K, V>> type)
	{
		return this.subject.toArray(type);
	}

	@Override
	public Equalator<? super KeyValue<K, V>> equality()
	{
		return this.subject.equality();
	}

	@Override
	public boolean equals(final XGettingCollection<? extends KeyValue<K, V>> samples, final Equalator<? super KeyValue<K, V>> equalator)
	{
		return this.subject.equals(samples, equalator);
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends KeyValue<K, V>> samples, final Equalator<? super KeyValue<K, V>> equalator)
	{
		return this.subject.equalsContent(samples, equalator);
	}

	@Override
	public boolean nullContained()
	{
		return this.subject.nullContained();
	}

	@Override
	public boolean containsId(final KeyValue<K, V> element)
	{
		return this.subject.containsId(element);
	}

	@Override
	public boolean contains(final KeyValue<K, V> element)
	{
		return this.subject.contains(element);
	}

	@Override
	public boolean containsSearched(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.subject.containsSearched(predicate);
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		return this.subject.containsAll(elements);
	}

	@Override
	public boolean applies(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.subject.applies(predicate);
	}

	@Override
	public long count(final KeyValue<K, V> element)
	{
		return this.subject.count(element);
	}

	@Override
	public long countBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.subject.countBy(predicate);
	}

	@Override
	public KeyValue<K, V> search(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.subject.search(predicate);
	}

	@Override
	public KeyValue<K, V> seek(final KeyValue<K, V> sample)
	{
		return this.subject.seek(sample);
	}

	@Override
	public KeyValue<K, V> max(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return this.subject.max(comparator);
	}

	@Override
	public KeyValue<K, V> min(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return this.subject.min(comparator);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T distinct(final T target)
	{
		return this.subject.distinct(target);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T distinct(final T target, final Equalator<? super KeyValue<K, V>> equalator)
	{
		return this.subject.distinct(target, equalator);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T copyTo(final T target)
	{
		return this.subject.copyTo(target);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T filterTo(final T target, final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.subject.filterTo(target, predicate);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T union(final XGettingCollection<? extends KeyValue<K, V>> other, final Equalator<? super KeyValue<K, V>> equalator, final T target)
	{
		return this.subject.union(other, equalator, target);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T intersect(final XGettingCollection<? extends KeyValue<K, V>> other, final Equalator<? super KeyValue<K, V>> equalator, final T target)
	{
		return this.subject.intersect(other, equalator, target);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T except(final XGettingCollection<? extends KeyValue<K, V>> other, final Equalator<? super KeyValue<K, V>> equalator, final T target)
	{
		return this.subject.except(other, equalator, target);
	}

	public TableView(final XGettingTable<K, V> subject)
	{
		super();
		this.subject = notNull(subject);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public final long size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public final boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public final boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public final boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public final Keys<K, V> keys()
	{
		return this.subject.keys();
	}

	@Override
	public final Values<K, V> values()
	{
		return this.subject.values();
	}

	@Override
	public final long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public final boolean isFull()
	{
		return this.subject.isFull();
	}

	@Override
	public final V get(final K key)
	{
		return this.subject.get(key);
	}

	@Override
	public final V searchValue(final Predicate<? super K> keyPredicate)
	{
		return this.subject.searchValue(keyPredicate);
	}

	@Override
	public final <C extends Consumer<? super V>> C query(final XIterable<? extends K> keys, final C collector)
	{
		return this.subject.query(keys, collector);
	}

	@Override
	public final XGettingTable<K, V> copy()
	{
		return this.subject.copy();
	}

	@Override
	public final XImmutableTable<K, V> immure()
	{
		return this.subject.immure();
	}

	@Override
	public final EntriesBridge<K, V> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME TableView#old
	}

	@Override
	public Bridge<K, V> oldMap()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME TableView#oldMap()
	}

	@Override
	public final boolean nullKeyAllowed()
	{
		return this.subject.nullKeyAllowed();
	}

	@Override
	public final boolean nullValuesAllowed()
	{
		return this.subject.nullValuesAllowed();
	}

	@Override
	public final TableView<K, V> view()
	{
		return this;
	}

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P iterate(final P procedure)
	{
		return this.subject.iterate(procedure);
	}

	@Override
	public final String toString()
	{
		return this.subject.toString();
	}

}
