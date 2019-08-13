package one.microstream.collections.types;

public interface XPuttingMap<K, V> extends XAddingMap<K, V>
{
	public interface Creator<K, V> extends XAddingMap.Creator<K, V>
	{
		@Override
		public XPuttingMap<K, V> newInstance();
	}
	
	/**
	 * Ensures the passed key and value to be contained as an entry in the map. Return value indicates new entry.
	 * @param key
	 * @param value
	 */
	public boolean put(K key, V value);

	/**
	 * Ensures the passed value to be either set to an existing entry appropriate to sampleKey or inserted as a new one.
	 * @param sampleKey
	 * @param value
	 */
	public boolean valuePut(K sampleKey, V value);

}
