package one.microstream.collections.types;

import java.util.function.Predicate;

import one.microstream.typing.KeyValue;


/**
 * 
 *
 */
public interface XImmutableMap<K, V> extends XGettingMap<K, V>, XImmutableSet<KeyValue<K, V>>
{
	// key to value querying
	@Override
	public V get(K key);

	@Override
	public V searchValue(Predicate<? super K> keyPredicate);

	// satellite instances

	@Override
	public Keys<K, V> keys();

	@Override
	public Values<K, V> values();


	@Override
	public XImmutableMap<K, V> copy();

	/**
	 * Provides an instance of an immutable collection type with equal behavior and data as this instance.
	 * <p>
	 * If this instance already is of an immutable collection type, it returns itself.
	 *
	 * @return an immutable copy of this collection instance.
	 */
	@Override
	public XImmutableTable<K, V> immure();

	@Override
	public EntriesBridge<K, V> old();

	@Override
	public Bridge<K, V> oldMap();

	// null handling characteristics information

	@Override
	public boolean nullKeyAllowed();
	@Override
	public boolean nullValuesAllowed();


	///////////////////////////////////////////////////////////////////////////
	// satellite types //
	////////////////////

	public interface Satellite<K, V>
	{
		public XImmutableMap<K, V> parent();

	}

	public interface Values<K, V> extends XGettingMap.Values<K, V>, XImmutableBag<V>, Satellite<K, V>
	{
		// empty so far
	}

	public interface Keys<K, V> extends XGettingMap.Keys<K, V>, XImmutableSet<K>, Satellite<K, V>
	{
		// empty so far
	}

	public interface Bridge<K, V> extends XGettingMap.Bridge<K, V>
	{
		// empty so far
	}

	public interface EntriesBridge<K, V> extends XGettingMap.EntriesBridge<K, V>
	{
		// empty so far
	}

}

