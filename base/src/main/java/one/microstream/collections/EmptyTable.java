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
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.old.OldList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XImmutableTable;
import one.microstream.collections.types.XIterable;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.KeyValue;

// (17.09.2013 TM)FIXME: EmptyTable: implement all FIXME
public final class EmptyTable<K, V> implements XImmutableTable<K, V>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Keys   keys   = new Keys();
	private final Values values = new Values();



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final V get(final K key)
	{
		return null;
	}
	
	@Override
	public final KeyValue<K, V> lookup(final K key)
	{
		return null;
	}

	@Override
	public final V searchValue(final Predicate<? super K> keyPredicate)
	{
		return null;
	}

	@Override
	public final <C extends Consumer<? super V>> C query(final XIterable<? extends K> keys, final C collector)
	{
		return collector;
	}

	@Override
	public final EmptyTable<K, V> copy()
	{
		return new EmptyTable<>();
	}

	@Override
	public final XImmutableTable<K, V> immure()
	{
		return this;
	}

	@Override
	public final boolean nullKeyAllowed()
	{
		return true;
	}

	@Override
	public final boolean nullValuesAllowed()
	{
		return true;
	}

	@Override
	public final long maximumCapacity()
	{
		return 0;
	}

	@Override
	public final long remainingCapacity()
	{
		return 0;
	}

	@Override
	public final boolean isFull()
	{
		return true;
	}

	@Override
	public final long size()
	{
		return 0;
	}

	@Override
	public final boolean isEmpty()
	{
		return true;
	}

	@Override
	public final boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public final boolean nullAllowed()
	{
		return true;
	}

	@Override
	public final EmptyTable<K, V> view()
	{
		return this;
	}

	@Override
	public final XImmutableTable.EntriesBridge<K, V> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingMap<K, V>#old
	}

	@Override
	public XImmutableTable.Bridge<K, V> oldMap()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EmptyTable#oldMap()
	}

	@Override
	public final XImmutableTable.Keys<K, V> keys()
	{
		return this.keys;
	}

	@Override
	public final XImmutableTable.Values<K, V> values()
	{
		return this.values;
	}

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P iterate(final P procedure)
	{
		return procedure;
	}

	@Override
	public KeyValue<K, V> get()
	{
		throw new NullPointerException();
	}

	@Override
	public Iterator<KeyValue<K, V>> iterator()
	{
		return new Iterator<KeyValue<K, V>>()
		{
			@Override
			public boolean hasNext()
			{
				return false;
			}

			@Override
			public KeyValue<K, V> next()
			{
				throw new NoSuchElementException();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public Object[] toArray()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#toArray()
	}

	@Override
	public KeyValue<K, V>[] toArray(final Class<KeyValue<K, V>> type)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#toArray()
	}

	@Override
	public Equalator<? super KeyValue<K, V>> equality()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#equality()
	}

	@Override
	public boolean equals(final XGettingCollection<? extends KeyValue<K, V>> samples, final Equalator<? super KeyValue<K, V>> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#equals()
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends KeyValue<K, V>> samples, final Equalator<? super KeyValue<K, V>> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#equalsContent()
	}

	@Override
	public boolean nullContained()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#nullContained()
	}

	@Override
	public boolean containsId(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#containsId()
	}

	@Override
	public boolean contains(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#contains()
	}

	@Override
	public boolean containsSearched(final Predicate<? super KeyValue<K, V>> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#contains()
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#containsAll()
	}

	@Override
	public boolean applies(final Predicate<? super KeyValue<K, V>> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#applies()
	}

	@Override
	public long count(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#count()
	}

	@Override
	public long countBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#count()
	}

	@Override
	public KeyValue<K, V> search(final Predicate<? super KeyValue<K, V>> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#search()
	}

	@Override
	public KeyValue<K, V> seek(final KeyValue<K, V> sample)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#seek()
	}

	@Override
	public KeyValue<K, V> max(final Comparator<? super KeyValue<K, V>> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#max()
	}

	@Override
	public KeyValue<K, V> min(final Comparator<? super KeyValue<K, V>> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#min()
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T distinct(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#distinct()
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T distinct(final T target, final Equalator<? super KeyValue<K, V>> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#distinct()
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T copyTo(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#copyTo()
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T filterTo(final T target, final Predicate<? super KeyValue<K, V>> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#filterTo()
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T union(final XGettingCollection<? extends KeyValue<K, V>> other, final Equalator<? super KeyValue<K, V>> equalator, final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#union()
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T intersect(final XGettingCollection<? extends KeyValue<K, V>> other, final Equalator<? super KeyValue<K, V>> equalator, final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#intersect()
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T except(final XGettingCollection<? extends KeyValue<K, V>> other, final Equalator<? super KeyValue<K, V>> equalator, final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<KeyValue<K, V>>#except()
	}

	@Override
	public <A> A join(final BiConsumer<? super KeyValue<K, V>, ? super A> joiner, final A aggregate)
	{
		return aggregate;
	}

	@Override
	public XImmutableEnum<KeyValue<K, V>> toReversed()
	{
		return this;
	}

	@Override
	public XGettingEnum<KeyValue<K, V>> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingEnum<KeyValue<K, V>>#view()
	}

	@Override
	public XGettingEnum<KeyValue<K, V>> range(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingEnum<KeyValue<K, V>>#range()
	}

	@Override
	public KeyValue<K, V> at(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#get()
	}

	@Override
	public KeyValue<K, V> first()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#first()
	}

	@Override
	public KeyValue<K, V> last()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#last()
	}

	@Override
	public KeyValue<K, V> poll()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#poll()
	}

	@Override
	public KeyValue<K, V> peek()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#peek()
	}

	@Override
	public long maxIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#maxIndex()
	}

	@Override
	public long minIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#minIndex()
	}

	@Override
	public long indexOf(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#indexOf()
	}

	@Override
	public long indexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#indexOf()
	}

	@Override
	public long lastIndexOf(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#lastIndexOf()
	}

	@Override
	public long lastIndexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#lastIndexOf()
	}

	@Override
	public long scan(final Predicate<? super KeyValue<K, V>> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#scan()
	}

	@Override
	public boolean isSorted(final Comparator<? super KeyValue<K, V>> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#isSorted()
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T copySelection(final T target, final long... indices)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<KeyValue<K, V>>#copySelection()
	}

	@Override
	public <P extends IndexedAcceptor<? super KeyValue<K, V>>> P iterateIndexed(final P procedure)
	{
		return procedure;
	}

	final class Values implements XImmutableTable.Values<K, V>
	{
		@Override
		public final long maximumCapacity()
		{
			return X.<V>empty().maximumCapacity();
		}

		@Override
		public final boolean nullAllowed()
		{
			return X.<V>empty().nullAllowed();
		}

		@Override
		public final <P extends Consumer<? super V>> P iterate(final P procedure)
		{
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super V, ? super A> joiner, final A aggregate)
		{
			return aggregate;
		}

		@Override
		public final XImmutableList<V> copy()
		{
			return X.<V>empty().copy();
		}

		@Override
		public final long remainingCapacity()
		{
			return X.<V>empty().remainingCapacity();
		}

		@Override
		public final boolean isFull()
		{
			return X.<V>empty().isFull();
		}

		@Override
		public final ListIterator<V> listIterator()
		{
			return X.<V>empty().listIterator();
		}

		@Override
		public final XImmutableList<V> immure()
		{
			return X.<V>empty().immure();
		}

		@Override
		public final ListIterator<V> listIterator(final long index)
		{
			return X.<V>empty().listIterator(index);
		}

		@Override
		public final XImmutableList<V> toReversed()
		{
			return X.<V>empty().toReversed();
		}

		@Override
		public final <P extends IndexedAcceptor<? super V>> P iterateIndexed(final P procedure)
		{
			return X.<V>empty().iterateIndexed(procedure);
		}

		@Override
		public final XImmutableList<V> range(final long fromIndex, final long toIndex)
		{
			return X.<V>empty().range(fromIndex, toIndex);
		}

		@Override
		public final V get()
		{
			return X.<V>empty().get();
		}

		@Override
		public final XImmutableList<V> view()
		{
			return X.<V>empty().view();
		}

		@Override
		public final XImmutableList<V> view(final long lowIndex, final long highIndex)
		{
			return X.<V>empty().view(lowIndex, highIndex);
		}

		@Override
		public final boolean equals(final Object o)
		{
			return X.<V>empty().equals(o);
		}

		@Override
		public final V at(final long index)
		{
			return X.<V>empty().at(index);
		}

		@Override
		public final V first()
		{
			return X.<V>empty().first();
		}

		@Override
		public final V last()
		{
			return X.<V>empty().last();
		}

		@Override
		public final V poll()
		{
			return X.<V>empty().poll();
		}

		@Override
		public final V peek()
		{
			return X.<V>empty().peek();
		}

		@Override
		public final long maxIndex(final Comparator<? super V> comparator)
		{
			return X.<V>empty().maxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super V> comparator)
		{
			return X.<V>empty().minIndex(comparator);
		}

		@Override
		public final long indexOf(final V element)
		{
			return X.<V>empty().indexOf(element);
		}

		@Override
		public final long indexBy(final Predicate<? super V> predicate)
		{
			return X.<V>empty().indexBy(predicate);
		}

		@Override
		public final long lastIndexOf(final V element)
		{
			return X.<V>empty().lastIndexOf(element);
		}

		@Override
		public final long lastIndexBy(final Predicate<? super V> predicate)
		{
			return X.<V>empty().lastIndexBy(predicate);
		}

		@Override
		public final long scan(final Predicate<? super V> predicate)
		{
			return X.<V>empty().scan(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super V> comparator)
		{
			return X.<V>empty().isSorted(comparator);
		}

		@Override
		public final int hashCode()
		{
			return X.<V>empty().hashCode();
		}

		@Override
		public final <T extends Consumer<? super V>> T copySelection(final T target, final long... indices)
		{
			return X.<V>empty().copySelection(target, indices);
		}

		@Override
		public final Iterator<V> iterator()
		{
			return X.<V>empty().iterator();
		}

		@Override
		public final Object[] toArray()
		{
			return X.<V>empty().toArray();
		}

		@Override
		public final V[] toArray(final Class<V> type)
		{
			return X.<V>empty().toArray(type);
		}

		@Override
		public final boolean hasVolatileElements()
		{
			return X.<V>empty().hasVolatileElements();
		}

		@Override
		public final long size()
		{
			return X.<V>empty().size();
		}

		@Override
		public final boolean isEmpty()
		{
			return X.<V>empty().isEmpty();
		}

		@Override
		public final Equalator<? super V> equality()
		{
			return X.<V>empty().equality();
		}

		@Override
		public final boolean equals(final XGettingCollection<? extends V> samples, final Equalator<? super V> equalator)
		{
			return X.<V>empty().equals(samples, equalator);
		}

		@Override
		public final boolean equalsContent(final XGettingCollection<? extends V> samples, final Equalator<? super V> equalator)
		{
			return X.<V>empty().equalsContent(samples, equalator);
		}

		@Override
		public final boolean nullContained()
		{
			return X.<V>empty().nullContained();
		}

		@Override
		public final boolean containsId(final V element)
		{
			return X.<V>empty().containsId(element);
		}

		@Override
		public final boolean contains(final V element)
		{
			return X.<V>empty().contains(element);
		}

		@Override
		public final boolean containsSearched(final Predicate<? super V> predicate)
		{
			return X.<V>empty().containsSearched(predicate);
		}

		@Override
		public final boolean containsAll(final XGettingCollection<? extends V> elements)
		{
			return X.<V>empty().containsAll(elements);
		}

		@Override
		public final boolean applies(final Predicate<? super V> predicate)
		{
			return X.<V>empty().applies(predicate);
		}

		@Override
		public final long count(final V element)
		{
			return X.<V>empty().count(element);
		}

		@Override
		public final long countBy(final Predicate<? super V> predicate)
		{
			return X.<V>empty().countBy(predicate);
		}

		@Override
		public final V search(final Predicate<? super V> predicate)
		{
			return X.<V>empty().search(predicate);
		}

		@Override
		public final V seek(final V sample)
		{
			return X.<V>empty().seek(sample);
		}

		@Override
		public final V max(final Comparator<? super V> comparator)
		{
			return X.<V>empty().max(comparator);
		}

		@Override
		public final V min(final Comparator<? super V> comparator)
		{
			return X.<V>empty().min(comparator);
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target)
		{
			return X.<V>empty().distinct(target);
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target, final Equalator<? super V> equalator)
		{
			return X.<V>empty().distinct(target, equalator);
		}

		@Override
		public final <T extends Consumer<? super V>> T copyTo(final T target)
		{
			return X.<V>empty().iterate(target);
		}

		@Override
		public final <T extends Consumer<? super V>> T filterTo(final T target, final Predicate<? super V> predicate)
		{
			return X.<V>empty().filterTo(target, predicate);
		}

		@Override
		public final <T extends Consumer<? super V>> T union(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			return X.<V>empty().union(other, equalator, target);
		}

		@Override
		public final <T extends Consumer<? super V>> T intersect(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			return X.<V>empty().intersect(other, equalator, target);
		}

		@Override
		public final <T extends Consumer<? super V>> T except(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			return X.<V>empty().except(other, equalator, target);
		}

		@Override
		public final OldList<V> old()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<V>#old()
		}

		@Override
		public final EmptyTable<K, V> parent()
		{
			return EmptyTable.this;
		}
	}

	final class Keys implements XImmutableTable.Keys<K, V>
	{
		@Override
		public final long maximumCapacity()
		{
			return X.<K>empty().maximumCapacity();
		}

		@Override
		public final boolean nullAllowed()
		{
			return X.<K>empty().nullAllowed();
		}

		@Override
		public final <P extends Consumer<? super K>> P iterate(final P procedure)
		{
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super K, ? super A> joiner, final A aggregate)
		{
			return aggregate;
		}

		@Override
		public final XImmutableEnum<K> copy()
		{
			return X.<K>empty().copy();
		}

		@Override
		public final long remainingCapacity()
		{
			return X.<K>empty().remainingCapacity();
		}

		@Override
		public final boolean isFull()
		{
			return X.<K>empty().isFull();
		}

		@Override
		public final XImmutableEnum<K> immure()
		{
			return X.<K>empty().immure();
		}

		@Override
		public final XImmutableEnum<K> toReversed()
		{
			return X.<K>empty().toReversed();
		}

		@Override
		public final <P extends IndexedAcceptor<? super K>> P iterateIndexed(final P procedure)
		{
			return X.<K>empty().iterateIndexed(procedure);
		}

		@Override
		public final XImmutableEnum<K> range(final long fromIndex, final long toIndex)
		{
			return X.<K>empty().range(fromIndex, toIndex);
		}

		@Override
		public final K get()
		{
			return X.<K>empty().get();
		}

		@Override
		public final XImmutableEnum<K> view()
		{
			return X.<K>empty().view();
		}

		@Override
		public final XImmutableEnum<K> view(final long lowIndex, final long highIndex)
		{
			return X.<K>empty().view(lowIndex, highIndex);
		}

		@Override
		public final boolean equals(final Object o)
		{
			return X.<K>empty().equals(o);
		}

		@Override
		public final K at(final long index)
		{
			return X.<K>empty().at(index);
		}

		@Override
		public final K first()
		{
			return X.<K>empty().first();
		}

		@Override
		public final K last()
		{
			return X.<K>empty().last();
		}

		@Override
		public final K poll()
		{
			return X.<K>empty().poll();
		}

		@Override
		public final K peek()
		{
			return X.<K>empty().peek();
		}

		@Override
		public final long maxIndex(final Comparator<? super K> comparator)
		{
			return X.<K>empty().maxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super K> comparator)
		{
			return X.<K>empty().minIndex(comparator);
		}

		@Override
		public final long indexOf(final K element)
		{
			return X.<K>empty().indexOf(element);
		}

		@Override
		public final long indexBy(final Predicate<? super K> predicate)
		{
			return X.<K>empty().indexBy(predicate);
		}

		@Override
		public final long lastIndexOf(final K element)
		{
			return X.<K>empty().lastIndexOf(element);
		}

		@Override
		public final long lastIndexBy(final Predicate<? super K> predicate)
		{
			return X.<K>empty().lastIndexBy(predicate);
		}

		@Override
		public final long scan(final Predicate<? super K> predicate)
		{
			return X.<K>empty().scan(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super K> comparator)
		{
			return X.<K>empty().isSorted(comparator);
		}

		@Override
		public final int hashCode()
		{
			return X.<K>empty().hashCode();
		}

		@Override
		public final <T extends Consumer<? super K>> T copySelection(final T target, final long... indices)
		{
			return X.<K>empty().copySelection(target, indices);
		}

		@Override
		public final Iterator<K> iterator()
		{
			return X.<K>empty().iterator();
		}

		@Override
		public final Object[] toArray()
		{
			return X.<K>empty().toArray();
		}

		@Override
		public final K[] toArray(final Class<K> type)
		{
			return X.<K>empty().toArray(type);
		}

		@Override
		public final boolean hasVolatileElements()
		{
			return X.<K>empty().hasVolatileElements();
		}

		@Override
		public final long size()
		{
			return X.<K>empty().size();
		}

		@Override
		public final boolean isEmpty()
		{
			return X.<K>empty().isEmpty();
		}

		@Override
		public final Equalator<? super K> equality()
		{
			return X.<K>empty().equality();
		}

		@Override
		public final boolean equals(final XGettingCollection<? extends K> samples, final Equalator<? super K> equalator)
		{
			return X.<K>empty().equals(samples, equalator);
		}

		@Override
		public final boolean equalsContent(final XGettingCollection<? extends K> samples, final Equalator<? super K> equalator)
		{
			return X.<K>empty().equalsContent(samples, equalator);
		}

		@Override
		public final boolean nullContained()
		{
			return X.<K>empty().nullContained();
		}

		@Override
		public final boolean containsId(final K element)
		{
			return X.<K>empty().containsId(element);
		}

		@Override
		public final boolean contains(final K element)
		{
			return X.<K>empty().contains(element);
		}

		@Override
		public final boolean containsSearched(final Predicate<? super K> predicate)
		{
			return X.<K>empty().containsSearched(predicate);
		}

		@Override
		public final boolean containsAll(final XGettingCollection<? extends K> elements)
		{
			return X.<K>empty().containsAll(elements);
		}

		@Override
		public final boolean applies(final Predicate<? super K> predicate)
		{
			return X.<K>empty().applies(predicate);
		}

		@Override
		public final long count(final K element)
		{
			return X.<K>empty().count(element);
		}

		@Override
		public final long countBy(final Predicate<? super K> predicate)
		{
			return X.<K>empty().countBy(predicate);
		}

		@Override
		public final K search(final Predicate<? super K> predicate)
		{
			return X.<K>empty().search(predicate);
		}

		@Override
		public final K seek(final K sample)
		{
			return X.<K>empty().seek(sample);
		}

		@Override
		public final K max(final Comparator<? super K> comparator)
		{
			return X.<K>empty().max(comparator);
		}

		@Override
		public final K min(final Comparator<? super K> comparator)
		{
			return X.<K>empty().min(comparator);
		}

		@Override
		public final <T extends Consumer<? super K>> T distinct(final T target)
		{
			return X.<K>empty().distinct(target);
		}

		@Override
		public final <T extends Consumer<? super K>> T distinct(final T target, final Equalator<? super K> equalator)
		{
			return target;
		}

		@Override
		public final <T extends Consumer<? super K>> T copyTo(final T target)
		{
			return target;
		}

		@Override
		public final <T extends Consumer<? super K>> T filterTo(final T target, final Predicate<? super K> predicate)
		{
			return target;
		}

		@Override
		public final <T extends Consumer<? super K>> T union(final XGettingCollection<? extends K> other, final Equalator<? super K> equalator, final T target)
		{
			return X.<K>empty().union(other, equalator, target);
		}

		@Override
		public final <T extends Consumer<? super K>> T intersect(final XGettingCollection<? extends K> other, final Equalator<? super K> equalator, final T target)
		{
			return X.<K>empty().intersect(other, equalator, target);
		}

		@Override
		public final <T extends Consumer<? super K>> T except(final XGettingCollection<? extends K> other, final Equalator<? super K> equalator, final T target)
		{
			return X.<K>empty().except(other, equalator, target);
		}

		@Override
		public final OldList<K> old()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<K>#old()
		}

		@Override
		public final EmptyTable<K, V> parent()
		{
			return EmptyTable.this;
		}
	}

}
