package one.microstream.cache;

import static one.microstream.X.notNull;

import javax.cache.CacheException;
import javax.cache.configuration.Factory;

import one.microstream.chars.XChars;
import one.microstream.configuration.types.Configuration;
import one.microstream.storage.types.EmbeddedStorageFoundationCreatorConfigurationBased;
import one.microstream.storage.types.EmbeddedStorageManager;

public interface CacheConfigurationBuilderConfigurationBased
{
	@FunctionalInterface
	public static interface ClassResolver
	{
		public Class<?> loadClass(String name) throws ClassNotFoundException;


		public static ClassResolver New()
		{
			return Class::forName;
		}
	}


	public CacheConfiguration.Builder<?, ?> buildCacheConfiguration(
		Configuration configuration
	);
	
	public <K, V> CacheConfiguration.Builder<K, V> buildCacheConfiguration(
		Configuration configuration, CacheConfiguration.Builder<K, V> builder
	);
	
	
	public static CacheConfigurationBuilderConfigurationBased New()
	{
		return new CacheConfigurationBuilderConfigurationBased.Default(
			ClassResolver.New()
		);
	}
	
	public static CacheConfigurationBuilderConfigurationBased New(
		final ClassResolver classResolver
	)
	{
		return new CacheConfigurationBuilderConfigurationBased.Default(
			notNull(classResolver)
		);
	}
	
	
	public static class Default implements
	CacheConfigurationBuilderConfigurationBased,
	CacheConfigurationPropertyNames
	{
		private final ClassResolver classResolver;

		Default(
			final ClassResolver classResolver
		)
		{
			super();
			this.classResolver = classResolver;
		}
		
		@Override
		public CacheConfiguration.Builder<?, ?> buildCacheConfiguration(
			final Configuration configuration
		)
		{
			final CacheConfiguration.Builder<?, ?> builder = CacheConfiguration.Builder(
				this.getClass(configuration, KEY_TYPE  ),
				this.getClass(configuration, VALUE_TYPE)
			);
			
			return this.buildCacheConfiguration(configuration, builder);
		}
		
		@Override
		public <K, V> CacheConfiguration.Builder<K, V> buildCacheConfiguration(
			final Configuration                    configuration,
			final CacheConfiguration.Builder<K, V> builder
		)
		{
			configuration.opt(EXPIRY_POLICY_FACTORY).ifPresent(value ->
				builder.expiryPolicyFactory(this.valueAsFactory(value))
			);
			configuration.opt(EVICTION_MANAGER_FACTORY).ifPresent(value ->
				builder.evictionManagerFactory(this.valueAsFactory(value))
			);
			configuration.optBoolean(STORE_BY_VALUE).ifPresent(value ->
				builder.storeByValue(value)
			);
			configuration.optBoolean(STATISTICS_ENABLED).ifPresent(value ->
				builder.enableStatistics(value)
			);
			configuration.optBoolean(MANAGEMENT_ENABLED).ifPresent(value ->
				builder.enableManagement(value)
			);

			final Configuration storageConfiguration = configuration.child(STORAGE);
			if(storageConfiguration != null)
			{
				final EmbeddedStorageManager storageManager = EmbeddedStorageFoundationCreatorConfigurationBased
					.New(storageConfiguration)
					.createEmbeddedStorageFoundation()
					.createEmbeddedStorageManager()
				;
				final String cacheKey = storageConfiguration.opt(STORAGE_KEY)
					.orElse(CachingProvider.defaultURI() + "::cache")
				;
				final CacheStore<K, V> cacheStore = CacheStore.New(cacheKey, storageManager);
				builder.cacheLoaderFactory(() -> cacheStore);
				builder.cacheWriterFactory(() -> cacheStore);
			}
			else
			{
				configuration.opt(CACHE_LOADER_FACTORY).ifPresent(value -> {
					builder.cacheLoaderFactory(this.valueAsFactory(value));
					builder.readThrough (configuration.optBoolean(READ_THROUGH ).orElse(true));
				});
				configuration.opt(CACHE_WRITER_FACTORY).ifPresent(value -> {
					builder.cacheWriterFactory(this.valueAsFactory(value));
					builder.writeThrough(configuration.optBoolean(WRITE_THROUGH).orElse(true));
				});
			}
			
			return builder;
		}
		
		private Class<?> getClass(
			final Configuration configuration,
			final String        key
		)
		{
			final String name = configuration.opt(key).orElse(null);
			
			try
			{
				return XChars.isEmpty(name)
					? Object.class
					: this.classResolver.loadClass(name)
				;
			}
			catch(final ClassNotFoundException e)
			{
				throw new CacheException(e);
			}
		}

		private <T> Factory<T> valueAsFactory(
			final String value
		)
		{
			try
			{
				return Factory.class.cast(
					this.classResolver
						.loadClass(value)
						.newInstance()
				);
			}
			catch(ClassNotFoundException | ClassCastException |
				  InstantiationException | IllegalAccessException e
			)
			{
				throw new CacheException(e);
			}
		}
		
	}
	
}
