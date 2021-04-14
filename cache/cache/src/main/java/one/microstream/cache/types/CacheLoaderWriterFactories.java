package one.microstream.cache.types;

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

import one.microstream.X;
import one.microstream.storage.configuration.Configuration;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

/**
 * 
 * @deprecated will be removed in a future release
 */
@Deprecated
public interface CacheLoaderWriterFactories<K, V>
{
	public Factory<CacheLoader<K, V>> loaderFactory();
	
	public Factory<CacheWriter<? super K, ? super V>> writerFactory();
	
	
	public static <K, V> CacheLoaderWriterFactories<K, V> New(
		final Configuration storageConfiguration
	)
	{
		return new ConfigurationBased<>(X.notNull(storageConfiguration));
	}
	
	
	public static class ConfigurationBased<K, V> implements CacheLoaderWriterFactories<K, V>
	{
		private final Configuration    storageConfiguration;
		private       CacheStore<K, V> cacheStore;

		ConfigurationBased(
			final Configuration storageConfiguration
		)
		{
			super();
			this.storageConfiguration = storageConfiguration;
		}
		
		protected CacheStore<K, V> cacheStore()
		{
			if(this.cacheStore == null)
			{
				final EmbeddedStorageManager storageManager = this.storageConfiguration
					.createEmbeddedStorageFoundation()
					.start()
				;
				this.cacheStore = CacheStore.New(
					"cache",
					storageManager
				);
			}
			
			return this.cacheStore;
		}

		@Override
		public Factory<CacheLoader<K, V>> loaderFactory()
		{
			return this::cacheStore;
		}

		@Override
		public Factory<CacheWriter<? super K, ? super V>> writerFactory()
		{
			return this::cacheStore;
		}
		
	}
}
