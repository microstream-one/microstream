package net.jadoth.collections;

import static net.jadoth.Jadoth.notNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XImmutableMap;
import net.jadoth.functional.BiProcedure;
import net.jadoth.util.Equalator;
import net.jadoth.util.KeyValue;

public final class MapView<K, V> implements XGettingMap<K, V>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final XGettingMap<K, V> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public MapView(final XGettingMap<K, V> subject)
	{
		super();
		this.subject = notNull(subject);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public long size()
	{
		return Jadoth.to_int(this.subject.size());
	}

	@Override
	public boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public Keys<K, V> keys()
	{
		return this.subject.keys();
	}

	@Override
	public Values<K, V> values()
	{
		return this.subject.values();
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
	public V get(final K key)
	{
		return this.subject.get(key);
	}

	@Override
	public V searchValue(final Predicate<? super K> keyPredicate)
	{
		return this.subject.searchValue(keyPredicate);
	}

	@Override
	public final <C extends Consumer<? super V>> C query(final XIterable<? extends K> keys, final C collector)
	{
		return this.subject.query(keys, collector);
	}

	@Override
	public XGettingMap<K, V> copy()
	{
		return this.subject.copy();
	}

	@Override
	public XImmutableMap<K, V> immure()
	{
		return this.subject.immure();
	}

	@Override
	public EntriesBridge<K, V> old()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME TableView#old
	}

	@Override
	public Bridge<K, V> oldMap()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME MapView#oldMap()
	}

	@Override
	public boolean nullKeyAllowed()
	{
		return this.subject.nullKeyAllowed();
	}

	@Override
	public boolean nullValuesAllowed()
	{
		return this.subject.nullValuesAllowed();
	}

	@Override
	public XGettingMap<K, V> view()
	{
		return this;
	}

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P iterate(final P procedure)
	{
		this.subject.iterate(procedure);
		return procedure;
	}

	@Override
	public <A> A join(final BiProcedure<? super KeyValue<K, V>, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
	}

	@Override
	public KeyValue<K, V> get()
	{
		return this.subject.get();
	}

	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		return this.subject.equals(o);
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
	public boolean equals(
		final XGettingCollection<? extends KeyValue<K, V>> samples  ,
		final Equalator<? super KeyValue<K, V>>            equalator
	)
	{
		return this.subject.equals(samples, equalator);
	}

	@Override
	public boolean equalsContent(
		final XGettingCollection<? extends KeyValue<K, V>> samples  ,
		final Equalator<? super KeyValue<K, V>>            equalator
	)
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

//	@Override
//	public boolean hasDistinctValues()
//	{
//		return this.subject.hasDistinctValues();
//	}
//
//	@Override
//	public boolean hasDistinctValues(final Equalator<? super KeyValue<K, V>> equalator)
//	{
//		return this.subject.hasDistinctValues(equalator);
//	}

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
	public <T extends Consumer<? super KeyValue<K, V>>> T distinct(
		final T                                 target   ,
		final Equalator<? super KeyValue<K, V>> equalator
	)
	{
		return this.subject.distinct(target, equalator);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T copyTo(final T target)
	{
		return this.subject.copyTo(target);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T filterTo(
		final T                                 target   ,
		final Predicate<? super KeyValue<K, V>> predicate
	)
	{
		return this.subject.filterTo(target, predicate);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T union(
		final XGettingCollection<? extends KeyValue<K, V>> other    ,
		final Equalator<? super KeyValue<K, V>>            equalator,
		final T                                            target
	)
	{
		return this.subject.union(other, equalator, target);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T intersect(
		final XGettingCollection<? extends KeyValue<K, V>> other    ,
		final Equalator<? super KeyValue<K, V>>            equalator,
		final T                                            target
	)
	{
		return this.subject.intersect(other, equalator, target);
	}

	@Override
	public <T extends Consumer<? super KeyValue<K, V>>> T except(
		final XGettingCollection<? extends KeyValue<K, V>> other    ,
		final Equalator<? super KeyValue<K, V>>            equalator,
		final T                                            target
	)
	{
		return this.subject.except(other, equalator, target);
	}

}
