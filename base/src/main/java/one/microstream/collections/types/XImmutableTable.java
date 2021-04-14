package one.microstream.collections.types;

import java.util.function.Predicate;

import one.microstream.typing.KeyValue;


/**
 * 
 *
 */
public interface XImmutableTable<K, V> extends XImmutableMap<K, V>, XGettingTable<K, V>, XImmutableEnum<KeyValue<K, V>>
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
	public XImmutableTable<K, V> copy();

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

	public interface Satellite<K, V> extends XImmutableMap.Satellite<K, V>, XGettingTable.Satellite<K, V>
	{
		@Override
		public XImmutableTable<K, V> parent();

	}

	public interface Keys<K, V> extends XImmutableMap.Keys<K, V>, XGettingTable.Keys<K, V>, XImmutableEnum<K>
	{
		@Override
		public XImmutableTable<K, V> parent();

		@Override
		public XImmutableEnum<K> copy();
	}

	public interface Values<K, V>
	extends XImmutableMap.Values<K, V>, XGettingTable.Values<K, V>, XImmutableList<V>, Satellite<K, V>
	{
		@Override
		public XImmutableTable<K, V> parent();

		@Override
		public XImmutableList<V> copy();
	}

	public interface Bridge<K, V> extends XImmutableMap.Bridge<K, V>, XGettingTable.Bridge<K, V>, Satellite<K, V>
	{
		@Override
		public XImmutableTable<K, V> parent();

	}

	public interface EntriesBridge<K, V>
	extends XImmutableMap.EntriesBridge<K, V>, XGettingTable.EntriesBridge<K, V>, Satellite<K, V>
	{
		@Override
		public XImmutableTable<K, V> parent();
	}

}
