package net.jadoth.collections.types;

import net.jadoth.collections.KeyValue;


public interface XPutGetMap<K, V> extends XPuttingMap<K, V>, XAddGetMap<K, V>
{
	public interface Creator<K, V> extends XPuttingMap.Creator<K, V>, XAddGetMap.Creator<K, V>
	{
		@Override
		public XPutGetMap<K, V> newInstance();
	}
	
	
	
	/**
	 * Ensures the passed key and value to be contained as an entry in the map. Returns the old value or {@code null}.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public KeyValue<K, V> putGet(K key, V value);
	
	public KeyValue<K, V> replace(K key, V value);
		
}
