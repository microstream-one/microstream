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

import one.microstream.typing.KeyValue;


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
	 */
	public KeyValue<K, V> putGet(K key, V value);
	
	public KeyValue<K, V> replace(K key, V value);
		
}
