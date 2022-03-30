
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

import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;


public class CacheEvent<K, V> extends CacheEntryEvent<K, V> implements Unwrappable
{
	private final K       key;
	private final V       value;
	private final V       oldValue;
	private final boolean oldValueAvailable;

	CacheEvent(
		final Cache<K, V> source,
		final EventType eventType,
		final K key,
		final V value
	)
	{
		this(
			source,
			eventType,
			key,
			value,
			null,
			false
		);
	}

	CacheEvent(
		final Cache<K, V> source,
		final EventType eventType,
		final K key,
		final V value,
		final V oldValue
	)
	{
		this(
			source,
			eventType,
			key,
			value,
			oldValue,
			true
		);
	}

	CacheEvent(
		final Cache<K, V> source,
		final EventType eventType,
		final K key,
		final V value,
		final V oldValue,
		final boolean oldValueAvailable
	)
	{
		super(source, eventType);

		this.key               = key;
		this.value             = value;
		this.oldValue          = oldValue;
		this.oldValueAvailable = oldValueAvailable;
	}

	public Cache<K, V> getCache()
	{
		return super.getSource();
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

	@Override
	public V getOldValue() throws UnsupportedOperationException
	{
		return this.isOldValueAvailable()
			? this.oldValue
			: null;
	}

	@Override
	public boolean isOldValueAvailable()
	{
		return this.oldValueAvailable;
	}

	@Override
	public <T> T unwrap(final Class<T> clazz)
	{
		return Static.unwrap(this, clazz);
	}

}
