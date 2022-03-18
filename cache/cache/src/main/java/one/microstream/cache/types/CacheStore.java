
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
import static one.microstream.chars.XChars.notEmpty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.cache.Cache.Entry;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XTable;
import one.microstream.reference.Lazy;
import one.microstream.storage.types.StorageManager;


public interface CacheStore<K, V> extends CacheLoader<K, V>, CacheWriter<K, V>
{
	public Iterator<K> keys();
	
	
	public static <K, V> CacheStore<K, V> New(final String cacheKey, final StorageManager storage)
	{
		return new Default<>(cacheKey, storage);
	}
	
	public static class Default<K, V> implements CacheStore<K, V>
	{
		private final String                 cacheKey;
		private final StorageManager storage;
		
		Default(final String cacheKey, final StorageManager storage)
		{
			super();
			
			this.cacheKey = notEmpty(cacheKey);
			this.storage  = notNull(storage);
		}
		
		@SuppressWarnings("unchecked")
		private XTable<K, Lazy<V>> cacheTable(final boolean create)
		{
			synchronized(this.storage)
			{
				if(!this.storage.isRunning())
				{
					this.storage.start();
				}
				
				boolean                                  storeRoot = false;
				XTable<String, Lazy<XTable<K, Lazy<V>>>> root;
				if((root = (XTable<String, Lazy<XTable<K, Lazy<V>>>>)this.storage.root()) == null)
				{
					this.storage.setRoot(root = EqHashTable.New());
					storeRoot = true;
				}
				XTable<K, Lazy<V>> cacheTable;
				if((cacheTable = Lazy.get(root.get(this.cacheKey))) == null && create)
				{
					root.put(this.cacheKey, Lazy.Reference(cacheTable = EqHashTable.New()));
					storeRoot = true;
				}
				if(storeRoot)
				{
					this.storage.storeRoot();
				}
				return cacheTable;
			}
		}
		
		@Override
		public synchronized Iterator<K> keys()
		{
			final XTable<K, Lazy<V>> cacheTable = this.cacheTable(false);
			return cacheTable != null
				? cacheTable.keys().iterator()
				: Collections.emptyIterator()
			;
		}
		
		@Override
		public synchronized V load(final K key) throws CacheLoaderException
		{
			try
			{
				final XTable<K, Lazy<V>> cacheTable;
				return (cacheTable = this.cacheTable(false)) != null
					? Lazy.get(cacheTable.get(key))
					: null;
			}
			catch(final Exception e)
			{
				throw new CacheLoaderException(e);
			}
		}
		
		@Override
		public synchronized Map<K, V> loadAll(final Iterable<? extends K> keys) throws CacheLoaderException
		{
			try
			{
				final Map<K, V>          result = new HashMap<>();
				final XTable<K, Lazy<V>> cacheTable;
				if((cacheTable = this.cacheTable(false)) != null)
				{
					keys.forEach(key -> result.put(key, Lazy.get(cacheTable.get(key))));
				}
				return result;
			}
			catch(final Exception e)
			{
				throw new CacheLoaderException(e);
			}
		}
		
		@Override
		public synchronized void write(final Entry<? extends K, ? extends V> entry) throws CacheWriterException
		{
			try
			{
				final XTable<K, Lazy<V>> cacheTable = this.cacheTable(true);
				cacheTable.put(entry.getKey(), Lazy.Reference(entry.getValue()));
				this.storage.store(cacheTable);
			}
			catch(final Exception e)
			{
				throw new CacheWriterException(e);
			}
		}
		
		@Override
		public synchronized void writeAll(final Collection<Entry<? extends K, ? extends V>> entries)
			throws CacheWriterException
		{
			try
			{
				final XTable<K, Lazy<V>> cacheTable = this.cacheTable(true);
				entries.forEach(entry -> cacheTable.put(entry.getKey(), Lazy.Reference(entry.getValue())));
				this.storage.store(cacheTable);
			}
			catch(final Exception e)
			{
				throw new CacheWriterException(e);
			}
		}
		
		@SuppressWarnings("unchecked") // Object in typed interface [sigh]
		@Override
		public synchronized void delete(final Object key) throws CacheWriterException
		{
			try
			{
				final XTable<K, Lazy<V>> cacheTable;
				if((cacheTable = this.cacheTable(false)) != null
					&& cacheTable.removeFor((K)key) != null)
				{
					this.storage.store(cacheTable);
				}
			}
			catch(final Exception e)
			{
				throw new CacheWriterException(e);
			}
		}
		
		@Override
		public synchronized void deleteAll(final Collection<?> keys) throws CacheWriterException
		{
			try
			{
				final XTable<K, Lazy<V>> cacheTable;
				if((cacheTable = this.cacheTable(false)) != null)
				{
					boolean           changed  = false;
					final Iterator<?> iterator = keys.iterator();
					while(iterator.hasNext())
					{
						@SuppressWarnings("unchecked")
						final K key = (K)iterator.next();
						if(cacheTable.removeFor(key) != null)
						{
							iterator.remove();
							changed = true;
						}
					}
					if(changed)
					{
						this.storage.store(cacheTable);
					}
				}
			}
			catch(final Exception e)
			{
				throw new CacheWriterException(e);
			}
		}
		
	}
	
}
