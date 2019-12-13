
package one.microstream.cache;

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;

import one.microstream.collections.EqHashTable;


public interface Cache<K, V> extends javax.cache.Cache<K, V>, Unwrappable
{
	@Override
	public CacheManager getCacheManager();
	
	@Override
	public default <T> T unwrap(final Class<T> clazz)
	{
		return Unwrappable.Static.unwrap(this, clazz);
	}
	
	public static class Default<K, V> implements Cache<K, V>
	{
		private final String              name;
		private final CacheManager        manager;
		private final Configuration<K, V> configuration;
		private final ClassLoader         classLoader;
		private volatile boolean          isClosed  = false;
		private final EqHashTable<K, V>   hashTable = EqHashTable.New();
		
		Default(
			final String name,
			final CacheManager manager,
			final Configuration<K, V> configuration,
			final ClassLoader classLoader)
		{
			super();
			
			this.name          = name;
			this.manager       = manager;
			this.configuration = configuration;
			this.classLoader   = classLoader;
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
			// final long start = this.isStatisticsEnabled()
			// ? System.nanoTime()
			// : 0;
			
			this.ensureOpen();
			
			this.validate(key, value);
			
			synchronized(this.hashTable)
			{
				
			}
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
	}
}
