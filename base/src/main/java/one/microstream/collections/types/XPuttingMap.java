package one.microstream.collections.types;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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
