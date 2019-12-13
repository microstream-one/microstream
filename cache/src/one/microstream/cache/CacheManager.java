
package one.microstream.cache;

import static one.microstream.X.notNull;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Properties;

import javax.cache.configuration.Configuration;

import one.microstream.collections.EqHashTable;


public interface CacheManager extends javax.cache.CacheManager
{
	@Override
	public default <T> T unwrap(final Class<T> clazz)
	{
		return Unwrappable.Static.unwrap(this, clazz);
	}
	
	static CacheManager New(
		final CachingProvider cachingProvider,
		final URI uri,
		final ClassLoader classLoader,
		final Properties properties)
	{
		return new Default(cachingProvider, uri, classLoader, properties);
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
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public <K, V> Cache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public <K, V> Cache<K, V> getCache(final String cacheName)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Iterable<String> getCacheNames()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void destroyCache(final String cacheName)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void enableManagement(final String cacheName, final boolean enabled)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void enableStatistics(final String cacheName, final boolean enabled)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void close()
		{
			// TODO Auto-generated method stub
			
		}
	}
}
