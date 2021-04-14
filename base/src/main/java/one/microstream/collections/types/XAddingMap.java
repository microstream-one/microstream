package one.microstream.collections.types;

import one.microstream.collections.interfaces.CapacityExtendable;
import one.microstream.collections.interfaces.ExtendedMap;

public interface XAddingMap<K, V> extends CapacityExtendable, ExtendedMap<K, V>
{
	public interface Creator<K, V>
	{
		public XAddingMap<K, V> newInstance();
	}


	public boolean nullKeyAllowed();
	public boolean nullValuesAllowed(); // don't fall for asking why Values is plural while Key is singular :P

	/**
	 * Adds the passed key and value as an entry if key is not yet contained. Return value indicates new entry.
	 * @param key
	 * @param value
	 */
	public boolean add(K key, V value);

	/**
	 * Sets the passed key and value to an appropriate entry if one can be found. Return value indicates entry change.
	 * @param key
	 * @param value
	 */
	public boolean set(K key, V value);


	/**
	 * Sets only the passed value to an existing entry appropriate to the passed sampleKey.
	 * Returns value indicates change.
	 * @param sampleKey
	 * @param value
	 */
	public boolean valueSet(K sampleKey, V value);


}
