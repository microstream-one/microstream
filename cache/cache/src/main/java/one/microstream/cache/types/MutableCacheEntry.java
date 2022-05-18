
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

import static one.microstream.X.notNull;

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.processor.MutableEntry;


public interface MutableCacheEntry<K, V> extends MutableEntry<K, V>, Unwrappable
{
	public Operation getOperation();

	@Override
	public default <T> T unwrap(final Class<T> clazz)
	{
		return Static.unwrap(this, clazz);
	}

	public static enum Operation
	{
		NONE,
		ACCESS,
		CREATE,
		LOAD,
		REMOVE,
		UPDATE;
	}

	public static <K, V> MutableCacheEntry<K, V> New(
		final ObjectConverter converter,
		final K key,
		final CachedValue cachedValue,
		final long now,
		final CacheLoader<K, V> cacheLoader
	)
	{
		return new Default<>(
			converter,
			key,
			cachedValue,
			now,
			cacheLoader
		);
	}

	public static class Default<K, V> implements MutableCacheEntry<K, V>
	{
		private final K                 key;
		private final CachedValue       cachedValue;
		private final ObjectConverter   converter;
		private final long              now;
		private final CacheLoader<K, V> cacheLoader;
		private V                       value;
		private Operation               operation;

		Default(
			final ObjectConverter converter,
			final K key,
			final CachedValue cachedValue,
			final long now,
			final CacheLoader<K, V> cacheLoader
		)
		{
			this.converter   = converter;
			this.key         = key;
			this.cachedValue = cachedValue;
			this.operation   = Operation.NONE;
			this.value       = null;
			this.now         = now;
			this.cacheLoader = cacheLoader;
		}

		@Override
		public K getKey()
		{
			return this.key;
		}

		@Override
		public V getValue()
		{
			if(this.operation == Operation.NONE)
			{
				if(this.cachedValue == null || this.cachedValue.isExpiredAt(this.now))
				{
					this.value = null;
				}
				else if(this.value == null)
				{
					final Object internalValue;
					this.value = (internalValue = this.cachedValue.value(this.now)) == null
						? null
						: this.converter.externalize(internalValue);
				}
			}

			if(this.value != null)
			{
				// mark as Accessed so AccessedExpiry will be computed upon return from entry processor.
				if(this.operation == Operation.NONE)
				{
					this.operation = Operation.ACCESS;
				}
			}
			else
			{
				// check for read-through
				if(this.cacheLoader != null)
				{
					try
					{
						if((this.value = this.cacheLoader.load(this.key)) != null)
						{
							this.operation = Operation.LOAD;
						}
					}
					catch(final Exception e)
					{
						if(!(e instanceof CacheLoaderException))
						{
							throw new CacheLoaderException("Exception in CacheLoader", e);
						}

						throw e;
					}
				}
			}
			return this.value;
		}

		@Override
		public boolean exists()
		{
			return this.value != null
			   || this.operation == Operation.NONE
			       && this.cachedValue != null
				   && !this.cachedValue.isExpiredAt(this.now);
		}

		@Override
		public void remove()
		{
			this.operation = this.operation == Operation.CREATE || this.operation == Operation.LOAD
				? Operation.NONE
				: Operation.REMOVE;
			this.value     = null;
		}

		@Override
		public void setValue(final V value)
		{
			notNull(value);
			this.operation = this.cachedValue == null || this.cachedValue.isExpiredAt(this.now)
				? Operation.CREATE
				: Operation.UPDATE;
			this.value     = value;
		}

		@Override
		public Operation getOperation()
		{
			return this.operation;
		}

	}

}
