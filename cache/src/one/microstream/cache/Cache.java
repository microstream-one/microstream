
package one.microstream.cache;

import static one.microstream.X.notNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.cache.CacheException;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.event.EventType;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XList;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.typing.KeyValue;


public interface Cache<K, V> extends javax.cache.Cache<K, V>, Unwrappable
{
	@Override
	public CacheManager getCacheManager();
	
	public CacheConfiguration<K, V> getConfiguration();
	
	public long size();
	
	public void putAll(
		Map<? extends K, ? extends V> map,
		boolean replaceExistingValues);
	
	public void putAll(
		Map<? extends K, ? extends V> map,
		boolean replaceExistingValues,
		boolean useWriteThrough);
	
	public void setManagementEnabled(boolean enabled);
	
	public void setStatisticsEnabled(boolean enabled);
	
	@Override
	public default <T> T unwrap(final Class<T> clazz)
	{
		return Unwrappable.Static.unwrap(this, clazz);
	}
	
	public static <K, V> Cache<K, V> New(
		final String                   name,
		final CacheManager             manager,
		final CacheConfiguration<K, V> configuration,
		final ObjectConverter          objectConverter,
		final CacheLoader<K, V>        cacheLoader,
		final CacheWriter<K, V>        cacheWriter,
		final ExpiryPolicy             expiryPolicy,
		final EvictionPolicy           evictionPolicy
	)
	{
		return new Default<>(
			name,
			manager,
			configuration,
			objectConverter,
			cacheLoader,
			cacheWriter,
			expiryPolicy,
			evictionPolicy
		);
	}
	
	public static class Default<K, V> implements Cache<K, V>
	{
		private final String                                      name;
		private final CacheManager                                manager;
		private final CacheConfiguration<K, V>                    configuration;
		private final ObjectConverter                             objectConverter;
		private final CacheLoader<K, V>                           cacheLoader;
		private final CacheWriter<K, V>                           cacheWriter;
		private final ExpiryPolicy                                expiryPolicy;
		private final EvictionPolicy                              evictionPolicy;
		private final CacheTable                                  cacheTable;
		private final XList<CacheEntryListenerRegistration<K, V>> listenerRegistrations;
		private final ExecutorService                             executorService;
		private final CacheMXBean                                 cacheMXBean;
		private final CacheStatisticsMXBean                       cacheStatisticsMXBean;
		private volatile boolean                                  isStatisticsEnabled;
		private volatile boolean                                  isClosed;
		
		Default(
			final String                   name,
			final CacheManager             manager,
			final CacheConfiguration<K, V> configuration,
			final ObjectConverter          objectConverter,
			final CacheLoader<K, V>        cacheLoader,
			final CacheWriter<K, V>        cacheWriter,
			final ExpiryPolicy             expiryPolicy,
			final EvictionPolicy           evictionPolicy
		)
		{
			super();
			
			this.name                  = name;
			this.manager               = manager;
			this.configuration         = configuration;
			this.objectConverter       = objectConverter;
			this.cacheLoader           = cacheLoader;
			this.cacheWriter           = cacheWriter;
			this.expiryPolicy          = expiryPolicy;
			this.evictionPolicy        = evictionPolicy;

			this.cacheTable            = CacheTable.New();
			this.listenerRegistrations = BulkList.New();
			this.executorService       = Executors.newFixedThreadPool(1);
			this.cacheMXBean           = new CacheMXBean.Default(this.configuration);
			this.cacheStatisticsMXBean = new CacheStatisticsMXBean.Default(this::size);
			
			configuration.getCacheEntryListenerConfigurations().forEach(this::registerCacheEntryListener);
			
			if(configuration.isManagementEnabled())
			{
				this.setManagementEnabled(true);
			}
			if(configuration.isStatisticsEnabled())
			{
				this.setStatisticsEnabled(true);
			}
		}
		
		@Override
		public String getName()
		{
			return this.name;
		}
		
		@Override
		public CacheManager getCacheManager()
		{
			return this.manager;
		}
		
		@Override
		public CacheConfiguration<K, V> getConfiguration()
		{
			return this.configuration;
		}
		
		@Override
		public <C extends Configuration<K, V>> C getConfiguration(final Class<C> clazz)
		{
			if(clazz.isInstance(this.configuration))
			{
				return clazz.cast(this.configuration);
			}
			
			throw new IllegalArgumentException("Unsupported configuration type: " + clazz.getName());
		}
		
		@Override
		public void registerCacheEntryListener(
			final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
		{
			notNull(cacheEntryListenerConfiguration);
			
			synchronized(this.listenerRegistrations)
			{
				this.listenerRegistrations.add(
					CacheEntryListenerRegistration.New(cacheEntryListenerConfiguration)
				);
			}
		}
		
		@Override
		public void deregisterCacheEntryListener(
			final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
		{
			notNull(cacheEntryListenerConfiguration);
			
			synchronized(this.listenerRegistrations)
			{
				this.listenerRegistrations.removeBy(
					reg -> cacheEntryListenerConfiguration.equals(reg.getConfiguration())
				);
			}
		}
		
		@Override
		public boolean isClosed()
		{
			return this.isClosed;
		}
		
		@Override
		public synchronized void close()
		{
			if(this.isClosed)
			{
				return;
			}
			
			this.isClosed = true;
			
			this.manager.removeCache(this.name);
			
			this.setStatisticsEnabled(false);
			this.setManagementEnabled(false);
			
			this.closeIfCloseable(this.cacheLoader);
			this.closeIfCloseable(this.cacheWriter);
			this.closeIfCloseable(this.expiryPolicy);
			this.listenerRegistrations.forEach(this::closeIfCloseable);
			
			this.executorService.shutdown();
			try
			{
				this.executorService.awaitTermination(10, TimeUnit.SECONDS);
			}
			catch(final InterruptedException e)
			{
				throw new CacheException(e);
			}
			
			this.cacheTable.clear();
		}
		
		@Override
		public long size()
		{
			synchronized(this.cacheTable)
			{
				return this.cacheTable.size();
			}
		}
		
		@Override
		public V get(final K key)
		{
			this.ensureOpen();
			
			this.validateKey(key);
			
			final CacheEventDispatcher<K, V> eventDispatcher = CacheEventDispatcher.New();
			
			final V                          value           = this.getValue(key, eventDispatcher);
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			return value;
		}
		
		@Override
		public Map<K, V> getAll(final Set<? extends K> keys)
		{
			this.ensureOpen();
			
			keys.forEach(this::validateKey);
			
			final HashMap<K, V>              result          = new HashMap<>(keys.size());
			
			final CacheEventDispatcher<K, V> eventDispatcher = CacheEventDispatcher.New();
			
			for(final K key : keys)
			{
				V value;
				if((value = this.getValue(key, eventDispatcher)) != null)
				{
					result.put(key, value);
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			return result;
		}
		
		@Override
		public boolean containsKey(final K key)
		{
			this.ensureOpen();
			
			this.validateKey(key);
			
			final Object internalKey = this.objectConverter.internalize(key);
			final long   now         = System.currentTimeMillis();
			
			synchronized(this.cacheTable)
			{
				final CachedValue cachedValue = this.cacheTable.get(internalKey);
				return cachedValue != null && !cachedValue.isExpiredAt(now);
			}
		}
		
		@Override
		public void loadAll(
			final Set<? extends K> keys,
			final boolean replaceExistingValues,
			final CompletionListener completionListener)
		{
			this.ensureOpen();
			
			if(this.cacheLoader != null)
			{
				keys.forEach(this::validateKey);
				
				this.submit(() -> this.loadAllInternal(keys, replaceExistingValues, completionListener));
			}
			else if(completionListener != null)
			{
				completionListener.onCompletion();
			}
		}
		
		private void loadAllInternal(
			final Set<? extends K> keys,
			final boolean replaceExistingValues,
			final CompletionListener completionListener)
		{
			try
			{
				final ArrayList<K> keysToLoad = new ArrayList<>();
				for(final K key : keys)
				{
					if(replaceExistingValues || !this.containsKey(key))
					{
						keysToLoad.add(key);
					}
				}
				
				Map<? extends K, ? extends V> loaded;
				try
				{
					loaded = this.cacheLoader.loadAll(keysToLoad);
				}
				catch(final CacheLoaderException e)
				{
					throw e;
				}
				catch(final Exception e)
				{
					throw new CacheLoaderException(e);
				}
				
				for(final K key : keysToLoad)
				{
					if(loaded.get(key) == null)
					{
						loaded.remove(key);
					}
				}
				
				this.putAll(loaded, replaceExistingValues, false);
				
				if(completionListener != null)
				{
					completionListener.onCompletion();
				}
			}
			catch(final Exception e)
			{
				if(completionListener != null)
				{
					completionListener.onException(e);
				}
			}
		}
		
		@Override
		public void put(final K key, final V value)
		{
			this.ensureOpen();
			
			this.validateKeyValue(key, value);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			int                              putCount            = 0;
			final long                       now                 = System.currentTimeMillis();
			final Object                     internalKey         = this.objectConverter.internalize(key);
			final Object                     internalValue       = this.objectConverter.internalize(value);
			
			synchronized(this.cacheTable)
			{
				CachedValue   cachedValue = this.cacheTable.get(internalKey);
				final boolean isExpired   = cachedValue != null && cachedValue.isExpiredAt(now);
				
				if(isExpired)
				{
					this.processExpiries(
						key,
						internalKey,
						eventDispatcher,
						this.objectConverter.externalize(cachedValue.value())
					);
				}
				
				final CacheEntry<K, V> entry = CacheEntry.New(key, value);
				
				if(cachedValue == null || isExpired)
				{
					cachedValue = CachedValue.New(
						internalValue,
						now,
						this.expiryForCreation().getAdjustedTime(now)
					);
					
					if(cachedValue.isExpiredAt(now))
					{
						this.processExpiries(
							key,
							internalKey,
							eventDispatcher,
							this.objectConverter.externalize(cachedValue.value())
						);
					}
					else
					{
						this.putValue(
							key,
							value,
							internalKey,
							cachedValue,
							eventDispatcher,
							isStatisticsEnabled
						);
						this.writeCacheEntry(entry);
						putCount++;
					}
				}
				else
				{
					final V oldValue = this.objectConverter.externalize(cachedValue.value(now));
					
					this.updateExpiryForUpdate(cachedValue, now);
					
					cachedValue.value(internalValue, now);
					this.writeCacheEntry(entry);
					putCount++;
					
					eventDispatcher.addEvent(
						CacheEntryUpdatedListener.class,
						new CacheEvent<>(this, EventType.UPDATED, key, value, oldValue)
					);
				}
				
				eventDispatcher.dispatch(this.listenerRegistrations);
			}
			
			if(isStatisticsEnabled && putCount > 0)
			{
				this.cacheStatisticsMXBean.increaseCachePuts(putCount);
				this.cacheStatisticsMXBean.addPutTimeNano(System.nanoTime() - start);
			}
		}
		
		@Override
		public V getAndPut(final K key, final V value)
		{
			this.ensureOpen();
			
			this.validateKeyValue(key, value);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			V                                result;
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			int                              putCount            = 0;
			final long                       now                 = System.currentTimeMillis();
			final Object                     internalKey         = this.objectConverter.internalize(key);
			final Object                     internalValue       = this.objectConverter.internalize(value);
			
			synchronized(this.cacheTable)
			{
				CachedValue   cachedValue = this.cacheTable.get(internalKey);
				final boolean isExpired   = cachedValue != null && cachedValue.isExpiredAt(now);
				
				if(isExpired)
				{
					this.processExpiries(
						key,
						internalKey,
						eventDispatcher,
						this.objectConverter.externalize(cachedValue.value())
					);
				}
				
				final CacheEntry<K, V> entry = CacheEntry.New(key, value);
				
				if(cachedValue == null || isExpired)
				{
					result = null;
					
					cachedValue = CachedValue.New(
						internalValue,
						now,
						this.expiryForCreation().getAdjustedTime(now)
					);
					
					if(cachedValue.isExpiredAt(now))
					{
						this.processExpiries(
							key,
							internalKey,
							eventDispatcher,
							this.objectConverter.externalize(cachedValue.value())
						);
					}
					else
					{
						this.putValue(
							key,
							value,
							internalKey,
							cachedValue,
							eventDispatcher,
							isStatisticsEnabled
						);
						this.writeCacheEntry(entry);
						putCount++;
					}
				}
				else
				{
					final V oldValue = result = this.objectConverter.externalize(cachedValue.value(now));
					
					this.updateExpiryForUpdate(cachedValue, now);
					
					cachedValue.value(internalValue, now);
					this.writeCacheEntry(entry);
					putCount++;
					
					eventDispatcher.addEvent(
						CacheEntryUpdatedListener.class,
						new CacheEvent<>(this, EventType.UPDATED, key, value, oldValue)
					);
				}
				
				eventDispatcher.dispatch(this.listenerRegistrations);
			}
			
			if(isStatisticsEnabled)
			{
				if(result == null)
				{
					this.cacheStatisticsMXBean.increaseCacheMisses(1);
				}
				else
				{
					this.cacheStatisticsMXBean.increaseCacheHits(1);
				}
				
				this.cacheStatisticsMXBean.addGetTimeNano(System.nanoTime() - start);
				
				if(putCount > 0)
				{
					this.cacheStatisticsMXBean.increaseCachePuts(putCount);
					this.cacheStatisticsMXBean.addPutTimeNano(System.nanoTime() - start);
				}
			}
			
			return result;
		}
		
		@Override
		public void putAll(final Map<? extends K, ? extends V> map)
		{
			this.putAll(map, true);
		}
		
		@Override
		public void putAll(
			final Map<? extends K, ? extends V> map,
			final boolean replaceExistingValues)
		{
			this.putAll(map, replaceExistingValues, true);
		}
		
		@Override
		public void putAll(
			final Map<? extends K, ? extends V> map,
			final boolean replaceExistingValues,
			final boolean useWriteThrough)
		{
			this.ensureOpen();
			
			map.keySet().forEach(this::validateKey);
			map.values().forEach(this::validateValue);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final long                       now                 = System.currentTimeMillis();
			int                              putCount            = 0;
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			CacheWriterException             exception           = null;
			
			synchronized(this.cacheTable)
			{
				final boolean                                           isWriteThrough =
					this.cacheWriter != null && useWriteThrough;
				
				final Collection<Cache.Entry<? extends K, ? extends V>> entriesToWrite = new ArrayList<>();
				final HashSet<K>                                        keysToPut      = new HashSet<>();
				for(final Map.Entry<? extends K, ? extends V> entry : map.entrySet())
				{
					final K key   = entry.getKey();
					final V value = entry.getValue();
					
					keysToPut.add(key);
					
					if(isWriteThrough)
					{
						entriesToWrite.add(CacheEntry.New(key, value));
					}
				}
				
				if(isWriteThrough)
				{
					try
					{
						this.cacheWriter.writeAll(entriesToWrite);
					}
					catch(final CacheWriterException e)
					{
						exception = e;
					}
					catch(final Exception e)
					{
						exception = new CacheWriterException(e);
					}
					
					for(final Cache.Entry<? extends K, ? extends V> entry : entriesToWrite)
					{
						keysToPut.remove(entry.getKey());
					}
				}
				
				for(final K key : keysToPut)
				{
					final V       value         = map.get(key);
					final Object  internalKey   = this.objectConverter.internalize(key);
					final Object  internalValue = this.objectConverter.internalize(value);
					CachedValue   cachedValue   = this.cacheTable.get(internalKey);
					
					final boolean isExpired     = cachedValue != null && cachedValue.isExpiredAt(now);
					if(cachedValue == null || isExpired)
					{
						if(isExpired)
						{
							this.processExpiries(
								key,
								internalKey,
								eventDispatcher,
								this.objectConverter.externalize(cachedValue.value())
							);
						}
						
						cachedValue = CachedValue.New(
							internalValue,
							now,
							this.expiryForCreation().getAdjustedTime(now)
						);
						if(cachedValue.isExpiredAt(now))
						{
							this.processExpiries(
								key,
								internalKey,
								eventDispatcher,
								value
							);
						}
						else
						{
							this.putValue(
								key,
								value,
								internalKey,
								cachedValue,
								eventDispatcher,
								isStatisticsEnabled
							);
							
							/*
							 * This method called from loadAll when useWriteThrough is false. Do not count loads as puts
							 * per statistics table in specification.
							 */
							if(useWriteThrough)
							{
								putCount++;
							}
						}
					}
					else if(replaceExistingValues)
					{
						final V oldValue = this.objectConverter.externalize(cachedValue.value());
						
						this.updateExpiryForUpdate(cachedValue, now);
						
						cachedValue.value(internalValue, now);
						
						/*
						 * Do not count loadAll calls as puts. useWriteThrough is false when called from loadAll.
						 */
						if(useWriteThrough)
						{
							putCount++;
						}
						
						eventDispatcher.addEvent(
							CacheEntryUpdatedListener.class,
							new CacheEvent<>(this, EventType.UPDATED, key, value, oldValue)
						);
					}
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(isStatisticsEnabled && putCount > 0)
			{
				this.cacheStatisticsMXBean.increaseCachePuts(putCount);
				this.cacheStatisticsMXBean.addPutTimeNano(System.nanoTime() - start);
			}
			
			if(exception != null)
			{
				throw exception;
			}
		}
		
		@Override
		public boolean putIfAbsent(final K key, final V value)
		{
			this.ensureOpen();
			
			this.validateKeyValue(key, value);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final long                       now                 = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			final Object                     internalKey         = this.objectConverter.internalize(key);
			final Object                     internalValue       = this.objectConverter.internalize(value);
			boolean                          result;
			
			synchronized(this.cacheTable)
			{
				CachedValue   cachedValue = this.cacheTable.get(internalKey);
				
				final boolean isExpired   = cachedValue != null && cachedValue.isExpiredAt(now);
				if(cachedValue == null || isExpired)
				{
					final CacheEntry<K, V> entry = CacheEntry.New(key, value);
					this.writeCacheEntry(entry);
					
					if(isExpired)
					{
						this.processExpiries(
							key,
							internalKey,
							eventDispatcher,
							this.objectConverter.externalize(cachedValue.value())
						);
					}
					
					cachedValue = CachedValue.New(
						internalValue,
						now,
						this.expiryForCreation().getAdjustedTime(now)
					);
					if(cachedValue.isExpiredAt(now))
					{
						this.processExpiries(
							key,
							internalKey,
							eventDispatcher,
							value
						);
						
						// no expiry event for created entry that expires before put in cache.
						// do not put entry in cache.
						result = false;
					}
					else
					{
						this.putValue(
							key,
							value,
							internalKey,
							cachedValue,
							eventDispatcher,
							isStatisticsEnabled
						);
						result = true;
					}
				}
				else
				{
					result = false;
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(isStatisticsEnabled)
			{
				if(result)
				{
					this.cacheStatisticsMXBean.increaseCachePuts(1);
					this.cacheStatisticsMXBean.increaseCacheMisses(1);
					this.cacheStatisticsMXBean.addPutTimeNano(System.nanoTime() - start);
				}
				else
				{
					this.cacheStatisticsMXBean.increaseCacheHits(1);
				}
			}
			
			return result;
		}
		
		@Override
		public boolean remove(final K key)
		{
			this.ensureOpen();
			
			this.validateKey(key);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final long                       now                 = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			final Object                     internalKey         = this.objectConverter.internalize(key);
			boolean                          result;
			
			synchronized(this.cacheTable)
			{
				this.deleteCacheEntry(key);
				
				final CachedValue cachedValue;
				if((cachedValue = this.cacheTable.get(internalKey)) == null)
				{
					return false;
				}
				
				if(cachedValue.isExpiredAt(now))
				{
					result = false;
				}
				else
				{
					this.cacheTable.remove(internalKey);
					final V value = this.objectConverter.externalize(cachedValue.value());
					
					eventDispatcher.addEvent(
						CacheEntryRemovedListener.class,
						new CacheEvent<>(this, EventType.REMOVED, key, value, value)
					);
					
					result = true;
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(result && isStatisticsEnabled)
			{
				this.cacheStatisticsMXBean.increaseCacheRemovals(1);
				this.cacheStatisticsMXBean.addRemoveTimeNano(System.nanoTime() - start);
			}
			
			return result;
		}
		
		@Override
		public boolean remove(final K key, final V oldValue)
		{
			this.ensureOpen();
			
			this.validateKeyValue(key, oldValue);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final long                       now                 = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			final Object                     internalKey         = this.objectConverter.internalize(key);
			boolean                          hit                 = false;
			boolean                          result;
			
			synchronized(this.cacheTable)
			{
				final CachedValue cachedValue = this.cacheTable.get(internalKey);
				if(cachedValue == null || cachedValue.isExpiredAt(now))
				{
					result = false;
				}
				else
				{
					hit = true;
					
					final Object internalValue    = cachedValue.value();
					final Object oldInternalValue = this.objectConverter.internalize(oldValue);
					
					if(internalValue.equals(oldInternalValue))
					{
						this.deleteCacheEntry(key);
						
						this.cacheTable.remove(internalKey);
						
						eventDispatcher.addEvent(
							CacheEntryRemovedListener.class,
							new CacheEvent<>(this, EventType.REMOVED, key, oldValue, oldValue)
						);
						
						result = true;
					}
					else
					{
						this.updateExpiryForAccess(cachedValue, now);
						
						result = false;
					}
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(isStatisticsEnabled)
			{
				final long duration = System.nanoTime() - start;
				if(result)
				{
					this.cacheStatisticsMXBean.increaseCacheRemovals(1);
					this.cacheStatisticsMXBean.addRemoveTimeNano(duration);
				}
				this.cacheStatisticsMXBean.addGetTimeNano(duration);
				if(hit)
				{
					this.cacheStatisticsMXBean.increaseCacheHits(1);
				}
				else
				{
					this.cacheStatisticsMXBean.increaseCacheMisses(1);
				}
			}
			
			return result;
		}
		
		@Override
		public V getAndRemove(final K key)
		{
			this.ensureOpen();
			
			this.validateKey(key);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final long                       now                 = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			final Object                     internalKey         = this.objectConverter.internalize(key);
			V                                result;
			
			synchronized(this.cacheTable)
			{
				this.deleteCacheEntry(key);
				
				final CachedValue cachedValue = this.cacheTable.get(internalKey);
				if(cachedValue == null || cachedValue.isExpiredAt(now))
				{
					result = null;
				}
				else
				{
					this.cacheTable.remove(internalKey);
					result = this.objectConverter.externalize(cachedValue.value(now));
					
					eventDispatcher.addEvent(
						CacheEntryRemovedListener.class,
						new CacheEvent<>(this, EventType.REMOVED, key, result, result)
					);
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(isStatisticsEnabled)
			{
				final long duration = System.nanoTime() - start;
				this.cacheStatisticsMXBean.addGetTimeNano(duration);
				if(result != null)
				{
					this.cacheStatisticsMXBean.increaseCacheHits(1);
					this.cacheStatisticsMXBean.increaseCacheRemovals(1);
					this.cacheStatisticsMXBean.addRemoveTimeNano(duration);
				}
				else
				{
					this.cacheStatisticsMXBean.increaseCacheMisses(1);
				}
			}
			
			return result;
		}
		
		@Override
		public boolean replace(final K key, final V oldValue, final V newValue)
		{
			this.ensureOpen();
			
			this.validateKey(key);
			this.validateValue(oldValue);
			this.validateValue(newValue);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final long                       now                 = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			final Object                     internalKey         = this.objectConverter.internalize(key);
			long                             hitCount            = 0;
			boolean                          result;
			
			synchronized(this.cacheTable)
			{
				final CachedValue cachedValue = this.cacheTable.get(internalKey);
				if(cachedValue == null || cachedValue.isExpiredAt(now))
				{
					result = false;
				}
				else
				{
					hitCount++;
					
					final Object oldInternalValue = this.objectConverter.internalize(oldValue);
					
					if(cachedValue.value().equals(oldInternalValue))
					{
						final CacheEntry<K, V> entry = CacheEntry.New(key, newValue);
						this.writeCacheEntry(entry);
						
						this.updateExpiryForUpdate(cachedValue, now);
						
						cachedValue.value(
							this.objectConverter.internalize(newValue),
							now
						);
						
						eventDispatcher.addEvent(
							CacheEntryUpdatedListener.class,
							new CacheEvent<>(this, EventType.UPDATED, key, newValue, oldValue)
						);
						
						result = true;
					}
					else
					{
						this.updateExpiryForAccess(cachedValue, now);
						
						result = false;
					}
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(isStatisticsEnabled)
			{
				final long duration = System.nanoTime() - start;
				if(result)
				{
					this.cacheStatisticsMXBean.increaseCachePuts(1);
					this.cacheStatisticsMXBean.addPutTimeNano(duration);
				}
				this.cacheStatisticsMXBean.addGetTimeNano(duration);
				if(hitCount == 1)
				{
					this.cacheStatisticsMXBean.increaseCacheHits(hitCount);
				}
				else
				{
					this.cacheStatisticsMXBean.increaseCacheMisses(1);
				}
			}
			
			return result;
		}
		
		@Override
		public boolean replace(final K key, final V value)
		{
			this.ensureOpen();
			
			this.validateKeyValue(key, value);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final long                       now                 = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			final Object                     internalKey         = this.objectConverter.internalize(key);
			boolean                          result;
			
			synchronized(this.cacheTable)
			{
				final CachedValue cachedValue = this.cacheTable.get(internalKey);
				if(cachedValue == null || cachedValue.isExpiredAt(now))
				{
					result = false;
				}
				else
				{
					final V                oldValue = this.objectConverter.externalize(cachedValue.value());
					
					final CacheEntry<K, V> entry    = CacheEntry.New(key, value);
					this.writeCacheEntry(entry);
					
					this.updateExpiryForUpdate(cachedValue, now);
					
					final Object newInternalValue = this.objectConverter.internalize(value);
					cachedValue.value(newInternalValue, now);
					
					eventDispatcher.addEvent(
						CacheEntryUpdatedListener.class,
						new CacheEvent<>(this, EventType.UPDATED, key, value, oldValue)
					);
					
					result = true;
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(isStatisticsEnabled)
			{
				final long duration = System.nanoTime() - start;
				this.cacheStatisticsMXBean.addGetTimeNano(duration);
				if(result)
				{
					this.cacheStatisticsMXBean.increaseCachePuts(1);
					this.cacheStatisticsMXBean.increaseCacheHits(1);
					this.cacheStatisticsMXBean.addPutTimeNano(duration);
				}
				else
				{
					this.cacheStatisticsMXBean.increaseCacheMisses(1);
				}
			}
			
			return result;
		}
		
		@Override
		public V getAndReplace(final K key, final V value)
		{
			this.ensureOpen();
			
			this.validateKeyValue(key, value);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final long                       now                 = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			final Object                     internalKey         = this.objectConverter.internalize(key);
			V                                result;
			
			synchronized(this.cacheTable)
			{
				final CachedValue cachedValue = this.cacheTable.get(internalKey);
				if(cachedValue == null || cachedValue.isExpiredAt(now))
				{
					result = null;
				}
				else
				{
					final V                oldValue = this.objectConverter.externalize(cachedValue.value());
					
					final CacheEntry<K, V> entry    = CacheEntry.New(key, value);
					this.writeCacheEntry(entry);
					
					this.updateExpiryForUpdate(cachedValue, now);
					
					cachedValue.value(
						this.objectConverter.internalize(value),
						now
					);
					
					eventDispatcher.addEvent(
						CacheEntryUpdatedListener.class,
						new CacheEvent<>(this, EventType.UPDATED, key, value, oldValue)
					);
					
					result = oldValue;
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(isStatisticsEnabled)
			{
				final long duration = System.nanoTime() - start;
				this.cacheStatisticsMXBean.addGetTimeNano(duration);
				if(result != null)
				{
					this.cacheStatisticsMXBean.increaseCachePuts(1);
					this.cacheStatisticsMXBean.increaseCacheHits(1);
					this.cacheStatisticsMXBean.addPutTimeNano(duration);
				}
				else
				{
					this.cacheStatisticsMXBean.increaseCacheMisses(1);
				}
			}
			
			return result;
		}
		
		@Override
		public void removeAll(final Set<? extends K> keys)
		{
			this.ensureOpen();
			
			keys.forEach(this::validateKey);
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			final long                       now                 = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			final HashSet<K>                 cacheWriterKeys     = new HashSet<>();
			final HashSet<Object>            deletedKeys         = new HashSet<>();
			cacheWriterKeys.addAll(keys);
			CacheException exception = null;
			
			synchronized(this.cacheTable)
			{
				if(this.cacheWriter != null)
				{
					try
					{
						this.cacheWriter.deleteAll(cacheWriterKeys);
					}
					catch(final CacheWriterException e)
					{
						exception = e;
					}
					catch(final Exception e)
					{
						exception = new CacheWriterException(e);
					}
					
					// At this point, cacheWriterKeys will contain only those that were _not_ written
					// Now delete only those that the writer deleted
					for(final K key : keys)
					{
						// only delete those keys that the writer deleted. per CacheWriter spec.
						if(!cacheWriterKeys.contains(key))
						{
							final Object      internalKey = this.objectConverter.internalize(key);
							final CachedValue cachedValue = this.cacheTable.remove(internalKey);
							if(cachedValue != null)
							{
								deletedKeys.add(key);
								
								final V value = this.objectConverter.externalize(cachedValue.value());
								
								if(cachedValue.isExpiredAt(now))
								{
									this.processExpiries(
										key,
										internalKey,
										eventDispatcher,
										value
									);
								}
								else
								{
									eventDispatcher.addEvent(
										CacheEntryRemovedListener.class,
										new CacheEvent<>(this, EventType.REMOVED, key, value, value)
									);
								}
							}
						}
					}
				}
				else
				{
					for(final K key : keys)
					{
						// only delete those keys that the writer deleted. per CacheWriter spec.
						final Object      internalKey = this.objectConverter.internalize(key);
						final CachedValue cachedValue = this.cacheTable.remove(internalKey);
						if(cachedValue != null)
						{
							deletedKeys.add(key);
							
							final V value = this.objectConverter.externalize(cachedValue.value());
							
							if(cachedValue.isExpiredAt(now))
							{
								this.processExpiries(
									key,
									internalKey,
									eventDispatcher,
									value
								);
							}
							else
							{
								eventDispatcher.addEvent(
									CacheEntryRemovedListener.class,
									new CacheEvent<>(this, EventType.REMOVED, key, value, value)
								);
							}
						}
					}
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(isStatisticsEnabled)
			{
				this.cacheStatisticsMXBean.increaseCacheRemovals(deletedKeys.size());
			}
			
			if(exception != null)
			{
				throw exception;
			}
		}
		
		@Override
		public void removeAll()
		{
			this.ensureOpen();
			
			final boolean                    isStatisticsEnabled = this.isStatisticsEnabled;
			int                              removed             = 0;
			final long                       now                 = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher     = CacheEventDispatcher.New();
			CacheException                   exception           = null;
			
			synchronized(this.cacheTable)
			{
				final HashSet<K> keys = new HashSet<>();
				this.cacheTable.keys().forEach(key -> keys.add(this.objectConverter.externalize(key)));
				final HashSet<K> keysToDelete = new HashSet<>(keys);
				
				if(this.cacheWriter != null && keysToDelete.size() > 0)
				{
					try
					{
						this.cacheWriter.deleteAll(keysToDelete);
					}
					catch(final CacheWriterException e)
					{
						exception = e;
					}
					catch(final Exception e)
					{
						exception = new CacheWriterException(e);
					}
				}
				
				// remove the deleted keys that were successfully deleted from the set
				for(final K key : keys)
				{
					if(!keysToDelete.contains(key))
					{
						final Object      internalKey = this.objectConverter.internalize(key);
						final CachedValue cachedValue = this.cacheTable.remove(internalKey);
						final V           value       = this.objectConverter.externalize(cachedValue.value());
						
						if(cachedValue.isExpiredAt(now))
						{
							this.processExpiries(
								key,
								internalKey,
								eventDispatcher,
								value
							);
						}
						else
						{
							eventDispatcher.addEvent(
								CacheEntryRemovedListener.class,
								new CacheEvent<>(this, EventType.REMOVED, key, value, value)
							);
							removed++;
						}
					}
				}
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			if(isStatisticsEnabled)
			{
				this.cacheStatisticsMXBean.increaseCacheRemovals(removed);
			}
			
			if(exception != null)
			{
				throw exception;
			}
		}
		
		@Override
		public void clear()
		{
			this.ensureOpen();
			
			synchronized(this.cacheTable)
			{
				this.cacheTable.clear();
			}
		}
		
		@Override
		public Iterator<Cache.Entry<K, V>> iterator()
		{
			this.ensureOpen();
			
			return new EntryIterator(this.cacheTable.iterator());
		}
		
		@Override
		public void setStatisticsEnabled(final boolean enabled)
		{
			this.isStatisticsEnabled = enabled;
			if(enabled)
			{
				MBeanServerUtils.registerCacheObject(this, this.cacheStatisticsMXBean);
			}
			else
			{
				MBeanServerUtils.unregisterCacheObject(this, this.cacheStatisticsMXBean);
			}
		}
		
		@Override
		public void setManagementEnabled(final boolean enabled)
		{
			if(enabled)
			{
				MBeanServerUtils.registerCacheObject(this, this.cacheMXBean);
			}
			else
			{
				MBeanServerUtils.unregisterCacheObject(this, this.cacheMXBean);
			}
		}
		
		@Override
		public <T> Map<K, javax.cache.processor.EntryProcessorResult<T>>
			invokeAll(
				final Set<? extends K> keys,
				final EntryProcessor<K, V, T> entryProcessor,
				final Object... arguments)
		{
			this.ensureOpen();
			
			keys.forEach(this::validateKey);
			
			notNull(entryProcessor);
			
			final HashMap<K, javax.cache.processor.EntryProcessorResult<T>> map = new HashMap<>();
			for(final K key : keys)
			{
				EntryProcessorResult<T> result = null;
				try
				{
					final T t = this.invoke(key, entryProcessor, arguments);
					result = t == null
						? null
						: EntryProcessorResult.New(t);
				}
				catch(final Exception e)
				{
					result = EntryProcessorResult.New(e);
				}
				if(result != null)
				{
					map.put(key, result);
				}
			}
			
			return map;
		}
		
		@Override
		public <T> T invoke(final K key, final EntryProcessor<K, V, T> entryProcessor, final Object... arguments)
			throws EntryProcessorException
		{
			this.ensureOpen();
			
			this.validateKey(key);
			
			final boolean isStatisticsEnabled = this.isStatisticsEnabled;
			long          start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			notNull(entryProcessor);
			
			final long                       now             = System.currentTimeMillis();
			final CacheEventDispatcher<K, V> eventDispatcher = CacheEventDispatcher.New();
			final Object                     internalKey     = this.objectConverter.internalize(key);
			T                                result          = null;
			
			synchronized(this.cacheTable)
			{
				final CachedValue cachedValue = this.cacheTable.get(internalKey);
				final boolean     isExpired   = cachedValue != null && cachedValue.isExpiredAt(now);
				
				if(isExpired)
				{
					this.processExpiries(
						key,
						internalKey,
						eventDispatcher,
						this.objectConverter.externalize(cachedValue.value())
					);
				}
				
				if(isStatisticsEnabled)
				{
					if(cachedValue == null || isExpired)
					{
						this.cacheStatisticsMXBean.increaseCacheMisses(1);
					}
					else
					{
						this.cacheStatisticsMXBean.increaseCacheHits(1);
					}
					final long nanoTime = System.nanoTime();
					this.cacheStatisticsMXBean.addGetTimeNano(nanoTime - start);
					// restart
					start = nanoTime;
				}
				
				final MutableCacheEntry<K, V> entry = MutableCacheEntry.New(
					this.objectConverter,
					key,
					cachedValue,
					now,
					this.cacheLoader
				);
				try
				{
					result = entryProcessor.process(entry, arguments);
				}
				catch(final CacheException e)
				{
					throw e;
				}
				catch(final Exception e)
				{
					throw new EntryProcessorException(e);
				}
				
				this.finishInvocation(
					key,
					internalKey,
					cachedValue,
					entry,
					start,
					now,
					eventDispatcher,
					isStatisticsEnabled
				);
			}
			
			eventDispatcher.dispatch(this.listenerRegistrations);
			
			return result;
		}
		
		@SuppressWarnings("incomplete-switch")
		private void finishInvocation(
			final K key,
			final Object internalKey,
			final CachedValue cachedValue,
			final MutableCacheEntry<K, V> entry,
			final long start,
			final long now,
			final CacheEventDispatcher<K, V> eventDispatcher,
			final boolean isStatisticsEnabled)
		{
			switch(entry.getOperation())
			{
				case ACCESS:

					this.updateExpiryForAccess(cachedValue, now);

					break;
				
				case CREATE:
				case LOAD:

					this.finishInvocationCreateLoad(
						key,
						internalKey,
						entry,
						start,
						now,
						eventDispatcher,
						isStatisticsEnabled
					);

					break;
				
				case UPDATE:

					this.finishInvocationUpdate(
						key,
						entry,
						cachedValue,
						start,
						now,
						eventDispatcher,
						isStatisticsEnabled
					);

					break;
				
				case REMOVE:

					this.finishInvocationRemove(
						key,
						internalKey,
						cachedValue,
						start,
						eventDispatcher,
						isStatisticsEnabled
					);

					break;
			}
		}
		
		private void finishInvocationCreateLoad(
			final K key,
			final Object internalKey,
			final MutableCacheEntry<K, V> entry,
			final long start,
			final long now,
			final CacheEventDispatcher<K, V> eventDispatcher,
			final boolean isStatisticsEnabled)
		{
			CachedValue            cachedValue;
			final CacheEntry<K, V> e          = CacheEntry.New(key, entry.getValue());
			
			if(entry.getOperation() == MutableCacheEntry.Operation.CREATE)
			{
				this.writeCacheEntry(e);
			}
			
			cachedValue = CachedValue.New(
				this.objectConverter.internalize(entry.getValue()),
				now,
				this.expiryForCreation().getAdjustedTime(now)
			);
			
			if(cachedValue.isExpiredAt(now))
			{
				final V previousValue = this.objectConverter.externalize(cachedValue.value());
				this.processExpiries(
					key,
					internalKey,
					eventDispatcher,
					previousValue
				);
			}
			else
			{
				this.putValue(
					key,
					entry.getValue(),
					internalKey,
					cachedValue,
					eventDispatcher,
					isStatisticsEnabled
				);
				
				// do not count LOAD as a put for cache statistics.
				if(isStatisticsEnabled && entry.getOperation() == MutableCacheEntry.Operation.CREATE)
				{
					this.cacheStatisticsMXBean.increaseCachePuts(1);
					this.cacheStatisticsMXBean.addPutTimeNano(System.nanoTime() - start);
				}
			}
		}
		
		private void finishInvocationUpdate(
			final K key,
			final MutableCacheEntry<K, V> entry,
			final CachedValue cachedValue,
			final long start,
			final long now,
			final CacheEventDispatcher<K, V> eventDispatcher,
			final boolean isStatisticsEnabled)
		{
			final V                oldValue = this.objectConverter.externalize(cachedValue.value());
			
			final CacheEntry<K, V> e        = CacheEntry.New(key, entry.getValue());
			this.writeCacheEntry(e);
			
			this.updateExpiryForUpdate(cachedValue, now);
			
			cachedValue.value(
				this.objectConverter.internalize(entry.getValue()),
				now
			);
			
			eventDispatcher.addEvent(
				CacheEntryUpdatedListener.class,
				new CacheEvent<>(this, EventType.UPDATED, key, entry.getValue(), oldValue)
			);
			
			if(isStatisticsEnabled)
			{
				this.cacheStatisticsMXBean.increaseCachePuts(1);
				this.cacheStatisticsMXBean.addPutTimeNano(System.nanoTime() - start);
			}
		}
		
		private void finishInvocationRemove(
			final K key,
			final Object internalKey,
			final CachedValue cachedValue,
			final long start,
			final CacheEventDispatcher<K, V> eventDispatcher,
			final boolean isStatisticsEnabled)
		{
			this.deleteCacheEntry(key);
			
			final V oldValue = cachedValue == null
				? null
				: this.objectConverter.externalize(cachedValue.value());
			this.cacheTable.remove(internalKey);
			
			eventDispatcher.addEvent(
				CacheEntryRemovedListener.class,
				new CacheEvent<>(this, EventType.REMOVED, key, oldValue, oldValue)
			);
			
			if(isStatisticsEnabled)
			{
				this.cacheStatisticsMXBean.increaseCacheRemovals(1);
				this.cacheStatisticsMXBean.addRemoveTimeNano(System.nanoTime() - start);
			}
		}
		
		private void ensureOpen()
		{
			if(this.isClosed)
			{
				throw new IllegalStateException("Cache is closed");
			}
		}
		
		private void validateKeyValue(final K key, final V value)
		{
			this.validateKey(key);
			this.validateValue(value);
		}
		
		private void validateKey(final K key)
		{
			if(key == null)
			{
				throw new NullPointerException("key cannot be null");
			}
			
			final Class<?> keyType = this.configuration.getKeyType();
			if(Object.class != keyType && !keyType.isAssignableFrom(key.getClass()))
			{
				throw new ClassCastException("Type mismatch for key: " + key + " <> " + keyType.getName());
			}
		}
		
		private void validateValue(final V value)
		{
			if(value == null)
			{
				throw new NullPointerException("value cannot be null");
			}
			
			final Class<?> valueType = this.configuration.getValueType();
			if(Object.class != valueType && !valueType.isAssignableFrom(value.getClass()))
			{
				throw new ClassCastException("Type mismatch for value: " + value + " <> " + valueType.getName());
			}
		}
		
		private V getValue(final K key, final CacheEventDispatcher<K, V> eventDispatcher)
		{
			final boolean isStatisticsEnabled = this.isStatisticsEnabled;
			final long    start               = isStatisticsEnabled
				? System.nanoTime()
				: 0;
			
			final long    now                 = System.currentTimeMillis();
			final Object  internalKey         = this.objectConverter.internalize(key);
			V             value               = null;
			
			synchronized(this.cacheTable)
			{
				CachedValue   cachedValue = this.cacheTable.get(internalKey);
				final boolean isExpired   = cachedValue != null && cachedValue.isExpiredAt(now);
				
				if(cachedValue == null || isExpired)
				{
					if(isExpired)
					{
						this.processExpiries(
							key,
							internalKey,
							eventDispatcher,
							this.objectConverter.externalize(cachedValue.value())
						);
					}
					
					if(isStatisticsEnabled)
					{
						this.cacheStatisticsMXBean.increaseCacheMisses(1);
					}
					
					value = this.loadCacheEntry(key, value);
					
					if(value != null)
					{
						cachedValue = CachedValue.New(
							this.objectConverter.internalize(value),
							now,
							this.expiryForCreation().getAdjustedTime(now)
						);
						
						if(cachedValue.isExpiredAt(now))
						{
							value = null;
						}
						else
						{
							this.putValue(
								key,
								value,
								internalKey,
								cachedValue,
								eventDispatcher,
								isStatisticsEnabled
							);
						}
					}
				}
				else
				{
					value = this.objectConverter.externalize(cachedValue.value(now));
					this.updateExpiryForAccess(cachedValue, now);
					
					if(isStatisticsEnabled)
					{
						this.cacheStatisticsMXBean.increaseCacheHits(1);
					}
				}
			}
			
			if(isStatisticsEnabled)
			{
				this.cacheStatisticsMXBean.addGetTimeNano(System.nanoTime() - start);
			}
			
			return value;
		}
		
		private void putValue(
			final K                          key,
			final V                          value,
			final Object                     internalKey,
			final CachedValue                cachedValue,
			final CacheEventDispatcher<K, V> eventDispatcher,
			final boolean                    isStatisticsEnabled
		)
		{
			final boolean newEntry = this.cacheTable.put(internalKey, cachedValue);
			
			eventDispatcher.addEvent(
				CacheEntryCreatedListener.class,
				new CacheEvent<>(this, EventType.CREATED, key, value)
			);
			
			if(newEntry)
			{
				KeyValue<Object, CachedValue> entryToEvict;
				if(this.evictionPolicy != null
					&& (entryToEvict = this.evictionPolicy.pickEntryToEvict(this.cacheTable)) != null
					&& !entryToEvict.key().equals(internalKey))
				{
					this.cacheTable.remove(entryToEvict.key());
					
					final K evictedKey   = this.objectConverter.externalize(entryToEvict.key());
					final V evictedValue = this.objectConverter.externalize(entryToEvict.value().value());
					
					this.deleteCacheEntry(evictedKey);
					
					eventDispatcher.addEvent(
						CacheEntryRemovedListener.class,
						new CacheEvent<>(this, EventType.REMOVED, evictedKey, evictedValue, evictedValue)
					);
					
					if(isStatisticsEnabled)
					{
						this.cacheStatisticsMXBean.increaseCacheEvictions(1);
					}
				}
			}
		}
		
		private void updateExpiryForAccess(final CachedValue cachedValue, final long now)
		{
			try
			{
				final Duration duration = this.expiryPolicy.getExpiryForAccess();
				if(duration != null)
				{
					cachedValue.expiryTime(duration.getAdjustedTime(now));
				}
			}
			catch(final Throwable t)
			{
				// Spec says leave the expiry time untouched when we can't determine a duration
			}
		}
		
		private void updateExpiryForUpdate(final CachedValue cachedValue, final long now)
		{
			try
			{
				final Duration duration = this.expiryPolicy.getExpiryForUpdate();
				if(duration != null)
				{
					cachedValue.expiryTime(duration.getAdjustedTime(now));
				}
			}
			catch(final Throwable t)
			{
				// Spec says leave the expiry time untouched when we can't determine a duration
			}
		}
		
		private void processExpiries(
			final K key,
			final Object internalKey,
			final CacheEventDispatcher<K, V> dispatcher,
			final V expiredValue)
		{
			this.cacheTable.remove(internalKey);
			dispatcher.addEvent(
				CacheEntryExpiredListener.class,
				new CacheEvent<>(this, EventType.EXPIRED, key, expiredValue, expiredValue)
			);
		}
		
		private Duration expiryForCreation()
		{
			// Spec says if exception happens, a default duration should be used.
			try
			{
				return this.expiryPolicy.getExpiryForCreation();
			}
			catch(final Throwable t)
			{
				return this.defaultDuration();
			}
		}
		
		private Duration defaultDuration()
		{
			return Duration.ETERNAL;
		}
		
		private void submit(final Runnable task)
		{
			this.executorService.submit(task);
		}
		
		private V loadCacheEntry(final K key, final V value)
		{
			if(this.cacheLoader == null)
			{
				return value;
			}

			try
			{
				return this.cacheLoader.load(key);
			}
			catch(final CacheWriterException e)
			{
				throw e;
			}
			catch(final Exception e)
			{
				throw new CacheWriterException(e);
			}
		}
		
		private void writeCacheEntry(final CacheEntry<K, V> entry)
		{
			if(this.cacheWriter != null)
			{
				try
				{
					this.cacheWriter.write(entry);
				}
				catch(final CacheWriterException e)
				{
					throw e;
				}
				catch(final Exception e)
				{
					throw new CacheWriterException(e);
				}
			}
		}
		
		private void deleteCacheEntry(final K key)
		{
			if(this.cacheWriter != null)
			{
				try
				{
					this.cacheWriter.delete(key);
				}
				catch(final CacheWriterException e)
				{
					throw e;
				}
				catch(final Exception e)
				{
					throw new CacheWriterException(e);
				}
			}
		}
		
		private void closeIfCloseable(final Object obj)
		{
			if(obj instanceof Closeable)
			{
				try
				{
					((Closeable)obj).close();
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}
			}
		}
		
		private final class EntryIterator implements Iterator<Cache.Entry<K, V>>
		{
			private final Iterator<KeyValue<Object, CachedValue>> iterator;
			private CacheEntry<K, V>                              nextEntry;
			private CacheEntry<K, V>                              lastEntry;
			private final long                                    now;
			private final boolean                                 isStatisticsEnabled;
			
			EntryIterator(final Iterator<KeyValue<Object, CachedValue>> iterator)
			{
				this.iterator            = iterator;
				this.nextEntry           = null;
				this.lastEntry           = null;
				this.now                 = System.currentTimeMillis();
				this.isStatisticsEnabled = Cache.Default.this.isStatisticsEnabled;
			}
			
			private void fetch()
			{
				final long start = this.isStatisticsEnabled
					? System.nanoTime()
					: 0;
					
				final ObjectConverter objectConverter = Cache.Default.this.objectConverter;
					
				while(this.nextEntry == null && this.iterator.hasNext())
				{
					final KeyValue<Object, CachedValue> entry       = this.iterator.next();
					final CachedValue                   cachedValue = entry.value();
					final K                             key         = objectConverter.externalize(entry.key());
					try
					{
						if(!cachedValue.isExpiredAt(this.now))
						{
							final V value  = objectConverter.externalize(cachedValue.value(this.now));
							this.nextEntry = CacheEntry.New(key, value);
							
							try
							{
								Cache.Default.this.updateExpiryForAccess(cachedValue, this.now);
							}
							catch(final Throwable t)
							{
								// Spec says leave the expiry time untouched when we can't determine a duration
							}
						}
					}
					finally
					{
						if(this.isStatisticsEnabled && this.nextEntry != null)
						{
							Cache.Default.this.cacheStatisticsMXBean.increaseCacheHits(1);
							Cache.Default.this.cacheStatisticsMXBean.addGetTimeNano(System.nanoTime() - start);
						}
					}
				}
			}
			
			@Override
			public boolean hasNext()
			{
				if(this.nextEntry == null)
				{
					this.fetch();
				}
				return this.nextEntry != null;
			}
			
			@Override
			public Entry<K, V> next()
			{
				if(this.hasNext())
				{
					// remember the lastEntry (so that we call allow for removal)
					this.lastEntry = this.nextEntry;
					
					// reset nextEntry to force fetching the next available entry
					this.nextEntry = null;
					
					return this.lastEntry;
				}
				else
				{
					throw new NoSuchElementException();
				}
			}
			
			@Override
			public void remove()
			{
				if(this.lastEntry == null)
				{
					throw new IllegalStateException("Must progress to the next entry to remove");
				}
				
				final long start         = this.isStatisticsEnabled
					? System.nanoTime()
					: 0;
				int        cacheRemovals = 0;
				try
				{
					Cache.Default.this.deleteCacheEntry(this.lastEntry.getKey());
					
					/*
					 * NOTE: There is the possibility here that the entry the application retrieved may have been
					 * replaced / expired or already removed since it retrieved it. We simply don't care here as
					 * multiple-threads are ok to remove and see such side-effects.
					 */
					this.iterator.remove();
					cacheRemovals++;
					
					// raise "remove" event
					final CacheEventDispatcher<K, V> dispatcher = CacheEventDispatcher.New();
					dispatcher.addEvent(
						CacheEntryRemovedListener.class,
						new CacheEvent<>(Cache.Default.this, EventType.REMOVED, this.lastEntry.getKey(),
							this.lastEntry.getValue(), this.lastEntry.getValue()
						)
					);
					dispatcher.dispatch(Cache.Default.this.listenerRegistrations);
				}
				finally
				{
					// reset lastEntry (we can't attempt to remove it again)
					this.lastEntry = null;
					if(this.isStatisticsEnabled && cacheRemovals > 0)
					{
						Cache.Default.this.cacheStatisticsMXBean.increaseCacheRemovals(cacheRemovals);
						Cache.Default.this.cacheStatisticsMXBean.addRemoveTimeNano(System.nanoTime() - start);
					}
				}
			}
			
		}
		
	}
	
}
