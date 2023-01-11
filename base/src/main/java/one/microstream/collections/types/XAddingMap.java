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

import one.microstream.collections.interfaces.CapacityExtendable;
import one.microstream.collections.interfaces.ExtendedMap;

/**
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public interface XAddingMap<K, V> extends CapacityExtendable, ExtendedMap<K, V>
{
	/**
	 * 
	 * @param <K> the key type
	 * @param <V> the value type
	 */
	public interface Creator<K, V>
	{
		public XAddingMap<K, V> newInstance();
	}


	public boolean nullKeyAllowed();
	public boolean nullValuesAllowed();

	/**
	 * Adds the passed key and value as an entry if key is not yet contained. Return value indicates new entry.
	 * @param key the key
	 * @param value the value
	 * @return <code>true</code> if a new entry was created
	 */
	public boolean add(K key, V value);

	/**
	 * Sets the passed key and value to an appropriate entry if one can be found. Return value indicates entry change.
	 * @param key the key
	 * @param value the value
	 * @return <code>true</code> if an entry was changed
	 */
	public boolean set(K key, V value);


	/**
	 * Sets only the passed value to an existing entry appropriate to the passed sampleKey.
	 * Returns value indicates change.
	 * @param sampleKey the key
	 * @param value the value
	 * @return <code>true</code> if a value was changed
	 */
	public boolean valueSet(K sampleKey, V value);


}
