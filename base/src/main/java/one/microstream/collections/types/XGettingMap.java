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

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.interfaces.ExtendedMap;
import one.microstream.collections.old.OldCollection;
import one.microstream.typing.ComponentType;
import one.microstream.typing.KeyValue;


/**
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public interface XGettingMap<K, V> extends ExtendedMap<K, V>, XGettingSet<KeyValue<K, V>>
{
	public interface Creator<K, V>
	{
		public XGettingMap<K, V> newInstance();
	}



	// key to value querying
	public V get(K key);
	
	public KeyValue<K, V> lookup(K key);

	public V searchValue(Predicate<? super K> keyPredicate);

	public <C extends Consumer<? super V>> C query(XIterable<? extends K> keys, C collector);

	// satellite instances

	public Keys<K, V> keys();

	public Values<K, V> values();

	@Override
	public XGettingMap<K, V> copy();

	@Override
	public XGettingMap<K, V> view();

	/**
	 * Provides an instance of an immutable collection type with equal behavior and data as this instance.
	 * <p>
	 * If this instance already is of an immutable collection type, it returns itself.
	 *
	 * @return an immutable copy of this collection instance.
	 */
	@Override
	public XImmutableMap<K, V> immure();

	@Override
	public EntriesBridge<K, V> old();

	public Bridge<K, V> oldMap();

	// null handling characteristics information

	public boolean nullKeyAllowed();

	public boolean nullValuesAllowed();


	///////////////////////////////////////////////////////////////////////////
	// satellite types //
	////////////////////

	public interface Satellite<K, V> extends ComponentType
	{
		public XGettingMap<K, V> parent();

	}

	public interface Keys<K, V> extends XGettingSet<K>, Satellite<K, V>
	{
		@Override
		public XImmutableSet<K> immure();

	}

	// values in an unordered map is a practical example for a bag
	public interface Values<K, V> extends XGettingBag<V>, Satellite<K, V>
	{
		// empty so far
	}

	public interface Bridge<K, V> extends Satellite<K, V>, Map<K, V>
	{
		// empty so far
	}

	public interface EntriesBridge<K, V> extends Satellite<K, V>, OldCollection<KeyValue<K, V>>
	{
		// empty so far
	}

}

