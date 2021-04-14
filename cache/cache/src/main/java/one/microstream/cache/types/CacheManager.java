
package one.microstream.cache.types;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Properties;

import javax.cache.CacheException;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;

import one.microstream.collections.EqHashTable;


/**
 * JSR-107 compliant {@link javax.cache.CacheManager}.
 *
 */
public interface CacheManager extends javax.cache.CacheManager
{
	@Override
	public <K, V> Cache<K, V> getCache(String cacheName);

	@Override
	public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType);

	@Override
	public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration)
		throws IllegalArgumentException;

	@Override
	public CachingProvider getCachingProvider();

	public void removeCache(final String cacheName);

	@Override
	public default <T> T unwrap(final Class<T> clazz)
	{
		return Unwrappable.Static.unwrap(this, clazz);
	}

	static CacheManager New(
		final CachingProvider cachingProvider,
		final URI uri,
		final ClassLoader classLoader,
		final Properties properties
	)
	{
		return new Default(
			cachingProvider,
			uri,
			classLoader,
			properties
		);
	}

	public static class Default implements CacheManager
	{
		private final CachingProvider                  cachingProvider;
		private final URI                              uri;
		private final WeakReference<ClassLoader>       classLoaderReference;
		private final Properties                       properties;
		private final EqHashTable<String, Cache<?, ?>> caches   = EqHashTable.New();
		private volatile boolean                       isClosed = false;

		Default(
			final CachingProvider cachingProvider,
			final URI uri,
			final ClassLoader classLoader,
			final Properties properties)
		{
			super();

			this.cachingProvider      = notNull(cachingProvider);
			this.uri                  = notNull(uri);
			this.classLoaderReference = new WeakReference<>(notNull(classLoader));
			this.properties           = new Properties();
			if(properties != null)
			{
				this.properties.putAll(properties);
			}
		}

		@Override
		public CachingProvider getCachingProvider()
		{
			return this.cachingProvider;
		}

		@Override
		public boolean isClosed()
		{
			return this.isClosed;
		}

		@Override
		public URI getURI()
		{
			return this.uri;
		}

		@Override
		public Properties getProperties()
		{
			return this.properties;
		}

		@Override
		public ClassLoader getClassLoader()
		{
			return this.classLoaderReference.get();
		}

		@Override
		public <K, V, C extends Configuration<K, V>> Cache<K, V>
			createCache(final String cacheName, final C configuration) throws IllegalArgumentException
		{
			notEmpty(cacheName);
			notNull(configuration);

			if(this.getCache(cacheName) != null)
			{
				throw new CacheException("A cache named " + cacheName + " already exists.");
			}

			synchronized(this.caches)
			{
				final Cache<K, V> cache = Cache.New(
					cacheName,
					this,
					CacheConfiguration.New(configuration)
				);
				this.caches.put(cacheName, cache);
				return cache;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <K, V> Cache<K, V> getCache(final String cacheName)
		{
			this.ensureOpen();

			notNull(cacheName);

			synchronized(this.caches)
			{
				return (Cache<K, V>)this.caches.get(cacheName);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <K, V> Cache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType)
		{
			this.ensureOpen();

			notNull(keyType);
			notNull(valueType);

			Cache<K, V> cache;
			synchronized(this.caches)
			{
				cache = (Cache<K, V>)this.caches.get(notNull(cacheName));
			}
			if(cache == null)
			{
				return null;
			}

			final CompleteConfiguration<K, V> configuration       = cache.getConfiguration(CompleteConfiguration.class);
			final Class<K>                    configuredKeyType   = configuration.getKeyType();
			final Class<V>                    configuredValueType = configuration.getValueType();
			if(configuredKeyType != null && !configuredKeyType.equals(keyType))
			{
				throw new ClassCastException("Incompatible key types: " + keyType + " <> " + configuredKeyType);
			}
			if(configuredValueType != null && !configuredValueType.equals(valueType))
			{
				throw new ClassCastException("Incompatible value types: " + valueType + " <> " + configuredValueType);
			}

			return cache;
		}

		@Override
		public Iterable<String> getCacheNames()
		{
			this.ensureOpen();

			synchronized(this.caches)
			{
				return this.caches.keys().immure();
			}
		}

		@SuppressWarnings("resource")
		@Override
		public void destroyCache(final String cacheName)
		{
			this.ensureOpen();

			Cache<?, ?> cache;
			synchronized(this.caches)
			{
				cache = this.caches.get(notNull(cacheName));
			}
			if(cache != null)
			{
				cache.close();
			}
		}

		@Override
		public void removeCache(final String cacheName)
		{
			notNull(cacheName);

			synchronized(this.caches)
			{
				this.caches.removeFor(cacheName);
			}
		}

		@Override
		public void enableManagement(final String cacheName, final boolean enabled)
		{
			this.ensureOpen();

			notNull(cacheName);

			synchronized(this.caches)
			{
				this.caches.get(cacheName)
					.setManagementEnabled(enabled);
			}
		}

		@Override
		public void enableStatistics(final String cacheName, final boolean enabled)
		{
			this.ensureOpen();

			notNull(cacheName);

			synchronized(this.caches)
			{
				this.caches.get(cacheName)
					.setStatisticsEnabled(enabled);
			}
		}

		@Override
		public synchronized void close()
		{
			if(this.isClosed)
			{
				// no-op, according to spec
				return;
			}

			this.isClosed = true;

			this.cachingProvider.remove(
				this.getURI(),
				this.getClassLoader()
			);

			try
			{
				for(final Cache<?, ?> cache : this.caches.values())
				{
					try
					{
						cache.close();
					}
					catch(final Exception e)
					{
						// ignore, according to spec
					}
				}
			}
			finally
			{
				this.caches.clear();
			}
		}

		private void ensureOpen()
		{
			if(this.isClosed)
			{
				throw new IllegalStateException("CacheManager is closed");
			}
		}

	}

}
