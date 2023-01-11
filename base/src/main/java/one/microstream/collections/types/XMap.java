package one.microstream.collections.types;

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

import java.util.function.Predicate;

import one.microstream.typing.KeyValue;


/**
 * @param <K> type of contained keys
 * @param <V> type of contained values
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
	 * @param key to add
	 * @param value to add
	 * @return {@code true} if element was added; {@code false} if not
	 */
	@Override
	public boolean add(K key, V value);

	/**
	 * Ensures the passed key and value to be contained as an entry in the map.
	 * @param key to add
	 * @param value to add
	 * @return {@code true} if element was added; {@code false} if not
	 */
	@Override
	public boolean put(K key, V value);

	/**
	 * Sets the passed key and value to an appropriate entry if one can be found.
	 * @param key to find element to change
	 * @param value to set
	 * @return {@code true} if element was changed; {@code false} if not
	 */
	@Override
	public boolean set(K key, V value);

	/**
	 * Ensures the passed key and value to be contained as an entry in the map.
	 * @param key to add
	 * @param value to add
	 * @return the old value or {@code null}
	 */
	@Override
	public KeyValue<K, V> putGet(K key, V value);
	

	/**
	 * Sets the passed key and value to an appropriate entry if one can be found.
	 * @param key to find element to change
	 * @param value to set
	 * @return the old value
	 */
	public KeyValue<K, V> setGet(K key, V value);

	/**
	 * Ensures the passed value to be either set to an existing entry equal to sampleKey or inserted as a new one.
	 * @param sampleKey to find an existing element
	 * @param value to insert
	 * @return {@code true} if element was changed; {@code false} if not
	 */
	@Override
	public boolean valuePut(K sampleKey, V value);

	/**
	 * Sets only the passed value to an existing entry appropriate to the passed sampleKey.
	 * @param sampleKey to find an existing element
	 * @param value to set
	 * @return {@code true} if element was changed; {@code false} if not
	 */
	@Override
	public boolean valueSet(K sampleKey, V value);

	/**
	 * Ensures the passed value to be either set to an existing entry appropriate to sampleKey or inserted as a new one.
	 * @param sampleKey to find an existing element
	 * @param value to add
	 * @return the old value
	 */
	public V valuePutGet(K sampleKey, V value);

	/**
	 * Sets only the passed value to an existing entry appropriate to the passed sampleKey.
	 * @param sampleKey to find an existing element
	 * @param value to add
	 * @return the old value
	 */
	public V valueSetGet(K sampleKey, V value);

	@Override
	public V get(K key);

	@Override
	public V searchValue(Predicate<? super K> keyPredicate);

	@Override
	public XImmutableMap<K, V> immure();

	
	/**
	 * Ensures the passed key-value-pairs to be contained as entries in the map.
	 * A return value indicates a new entry.
	 * 
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	@Override
	public XMap<K, V> putAll(KeyValue<K, V>... elements);

	/**
	 * Ensures the passed key-value-pairs to be contained as entries in the map.
	 * Only the elements with indices from the srcStartIndex to the srcStartIndex+srcLength are put in the collection. <br>
	 * A return value indicates a new entry.
	 * 
	 * @return this
	 */
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
		// empty so far
	}

}

