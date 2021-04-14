package one.microstream.collections.types;

import java.util.function.Function;

import one.microstream.typing.KeyValue;

public interface XAddGetMap<K, V> extends XAddingMap<K, V>, XGettingMap<K, V>
{
	public interface Creator<K, V> extends XAddingMap.Creator<K, V>, XGettingMap.Creator<K, V>
	{
		@Override
		public XAddGetMap<K, V> newInstance();
	}
	
	
	
	public KeyValue<K, V> addGet(K key, V value);
	
	public KeyValue<K, V> substitute(K key, V value);
	
	/**
	 * Ensures that this map instance contains a non-null value for the passed key and returns that value.
	 * <p>
	 * If a non-null value can be found for the passed key, it is returned. Otherwise, the value provided
	 * by the passed supplier will be associated with the passed key and is returned.
	 * 
	 * @param key the search key.
	 * @param valueProvider the value supplier used to provide a value for the passed key in case non could be found.
	 * @return the value associated with the passed key, either already existing or newly assiciated by
	 *         the call of this method.
	 */
	public V ensure(K key, Function<? super K, V> valueProvider);
	
}
