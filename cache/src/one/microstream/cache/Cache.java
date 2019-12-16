
package one.microstream.cache;

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.event.EventType;
import javax.cache.expiry.Duration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;

import one.microstream.cache.MBeanServerRegistrationUtility.ObjectNameType;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XList;


public interface Cache<K, V> extends javax.cache.Cache<K, V>, Unwrappable
{
	@Override
	public CacheManager getCacheManager();
	
	public long size();
	
	public void setManagementEnabled(boolean enabled);
	
	public void setStatisticsEnabled(boolean enabled);
	
	public CacheMXBean getCacheMXBean();
	
	public CacheStatisticsMXBean getCacheStatisticsMXBean();
	
	@Override
	public default <T> T unwrap(final Class<T> clazz)
	{
		return Unwrappable.Static.unwrap(this, clazz);
	}
	
	public static class Default<K, V> implements Cache<K, V>
	{
		private final String                                name;
		private final CacheManager                          manager;
		private final MutableConfiguration<K, V>            configuration;
		private ObjectConverter                             objectConverter;
		private CacheLoader<K, V>                           cacheLoader;
		private CacheWriter<? super K, ? super V>           cacheWriter;
		private boolean                                     isReadThrough;
		private boolean                                     isWriteThrough;
		private ExpiryPolicy                                expiryPolicy;
		private XList<CacheEntryListenerRegistration<K, V>> listenerRegistrations;
		private CacheMXBean                                 cacheMXBean;
		private CacheStatisticsMXBean                       cacheStatisticsMXBean;
		private EqHashTable<Object, CachedValue>            hashTable;
		private volatile boolean                            isClosed = false;
		
		Default(
			final String name,
			final CacheManager manager,
			final Configuration<K, V> configuration,
			final ClassLoader classLoader)
		{
			super();
			
			this.name          = name;
			this.manager       = manager;
			
			/*
			 * We don't know if configuration is mutable, so we make a defense copy.
			 */
			this.configuration = this.copyConfiguration(configuration);
			
			this.init(classLoader);
		}
		
		private MutableConfiguration<K, V> copyConfiguration(final Configuration<K, V> configuration)
		{
			if(configuration instanceof CompleteConfiguration)
			{
				return new MutableConfiguration<>((CompleteConfiguration<K, V>)configuration);
			}
			
			final MutableConfiguration<K, V> mutableConfiguration = new MutableConfiguration<>();
			mutableConfiguration.setStoreByValue(configuration.isStoreByValue());
			mutableConfiguration.setTypes(configuration.getKeyType(), configuration.getValueType());
			return new MutableConfiguration<>(mutableConfiguration);
		}
		
		@SuppressWarnings("unchecked")
		void init(final ClassLoader classLoader)
		{
			this.objectConverter = this.configuration.isStoreByValue()
				? ObjectConverter.ByValue(Serializer.get(classLoader))
				: ObjectConverter.ByReference();
			
			final Factory<CacheLoader<K, V>> cacheLoaderFactory;
			if((cacheLoaderFactory = this.configuration.getCacheLoaderFactory()) != null)
			{
				this.cacheLoader = cacheLoaderFactory.create();
			}
			final Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory;
			if((cacheWriterFactory = this.configuration.getCacheWriterFactory()) != null)
			{
				this.cacheWriter = cacheWriterFactory.create();
			}
			
			this.isReadThrough  = this.configuration.isReadThrough() && this.cacheLoader != null;
			this.isWriteThrough = this.configuration.isWriteThrough() && this.cacheWriter != null;
			
			Factory<ExpiryPolicy> expiryPolicyFactory;
			this.expiryPolicy          = (expiryPolicyFactory = this.configuration.getExpiryPolicyFactory()) != null
				? expiryPolicyFactory.create()
				: new EternalExpiryPolicy();
			
			this.listenerRegistrations = BulkList.New();
			
			this.cacheMXBean           = CacheMXBean.New(this);
			this.cacheStatisticsMXBean = CacheStatisticsMXBean.New(this);
			
			if(this.configuration.isManagementEnabled())
			{
				this.setManagementEnabled(true);
			}
			if(this.configuration.isStatisticsEnabled())
			{
				this.setStatisticsEnabled(true);
			}
			
			this.hashTable = EqHashTable.New();
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
		public <C extends Configuration<K, V>> C getConfiguration(final Class<C> clazz)
		{
			if(clazz.isInstance(this.configuration))
			{
				return clazz.cast(this.configuration);
			}
			
			throw new IllegalArgumentException("Unsupported configuration type: " + clazz.getName());
		}
		
		@Override
		public boolean isClosed()
		{
			return this.isClosed;
		}
		
		@Override
		public void put(final K key, final V value)
		{
			final boolean statisticsEnabled = this.isStatisticsEnabled();
			final long    start             = statisticsEnabled
				? System.nanoTime()
				: 0;
			
			this.ensureOpen();
			
			this.validate(key, value);
			
			final CacheEventDispatcher<K, V> eventDispatcher = CacheEventDispatcher.New();
			int                              putCount        = 0;
			final long                       now             = System.currentTimeMillis();
			final Object                     internalKey     = this.objectConverter.toInternal(key);
			final Object                     internalValue   = this.objectConverter.toInternal(value);
			
			synchronized(this.hashTable)
			{
				CachedValue   cachedValue       = this.hashTable.get(internalKey);
				final boolean isOldEntryExpired = cachedValue != null && cachedValue.isExpiredAt(now);
				
				if(isOldEntryExpired)
				{
					final V expiredValue = this.objectConverter.fromInternal(cachedValue.value());
					this.processExpiries(key, internalKey, eventDispatcher, expiredValue);
				}
				
				final CacheEntry<K, V> entry = CacheEntry.New(key, value);
				
				if(cachedValue == null || isOldEntryExpired)
				{
					final long expiryTime = this.expiryForCreation().getAdjustedTime(now);
					
					cachedValue = CachedValue.New(internalValue, now, expiryTime);
					
					// todo #32 writes should not happen on a new expired entry
					this.writeCacheEntry(entry);
					
					// check that new entry is not already expired, in which case it should
					// not be added to the cache or listeners called or writers called.
					if(cachedValue.isExpiredAt(now))
					{
						this.processExpiries(key, internalKey, eventDispatcher,
							this.objectConverter.fromInternal(cachedValue.value()));
					}
					else
					{
						this.hashTable.put(internalKey, cachedValue);
						putCount++;
						eventDispatcher.addEvent(CacheEntryCreatedListener.class,
							new CacheEvent<>(this, EventType.CREATED, key, value));
					}
				}
				else
				{
					final V oldValue = this.objectConverter.fromInternal(cachedValue.value());
					
					this.writeCacheEntry(entry);
					this.updateExpiry(cachedValue, now);
					
					cachedValue.value(internalValue, now);
					putCount++;
					
					eventDispatcher.addEvent(CacheEntryUpdatedListener.class,
						new CacheEvent<>(this, EventType.UPDATED, key, value, oldValue));
				}
				
				eventDispatcher.dispatch(this.listenerRegistrations);
			}
			
			if(statisticsEnabled && putCount > 0)
			{
				this.cacheStatisticsMXBean.increaseCachePuts(putCount);
				this.cacheStatisticsMXBean.addPutTimeNano(System.nanoTime() - start);
			}
		}
		
		private void updateExpiry(final CachedValue cachedValue, final long now)
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
				// XXX Spec says leave the expiry time untouched when we can't determine a duration
			}
		}
		
		private void processExpiries(
			final K key,
			final Object internalKey,
			final CacheEventDispatcher<K, V> dispatcher,
			final V expiredValue)
		{
			this.hashTable.removeFor(internalKey);
			dispatcher.addEvent(
				CacheEntryExpiredListener.class,
				new CacheEvent<>(this, EventType.EXPIRED, key, expiredValue, expiredValue));
		}
		
		@Override
		public CacheMXBean getCacheMXBean()
		{
			return this.cacheMXBean;
		}
		
		@Override
		public CacheStatisticsMXBean getCacheStatisticsMXBean()
		{
			return this.cacheStatisticsMXBean;
		}
		
		@Override
		public void setStatisticsEnabled(final boolean enabled)
		{
			if(enabled)
			{
				MBeanServerRegistrationUtility.registerCacheObject(this, ObjectNameType.Statistics);
			}
			else
			{
				MBeanServerRegistrationUtility.unregisterCacheObject(this, ObjectNameType.Statistics);
			}
			this.configuration.setStatisticsEnabled(enabled);
		}
		
		@Override
		public void setManagementEnabled(final boolean enabled)
		{
			if(enabled)
			{
				MBeanServerRegistrationUtility.registerCacheObject(this, ObjectNameType.Configuration);
			}
			else
			{
				MBeanServerRegistrationUtility.unregisterCacheObject(this, ObjectNameType.Configuration);
			}
			this.configuration.setManagementEnabled(enabled);
		}
		
		private void ensureOpen()
		{
			if(this.isClosed())
			{
				throw new IllegalStateException("Cache is closed");
			}
		}
		
		@SuppressWarnings("unchecked")
		private boolean isStatisticsEnabled()
		{
			return this.getConfiguration(CompleteConfiguration.class).isStatisticsEnabled();
		}
		
		private void validate(final K key, final V value)
		{
			if(key == null)
			{
				throw new NullPointerException("key cannot be null");
			}
			if(value == null)
			{
				throw new NullPointerException("value cannot be null");
			}
			
			final Class<?> keyType   = this.configuration.getKeyType();
			final Class<?> valueType = this.configuration.getValueType();
			if(Object.class != keyType && !keyType.isAssignableFrom(key.getClass()))
			{
				throw new ClassCastException("Type mismatch for key: " + key + " <> " + keyType.getName());
			}
			if(Object.class != valueType && !valueType.isAssignableFrom(value.getClass()))
			{
				throw new ClassCastException("Type mismatch for value: " + value + " <> " + valueType.getName());
			}
		}
		
		private Duration expiryForCreation()
		{
			// XXX Spec says if exception happens, a default duration should be used.
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
		
		private void writeCacheEntry(final CacheEntry<K, V> entry)
		{
			if(this.isWriteThrough)
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
			if(this.isWriteThrough)
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
		
	}
	
}
