
package one.microstream.cache;

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Predicate;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

import one.microstream.chars.XChars;
import one.microstream.reflect.XReflect;
import one.microstream.storage.types.EmbeddedStorageManager;


public interface CacheConfiguration<K, V> extends CompleteConfiguration<K, V>
{	
	public static String PathProperty()
	{
		return "microstream.cache.configuration.path";
	}
	
	public static String DefaultResourceName()
	{
		return "microstream-cache.properties";
	}
	
	
	public static CacheConfiguration<?, ?> Load()
	{
		final String path = System.getProperty(PathProperty());
		if(!XChars.isEmpty(path))
		{
			final CacheConfiguration<?, ?> configuration = Load(path);
			if(configuration != null)
			{
				return configuration;
			}
		}

		final String      defaultName        = DefaultResourceName();
		final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
		final URL         url                = contextClassloader != null
			? contextClassloader.getResource(defaultName)
			: Configuration.class.getResource("/" + defaultName);
		if(url != null)
		{
			return Load(url);
		}
		
		File file = new File(defaultName);
		if(file.exists())
		{
			return Load(file);
		}
		file = new File(System.getProperty("user.home"), defaultName);
		if(file.exists())
		{
			return Load(file);
		}
		
		return null;
	}
	
	public static CacheConfiguration<?, ?> Load(
		final String path
	)
	{
		final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
		      URL         url                = contextClassloader != null
			? contextClassloader.getResource(path)
			: Configuration.class.getResource(path);
		if(url != null)
		{
			return Load(url);
		}
			
		try
		{
			url = new URL(path);
			return Load(url);
		}
		catch(MalformedURLException e)
		{
			final File file = new File(path);
			if(file.exists())
			{
				return Load(file);
			}
		}
		
		return null;
	}
	
	public static <K, V> CacheConfiguration<K, V> Load(
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		final String path = System.getProperty(PathProperty());
		if(!XChars.isEmpty(path))
		{
			final CacheConfiguration<K, V> configuration = Load(path, keyType, valueType);
			if(configuration != null)
			{
				return configuration;
			}
		}

		final String      defaultName        = DefaultResourceName();
		final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
		final URL         url                = contextClassloader != null
			? contextClassloader.getResource(defaultName)
			: Configuration.class.getResource("/" + defaultName);
		if(url != null)
		{
			return Load(url, keyType, valueType);
		}
		
		File file = new File(defaultName);
		if(file.exists())
		{
			return Load(file, keyType, valueType);
		}
		file = new File(System.getProperty("user.home"), defaultName);
		if(file.exists())
		{
			return Load(file, keyType, valueType);
		}
		
		return null;
	}
	
	public static <K, V> CacheConfiguration<K, V> Load(
		final String path,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
	          URL         url                = contextClassloader != null
			? contextClassloader.getResource(path)
			: Configuration.class.getResource(path);
		if(url != null)
		{
			return Load(url, keyType, valueType);
		}
			
		try
		{
			url = new URL(path);
			return Load(url, keyType, valueType);
		}
		catch(MalformedURLException e)
		{
			final File file = new File(path);
			if(file.exists())
			{
				return Load(file, keyType, valueType);
			}
		}
		
		return null;
	}
	
	public static CacheConfiguration<?, ?> Load(
		final Path path
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromPath(path)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final Path path, 
		final Charset charset
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromPath(path, charset)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final File file
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromFile(file)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final File file, 
		final Charset charset
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromFile(file, charset)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final URL url
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromUrl(url)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final URL url, 
		final Charset charset
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromUrl(url, charset)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final InputStream inputStream
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.FromInputStream(inputStream).loadConfiguration()
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final InputStream inputStream, 
		final Charset charset
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.FromInputStream(inputStream, charset).loadConfiguration()
		);
	}
	
	public static <K,V> CacheConfiguration<K, V> Load(
		final Path path,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromPath(path),
			keyType,
			valueType
		);
	}
	
	public static <K,V> CacheConfiguration<K, V> Load(
		final Path path, 
		final Charset charset,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromPath(path, charset),
			keyType,
			valueType
		);
	}
	
	public static <K,V> CacheConfiguration<K, V> Load(
		final File file,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromFile(file),
			keyType,
			valueType
		);
	}
	
	public static <K,V> CacheConfiguration<K, V> Load(
		final File file, 
		final Charset charset,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromFile(file, charset),
			keyType,
			valueType
		);
	}
	
	public static <K,V> CacheConfiguration<K, V> Load(
		final URL url,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromUrl(url),
			keyType,
			valueType
		);
	}
	
	public static <K,V> CacheConfiguration<K, V> Load(
		final URL url, 
		final Charset charset,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromUrl(url, charset),
			keyType,
			valueType
		);
	}
	
	public static <K,V> CacheConfiguration<K, V> Load(
		final InputStream inputStream,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.FromInputStream(inputStream).loadConfiguration(),
			keyType,
			valueType
		);
	}
	
	public static <K,V> CacheConfiguration<K, V> Load(
		final InputStream inputStream, 
		final Charset charset,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.FromInputStream(inputStream, charset).loadConfiguration(),
			keyType,
			valueType
		);
	}
		
	
	public Factory<EvictionManager<K, V>> getEvictionManagerFactory();
	
	public Predicate<? super Field> getSerializerFieldPredicate();
	
	
	public static <K, V> Builder<K, V> Builder(
		final Class<K> keyType, 
		final Class<V> valueType
	)
	{
		return new Builder.Default<>(keyType, valueType);
	}
	
	public static <K, V> Builder<K, V> Builder(
		final Class<K> keyType, 
		final Class<V> valueType,
		final String cacheName,
		final EmbeddedStorageManager storageManager
	)
	{
		return Builder(
			keyType,
			valueType,
			CachingProvider.defaultURI(),
			cacheName,
			storageManager
		);
	}
	
	public static <K, V> Builder<K, V> Builder(
		final Class<K> keyType, 
		final Class<V> valueType,
		final URI uri,
		final String cacheName,
		final EmbeddedStorageManager storageManager
	)
	{
		notNull(uri);
		notEmpty(cacheName);
		notNull(storageManager);
		
		final String           cacheKey   = uri.toString() + "::" + cacheName;
		final CacheStore<K, V> cacheStore = CacheStore.New(cacheKey, storageManager);
		
		return Builder(keyType, valueType)
			.cacheLoaderFactory(() -> cacheStore)
			.cacheWriterFactory(() -> cacheStore)
			.readThrough()
			.writeThrough();
	}
	
	public static interface Builder<K, V>
	{
		public Builder<K, V> addListenerConfiguration(CacheEntryListenerConfiguration<K, V> listenerConfigurations);
		
		public Builder<K, V> cacheLoaderFactory(Factory<CacheLoader<K, V>> cacheLoaderFactory);
		
		public Builder<K, V> cacheWriterFactory(Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory);
		
		public Builder<K, V> expiryPolicyFactory(Factory<ExpiryPolicy> expiryPolicyFactory);
		
		public Builder<K, V> evictionManagerFactory(Factory<EvictionManager<K, V>> evictionManagerFactory);
		
		public default Builder<K, V> readThrough()
		{
			return readThrough(true);
		}
		
		public Builder<K, V> readThrough(boolean readThrough);
		
		public default Builder<K, V> writeThrough()
		{
			return writeThrough(true);
		}
		
		public Builder<K, V> writeThrough(boolean writeThrough);
		
		public Builder<K, V> storeByValue();
		
		public Builder<K, V> storeByReference();
		
		public Builder<K, V> enableStatistics();
		
		public Builder<K, V> enableManagement();
		
		public Builder<K, V> serializerFieldPredicate(Predicate<? super Field> serializerFieldPredicate);
		
		public CacheConfiguration<K, V> build();
		
		public static class Default<K, V> implements Builder<K, V>
		{
			private final Class<K>                                 keyType;
			private final Class<V>                                 valueType;
			private HashSet<CacheEntryListenerConfiguration<K, V>> listenerConfigurations;
			private Factory<CacheLoader<K, V>>                     cacheLoaderFactory;
			private Factory<CacheWriter<? super K, ? super V>>     cacheWriterFactory;
			private Factory<ExpiryPolicy>                          expiryPolicyFactory;
			private Factory<EvictionManager<K, V>>                 evictionManagerFactory;
			private boolean                                        readThrough;
			private boolean                                        writeThrough;
			private boolean                                        storeByValue;
			private boolean                                        statisticsEnabled;
			private boolean                                        managementEnabled;
			private Predicate<? super Field>                       serializerFieldPredicate;
			
			Default(final Class<K> keyType, final Class<V> valueType)
			{
				super();
				
				this.keyType   = notNull(keyType);
				this.valueType = notNull(valueType);
			}
			
			@Override
			public Builder<K, V>
				addListenerConfiguration(final CacheEntryListenerConfiguration<K, V> listenerConfiguration)
			{
				this.listenerConfigurations.add(listenerConfiguration);
				return this;
			}
			
			@Override
			public Builder<K, V> cacheLoaderFactory(final Factory<CacheLoader<K, V>> cacheLoaderFactory)
			{
				this.cacheLoaderFactory = cacheLoaderFactory;
				return this;
			}
			
			@Override
			public Builder<K, V> cacheWriterFactory(final Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory)
			{
				this.cacheWriterFactory = cacheWriterFactory;
				return this;
			}
			
			@Override
			public Builder<K, V> expiryPolicyFactory(final Factory<ExpiryPolicy> expiryPolicyFactory)
			{
				this.expiryPolicyFactory = expiryPolicyFactory;
				return this;
			}
			
			@Override
			public Builder<K, V> evictionManagerFactory(final Factory<EvictionManager<K, V>> evictionManagerFactory)
			{
				this.evictionManagerFactory = evictionManagerFactory;
				return this;
			}
			
			@Override
			public Builder<K, V> readThrough(final boolean readThrough)
			{
				this.readThrough = readThrough;
				return this;
			}
			
			@Override
			public Builder<K, V> writeThrough(final boolean writeThrough)
			{
				this.writeThrough = writeThrough;
				return this;
			}
			
			@Override
			public Builder<K, V> storeByValue()
			{
				this.storeByValue = true;
				return this;
			}
			
			@Override
			public Builder<K, V> storeByReference()
			{
				this.storeByValue = false;
				return this;
			}
			
			@Override
			public Builder<K, V> enableStatistics()
			{
				this.statisticsEnabled = true;
				return this;
			}
			
			@Override
			public Builder<K, V> enableManagement()
			{
				this.managementEnabled = true;
				return this;
			}
			
			@Override
			public Builder<K, V> serializerFieldPredicate(final Predicate<? super Field> serializerFieldPredicate)
			{
				this.serializerFieldPredicate = serializerFieldPredicate;
				return this;
			}
			
			@Override
			public CacheConfiguration<K, V> build()
			{
				final Factory<ExpiryPolicy> expiryPolicyFactory = coalesce(
					this.expiryPolicyFactory,
					DefaultExpiryPolicyFactory()
				);
				
				final Factory<EvictionManager<K, V>> evictionManagerFactory = coalesce(
					this.evictionManagerFactory,
					DefaultEvictionManagerFactory()
				);
				
				final Predicate<? super Field> serializerFieldPredicate = coalesce(
					this.serializerFieldPredicate,
					DefaultSerializerFieldPredicate()
				);
				
				return new CacheConfiguration.Default<>(this.keyType,
					this.valueType,
					this.listenerConfigurations,
					this.cacheLoaderFactory,
					this.cacheWriterFactory,
					expiryPolicyFactory,
					evictionManagerFactory,
					this.readThrough,
					this.writeThrough,
					this.storeByValue,
					this.statisticsEnabled,
					this.managementEnabled,
					serializerFieldPredicate
				);
			}
			
		}
		
	}
	
	public static Factory<ExpiryPolicy> DefaultExpiryPolicyFactory()
	{
		return EternalExpiryPolicy.factoryOf();
	}
	
	public static <K, V> Factory<EvictionManager<K, V>> DefaultEvictionManagerFactory()
	{
		return () -> null;
	}
	
	public static Predicate<? super Field> DefaultSerializerFieldPredicate()
	{
		return XReflect::isNotTransient;
	}
	
	public static <K, V> CacheConfiguration<K, V> New(final Configuration<K, V> other)
	{
		final HashSet<CacheEntryListenerConfiguration<K, V>> listenerConfigurations = new HashSet<>();
		
		if(other instanceof CompleteConfiguration)
		{
			final CompleteConfiguration<K, V> complete = (CompleteConfiguration<K, V>)other;
			
			for(final CacheEntryListenerConfiguration<K, V> listenerConfig : complete
				.getCacheEntryListenerConfigurations())
			{
				listenerConfigurations.add(listenerConfig);
			}
			
			final Factory<ExpiryPolicy> expiryPolicyFactory = coalesce(
				complete.getExpiryPolicyFactory(),
				DefaultExpiryPolicyFactory()
			);
			
			final Factory<EvictionManager<K, V>> evictionManagerFactory;
			final Predicate<? super Field>       serializerFieldPredicate;
			if(other instanceof CacheConfiguration)
			{
				final CacheConfiguration<K, V> msCacheConfig = (CacheConfiguration<K, V>)other;
				evictionManagerFactory   = msCacheConfig.getEvictionManagerFactory();
				serializerFieldPredicate = msCacheConfig.getSerializerFieldPredicate();
			}
			else
			{
				evictionManagerFactory   = DefaultEvictionManagerFactory();
				serializerFieldPredicate = DefaultSerializerFieldPredicate();
			}
			
			return new Default<>(
				complete.getKeyType(),
				complete.getValueType(),
				listenerConfigurations,
				complete.getCacheLoaderFactory(),
				complete.getCacheWriterFactory(),
				expiryPolicyFactory,
				evictionManagerFactory,
				complete.isReadThrough(),
				complete.isWriteThrough(),
				complete.isStoreByValue(),
				complete.isStatisticsEnabled(),
				complete.isManagementEnabled(),
				serializerFieldPredicate
			);
		}
		
		return new Default<>(
			other.getKeyType(),
			other.getValueType(),
			listenerConfigurations,
			null,
			null,
			DefaultExpiryPolicyFactory(),
			DefaultEvictionManagerFactory(),
			false,
			false,
			other.isStoreByValue(),
			false,
			false,
			DefaultSerializerFieldPredicate());
	}
	
	public static class Default<K, V> extends MutableConfiguration<K, V> implements CacheConfiguration<K, V>
	{
		private final Factory<EvictionManager<K, V>>                 evictionManagerFactory;
		private final Predicate<? super Field>                       serializerFieldPredicate;
		
		Default(
			final Class<K>                                       keyType,
			final Class<V>                                       valueType,
			final HashSet<CacheEntryListenerConfiguration<K, V>> listenerConfigurations,
			final Factory<CacheLoader<K, V>>                     cacheLoaderFactory,
			final Factory<CacheWriter<? super K, ? super V>>     cacheWriterFactory,
			final Factory<ExpiryPolicy>                          expiryPolicyFactory,
			final Factory<EvictionManager<K, V>>                 evictionManagerFactory,
			final boolean                                        isReadThrough,
			final boolean                                        isWriteThrough,
			final boolean                                        isStoreByValue,
			final boolean                                        isStatisticsEnabled,
			final boolean                                        isManagementEnabled,
			final Predicate<? super Field>                       serializerFieldPredicate
		)
		{
			super();
			
			this.keyType                  = keyType;
			this.valueType                = valueType;
			if(listenerConfigurations != null)
			{
				this.listenerConfigurations.addAll(listenerConfigurations);
			}
			this.cacheLoaderFactory       = cacheLoaderFactory;
			this.cacheWriterFactory       = cacheWriterFactory;
			this.expiryPolicyFactory      = expiryPolicyFactory;
			this.evictionManagerFactory   = evictionManagerFactory;
			this.isReadThrough            = isReadThrough;
			this.isWriteThrough           = isWriteThrough;
			this.isStatisticsEnabled      = isStatisticsEnabled;
			this.isStoreByValue           = isStoreByValue;
			this.isManagementEnabled      = isManagementEnabled;
			this.serializerFieldPredicate = serializerFieldPredicate;
		}
		
		@Override
		public Class<K> getKeyType()
		{
			return this.keyType;
		}
		
		@Override
		public Class<V> getValueType()
		{
			return this.valueType;
		}
		
		@Override
		public Iterable<CacheEntryListenerConfiguration<K, V>> getCacheEntryListenerConfigurations()
		{
			return this.listenerConfigurations != null
				? this.listenerConfigurations
				: Collections.emptyList();
		}
		
		@Override
		public Factory<EvictionManager<K, V>> getEvictionManagerFactory()
		{
			return this.evictionManagerFactory;
		}
		
		@Override
		public Predicate<? super Field> getSerializerFieldPredicate()
		{
			return this.serializerFieldPredicate;
		}
		
		@Override
		public int hashCode()
		{
			final int prime  = 31;
			int       result = 1;
			result = prime * result + (this.cacheLoaderFactory == null ? 0 : this.cacheLoaderFactory.hashCode());
			result = prime * result + (this.cacheWriterFactory == null ? 0 : this.cacheWriterFactory.hashCode());
			result = prime * result + (this.expiryPolicyFactory == null ? 0 : this.expiryPolicyFactory.hashCode());
			result = prime * result + (this.isManagementEnabled ? 1231 : 1237);
			result = prime * result + (this.isReadThrough ? 1231 : 1237);
			result = prime * result + (this.isStatisticsEnabled ? 1231 : 1237);
			result = prime * result + (this.isStoreByValue ? 1231 : 1237);
			result = prime * result + (this.isWriteThrough ? 1231 : 1237);
			result = prime * result + (this.keyType == null ? 0 : this.keyType.hashCode());
			result = prime * result + (this.listenerConfigurations == null ? 0 : this.listenerConfigurations.hashCode());
			result = prime * result + (this.valueType == null ? 0 : this.valueType.hashCode());
			result = prime * result + (this.evictionManagerFactory == null ? 0 : this.evictionManagerFactory.hashCode());
			result = prime * result + (this.serializerFieldPredicate == null ? 0 : this.serializerFieldPredicate.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			if(this == obj)
			{
				return true;
			}
			if(!(obj instanceof CacheConfiguration))
			{
				return false;
			}
			final CacheConfiguration<?, ?> other = (CacheConfiguration<?, ?>)obj;
			if(this.cacheLoaderFactory == null)
			{
				if(other.getCacheLoaderFactory() != null)
				{
					return false;
				}
			}
			else if(!this.cacheLoaderFactory.equals(other.getCacheLoaderFactory()))
			{
				return false;
			}
			if(this.cacheWriterFactory == null)
			{
				if(other.getCacheWriterFactory() != null)
				{
					return false;
				}
			}
			else if(!this.cacheWriterFactory.equals(other.getCacheWriterFactory()))
			{
				return false;
			}
			if(this.expiryPolicyFactory == null)
			{
				if(other.getExpiryPolicyFactory() != null)
				{
					return false;
				}
			}
			else if(!this.expiryPolicyFactory.equals(other.getExpiryPolicyFactory()))
			{
				return false;
			}
			if(this.isManagementEnabled != other.isManagementEnabled())
			{
				return false;
			}
			if(this.isReadThrough != other.isReadThrough())
			{
				return false;
			}
			if(this.isStatisticsEnabled != other.isStatisticsEnabled())
			{
				return false;
			}
			if(this.isStoreByValue != other.isStoreByValue())
			{
				return false;
			}
			if(this.isWriteThrough != other.isWriteThrough())
			{
				return false;
			}
			if(this.keyType == null)
			{
				if(other.getKeyType() != null)
				{
					return false;
				}
			}
			else if(!this.keyType.equals(other.getKeyType()))
			{
				return false;
			}
			if(this.listenerConfigurations == null)
			{
				if(other.getCacheEntryListenerConfigurations() != null)
				{
					return false;
				}
			}
			else if(!this.listenerConfigurations.equals(other.getCacheEntryListenerConfigurations()))
			{
				return false;
			}
			if(this.valueType == null)
			{
				if(other.getValueType() != null)
				{
					return false;
				}
			}
			else if(!this.valueType.equals(other.getValueType()))
			{
				return false;
			}
			if(this.evictionManagerFactory == null)
			{
				if(other.getEvictionManagerFactory() != null)
				{
					return false;
				}
			}
			else if(!this.evictionManagerFactory.equals(other.getEvictionManagerFactory()))
			{
				return false;
			}
			if(this.serializerFieldPredicate == null)
			{
				if(other.getSerializerFieldPredicate() != null)
				{
					return false;
				}
			}
			else if(!this.serializerFieldPredicate.equals(other.getSerializerFieldPredicate()))
			{
				return false;
			}
			return true;
		}
		
	}
	
}
