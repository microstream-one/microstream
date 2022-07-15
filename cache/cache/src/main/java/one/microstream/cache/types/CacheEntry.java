
package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
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

import one.microstream.typing.KeyValue;


public interface CacheEntry<K, V> extends javax.cache.Cache.Entry<K, V>, KeyValue<K, V>, Unwrappable
{
	@Override
	public default K key()
	{
		return this.getKey();
	}
	
	@Override
	public default V value()
	{
		return this.getValue();
	}
	
	@Override
	public default <T> T unwrap(final Class<T> clazz)
	{
		return Unwrappable.Static.unwrap(this, clazz);
	}
	
	static <K, V> CacheEntry<K, V> New(final K key, final V value)
	{
		return new Default<>(key, value);
	}

	public static class Default<K, V> implements CacheEntry<K, V>
	{
		private final K key;
		private final V value;

		Default(final K key, final V value)
		{
			super();

			this.key   = key;
			this.value = value;
		}

		@Override
		public K getKey()
		{
			return this.key;
		}

		@Override
		public V getValue()
		{
			return this.value;
		}

	}
	
}
