
package one.microstream.cache;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.util.Collection;
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
import one.microstream.persistence.lazy.Lazy;
import one.microstream.storage.types.EmbeddedStorageManager;


public interface CacheStore<K, V> extends CacheLoader<K, V>, CacheWriter<K, V>
{
	public static <K, V> CacheStore<K, V> New(final EmbeddedStorageManager storage, final String cacheName)
	{
		return new Default<>(storage, cacheName);
	}
	
	public static class Default<K, V> implements CacheStore<K, V>
	{
		private final EmbeddedStorageManager storage;
		private final String                 cacheName;
		
		Default(final EmbeddedStorageManager storage, final String cacheName)
		{
			super();
			
			this.storage   = notNull(storage);
			this.cacheName = notEmpty(cacheName);
		}
		
		@SuppressWarnings("unchecked")
		private XTable<K, Lazy<V>> cacheTable(final boolean create)
		{
			synchronized(this.storage)
			{
				XTable<String, Lazy<XTable<K, Lazy<V>>>> root;
				if((root = (XTable<String, Lazy<XTable<K, Lazy<V>>>>)this.storage.root()) == null)
				{
					this.storage.setRoot(root = EqHashTable.New());
					this.storage.storeRoot();
				}
				XTable<K, Lazy<V>> cacheTable;
				if((cacheTable = Lazy.get(root.get(this.cacheName))) == null && create)
				{
					this.storage.store(cacheTable = EqHashTable.New());
				}
				return cacheTable;
			}
		}
		
		@Override
		public synchronized V load(final K key) throws CacheLoaderException
		{
			final XTable<K, Lazy<V>> cacheTable;
			return (cacheTable = this.cacheTable(false)) != null
				? Lazy.get(cacheTable.get(key))
				: null;
		}
		
		@Override
		public synchronized Map<K, V> loadAll(final Iterable<? extends K> keys) throws CacheLoaderException
		{
			final Map<K, V>          result = new HashMap<>();
			final XTable<K, Lazy<V>> cacheTable;
			if((cacheTable = this.cacheTable(false)) != null)
			{
				keys.forEach(key -> result.put(key, Lazy.get(cacheTable.get(key))));
			}
			return result;
		}
		
		@Override
		public synchronized void write(final Entry<? extends K, ? extends V> entry) throws CacheWriterException
		{
			final XTable<K, Lazy<V>> cacheTable = this.cacheTable(true);
			cacheTable.put(entry.getKey(), Lazy.Reference(entry.getValue()));
			this.storage.store(cacheTable);
		}
		
		@Override
		public synchronized void writeAll(final Collection<Entry<? extends K, ? extends V>> entries)
			throws CacheWriterException
		{
			final XTable<K, Lazy<V>> cacheTable = this.cacheTable(true);
			entries.forEach(entry -> cacheTable.put(entry.getKey(), Lazy.Reference(entry.getValue())));
			this.storage.store(cacheTable);
		}
		
		@SuppressWarnings("unchecked") // Object in typed interface [sigh]
		@Override
		public synchronized void delete(final Object key) throws CacheWriterException
		{
			final XTable<K, Lazy<V>> cacheTable;
			if((cacheTable = this.cacheTable(false)) != null
				&& cacheTable.removeFor((K)key) != null)
			{
				this.storage.store(cacheTable);
			}
		}
		
		@Override
		public synchronized void deleteAll(final Collection<?> keys) throws CacheWriterException
		{
			final XTable<K, Lazy<V>> cacheTable;
			if((cacheTable = this.cacheTable(false)) != null)
			{
				boolean     changed  = false;
				final Iterator<?> iterator = keys.iterator();
				while(iterator.hasNext())
				{
					@SuppressWarnings("unchecked")
					final
					K key = (K)iterator.next();
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
		
	}
	
}
