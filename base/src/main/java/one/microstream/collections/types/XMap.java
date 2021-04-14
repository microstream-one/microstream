package one.microstream.collections.types;

import java.util.function.Predicate;

import one.microstream.typing.KeyValue;


/**
 * 
 *
 */
public interface XMap<K, V> extends XProcessingMap<K, V>, XPutGetMap<K, V>, XSet<KeyValue<K, V>>
{
	public interface Creator<K, V> extends XProcessingMap.Creator<K, V>, XPutGetMap.Creator<K, V>
	{
		@Override
		public XMap<K, V> newInstance();
	}


	// (15.07.2011 TM)FIXME: extract XSettingMap, requires XBasicMap?


	@Override
	public Keys<K, V> keys();

	@Override
	public Values<K, V> values();

	@Override
	public EntriesBridge<K, V> old();

	@Override
	public Bridge<K, V> oldMap();

	@Override
	public XMap<K, V> copy();

	@Override
	public boolean nullKeyAllowed();

	@Override
	public boolean nullValuesAllowed();


	/**
	 * Adds the passed key and value as an entry if key is not yet contained. Return value indicates new entry.
	 * @param key
	 * @param value
	 */
	@Override
	public boolean add(K key, V value);

	/**
	 * Ensures the passed key and value to be contained as an entry in the map. Return value indicates new entry.
	 * @param key
	 * @param value
	 */
	@Override
	public boolean put(K key, V value);

	/**
	 * Sets the passed key and value to an appropriate entry if one can be found. Return value indicates entry change.
	 * @param key
	 * @param value
	 */
	@Override
	public boolean set(K key, V value);

	/**
	 * Ensures the passed key and value to be contained as an entry in the map. Returns the old value or {@code null}.
	 * @param key
	 * @param value
	 */
	@Override
	public KeyValue<K, V> putGet(K key, V value);
	

	/**
	 * Sets the passed key and value to an appropriate entry if one can be found. Returns the old value.
	 * @param key
	 * @param value
	 */
	public KeyValue<K, V> setGet(K key, V value);

	/**
	 * Ensures the passed value to be either set to an existing entry appropriate to sampleKey or inserted as a new one.
	 * @param sampleKey
	 * @param value
	 */
	@Override
	public boolean valuePut(K sampleKey, V value);

	/**
	 * Sets only the passed value to an existing entry appropriate to the passed sampleKey.
	 * Returns value indicates change.
	 * @param sampleKey
	 * @param value
	 */
	@Override
	public boolean valueSet(K sampleKey, V value);

	/**
	 * Ensures the passed value to be either set to an existing entry appropriate to sampleKey or inserted as a new one.
	 */
	public V valuePutGet(K sampleKey, V value);

	/**
	 * Sets only the passed value to an existing entry appropriate to the passed sampleKey. Returns the old value.
	 */
	public V valueSetGet(K sampleKey, V value);

	@Override
	public V get(K key);

	@Override
	public V searchValue(Predicate<? super K> keyPredicate);

	@Override
	public XImmutableMap<K, V> immure();

	@SuppressWarnings("unchecked")
	@Override
	public XMap<K, V> putAll(KeyValue<K, V>... elements);

	@Override
	public XMap<K, V> putAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

	@SuppressWarnings("unchecked")
	@Override
	public XMap<K, V> addAll(KeyValue<K, V>... elements);

	@Override
	public XMap<K, V> addAll(KeyValue<K, V>[] elements, int srcStartIndex, int srcLength);

	@Override
	public XMap<K, V> addAll(XGettingCollection<? extends KeyValue<K, V>> elements);



	public interface Satellite<K, V> extends XGettingMap.Satellite<K, V>
	{
		@Override
		public XMap<K, V> parent();

	}


	public interface Bridge<K, V> extends XGettingMap.Bridge<K, V>, Satellite<K, V>
	{
		@Override
		public XMap<K, V> parent();

	}

	public interface EntriesBridge<K, V> extends XGettingMap.EntriesBridge<K, V>
	{
		@Override
		public XMap<K, V> parent();
	}

	public interface Values<K, V> extends XProcessingMap.Values<K, V>, Satellite<K, V>, XReplacingCollection<V>
	{
		@Override
		public XBag<V> copy(); // values in an unordered map is a practical example for a bag

	}

	public interface Keys<K, V> extends XProcessingMap.Keys<K, V>, XSet<K>, Satellite<K, V>, XReplacingCollection<K>
	{
		// emoty so far
	}

}

