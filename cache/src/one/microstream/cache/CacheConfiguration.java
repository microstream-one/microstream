
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

import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryListener;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

import one.microstream.chars.XChars;
import one.microstream.reflect.XReflect;
import one.microstream.storage.types.EmbeddedStorageManager;

/**
 * Extended {@link CompleteConfiguration} used by MicroStream's {@link Cache}.
 * <p>
 * Added features:<br>
 * - {@link #getEvictionManagerFactory()}<br>
 * - {@link #getSerializerFieldPredicate()}
 * </p>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface CacheConfiguration<K, V> extends CompleteConfiguration<K, V>
{
	/**
	 * The property name which is used to hand the external configuration file path to the application.
	 * <p>
	 * Either as system property or in the context's configuration, e.g. Spring's application.properties.
	 *
	 * @return "microstream.cache.configuration.path"
	 */
	public static String PathProperty()
	{
		return "microstream.cache.configuration.path";
	}

	/**
	 * The default name of the cache configuration resource.
	 *
	 * @see #Load()
	 *
	 * @return "microstream-cache.properties"
	 */
	public static String DefaultResourceName()
	{
		return "microstream-cache.properties";
	}

	/**
	 * Tries to load the default configuration file.
	 * <p>
	 * The search order is as follows:
	 * <ul>
	 * <li>The path set in the system property "microstream.cache.configuration.path"</li>
	 * <li>The file named "microstream-cache.properties" in
	 * <ul>
	 * <li>The classpath</li>
	 * <li>The application's directory</li>
	 * <li>The user home directory</li>
	 * </ul></li>
	 * </ul>
	 *
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 *
	 * @return the loaded configuration or <code>null</code> if none was found
	 */
	public static CacheConfiguration<?, ?> Load()
	{
		return Load(CacheConfigurationLoader.Defaults.defaultCharset());
	}

	/**
	 * Tries to load the default configuration file.
	 * <p>
	 * The search order is as follows:
	 * <ul>
	 * <li>The path set in the system property "microstream.cache.configuration.path"</li>
	 * <li>The file named "microstream-cache.properties" in
	 * <ul>
	 * <li>The classpath</li>
	 * <li>The application's directory</li>
	 * <li>The user home directory</li>
	 * </ul></li>
	 * </ul>
	 *
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 *
	 * @param charset the charset used to load the configuration
	 * @return the loaded configuration or <code>null</code> if none was found
	 */
	public static CacheConfiguration<?, ?> Load(
		final Charset charset
	)
	{
		final String path = System.getProperty(PathProperty());
		if(!XChars.isEmpty(path))
		{
			final CacheConfiguration<?, ?> configuration = Load(path, charset);
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
			return Load(url, charset);
		}

		File file = new File(defaultName);
		if(file.exists())
		{
			return Load(file, charset);
		}
		file = new File(System.getProperty("user.home"), defaultName);
		if(file.exists())
		{
			return Load(file, charset);
		}

		return null;
	}

	/**
	 * Tries to load the default configuration file.
	 * <p>
	 * The search order is as follows:
	 * <ul>
	 * <li>The path set in the system property "microstream.cache.configuration.path"</li>
	 * <li>The file named "microstream-cache.properties" in
	 * <ul>
	 * <li>The classpath</li>
	 * <li>The application's directory</li>
	 * <li>The user home directory</li>
	 * </ul></li>
	 * </ul>
	 *
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 *
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the loaded configuration or <code>null</code> if none was found
	 */
	public static <K, V> CacheConfiguration<K, V> Load(
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return Load(
			CacheConfigurationLoader.Defaults.defaultCharset(),
			keyType,
			valueType
		);
	}

	/**
	 * Tries to load the default configuration file.
	 * <p>
	 * The search order is as follows:
	 * <ul>
	 * <li>The path set in the system property "microstream.cache.configuration.path"</li>
	 * <li>The file named "microstream-cache.properties" in
	 * <ul>
	 * <li>The classpath</li>
	 * <li>The application's directory</li>
	 * <li>The user home directory</li>
	 * </ul></li>
	 * </ul>
	 *
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 *
	 * @param charset the charset used to load the configuration
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the loaded configuration or <code>null</code> if none was found
	 */
	public static <K, V> CacheConfiguration<K, V> Load(
		final Charset charset,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		final String path = System.getProperty(PathProperty());
		if(!XChars.isEmpty(path))
		{
			final CacheConfiguration<K, V> configuration = Load(path, charset, keyType, valueType);
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
			return Load(url, charset, keyType, valueType);
		}

		File file = new File(defaultName);
		if(file.exists())
		{
			return Load(file, charset, keyType, valueType);
		}
		file = new File(System.getProperty("user.home"), defaultName);
		if(file.exists())
		{
			return Load(file, charset, keyType, valueType);
		}

		return null;
	}

	/**
	 * Tries to load the configuration file from <code>path</code>.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @return the configuration or <code>null</code> if none was found
	 */
	public static CacheConfiguration<?, ?> Load(
		final String path
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.load(path)
		);
	}

	/**
	 * Tries to load the configuration file from <code>path</code>.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @param charset the charset used to load the configuration
	 * @return the configuration or <code>null</code> if none was found
	 */
	public static CacheConfiguration<?, ?> Load(
		final String path,
		final Charset charset
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.load(path, charset)
		);
	}

	/**
	 * Tries to load the configuration file from <code>path</code>.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration or <code>null</code> if none was found
	 */
	public static <K, V> CacheConfiguration<K, V> Load(
		final String path,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return Load(
			path,
			CacheConfigurationLoader.Defaults.defaultCharset(),
			keyType,
			valueType
		);
	}

	/**
	 * Tries to load the configuration file from <code>path</code>.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @param charset the charset used to load the configuration
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration or <code>null</code> if none was found
	 */
	public static <K, V> CacheConfiguration<K, V> Load(
		final String path,
		final Charset charset,
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
			return Load(url, charset, keyType, valueType);
		}

		try
		{
			url = new URL(path);
			return Load(url, charset, keyType, valueType);
		}
		catch(final MalformedURLException e)
		{
			final File file = new File(path);
			if(file.exists())
			{
				return Load(file, charset, keyType, valueType);
			}
		}

		return null;
	}

	/**
	 * Tries to load the configuration from <code>path</code>.
	 *
	 * @param path file system path
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
	public static CacheConfiguration<?, ?> Load(
		final Path path
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromPath(path)
		);
	}

	/**
	 * Tries to load the configuration from <code>path</code>.
	 *
	 * @param path file system path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
	public static CacheConfiguration<?, ?> Load(
		final Path path,
		final Charset charset
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromPath(path, charset)
		);
	}

	/**
	 * Tries to load the configuration from the file <code>file</code>.
	 *
	 * @param file file path
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
	public static CacheConfiguration<?, ?> Load(
		final File file
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromFile(file)
		);
	}

	/**
	 * Tries to load the configuration from the file <code>file</code>.
	 *
	 * @param file file path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
	public static CacheConfiguration<?, ?> Load(
		final File file,
		final Charset charset
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromFile(file, charset)
		);
	}

	/**
	 * Tries to load the configuration from the URL <code>url</code>.
	 *
	 * @param url URL path
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
	public static CacheConfiguration<?, ?> Load(
		final URL url
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromUrl(url)
		);
	}

	/**
	 * Tries to load the configuration from the URL <code>url</code>.
	 *
	 * @param url URL path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
	public static CacheConfiguration<?, ?> Load(
		final URL url,
		final Charset charset
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.loadFromUrl(url, charset)
		);
	}

	/**
	 * Tries to load the configuration from the {@link InputStream} <code>inputStream</code>.
	 *
	 * @param inputStream the stream to read from
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
	public static CacheConfiguration<?, ?> Load(
		final InputStream inputStream
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.FromInputStream(inputStream).loadConfiguration()
		);
	}

	/**
	 * Tries to load the configuration from the {@link InputStream} <code>inputStream</code>.
	 *
	 * @param inputStream the stream to read from
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
	public static CacheConfiguration<?, ?> Load(
		final InputStream inputStream,
		final Charset charset
	)
	{
		return CacheConfigurationParser.New().parse(
			CacheConfigurationLoader.FromInputStream(inputStream, charset).loadConfiguration()
		);
	}

	/**
	 * Tries to load the configuration from <code>path</code>.
	 *
	 * @param path file system path
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
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

	/**
	 * Tries to load the configuration from <code>path</code>.
	 *
	 * @param path file system path
	 * @param charset the charset used to load the configuration
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
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

	/**
	 * Tries to load the configuration from the file <code>file</code>.
	 *
	 * @param file file path
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
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

	/**
	 * Tries to load the configuration from the file <code>file</code>.
	 *
	 * @param file file path
	 * @param charset the charset used to load the configuration
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
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

	/**
	 * Tries to load the configuration from the URL <code>url</code>.
	 *
	 * @param url URL path
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
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

	/**
	 * Tries to load the configuration from the URL <code>url</code>.
	 *
	 * @param url URL path
	 * @param charset the charset used to load the configuration
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
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

	/**
	 * Tries to load the configuration from the {@link InputStream} <code>inputStream</code>.
	 *
	 * @param inputStream the stream to read from
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
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

	/**
	 * Tries to load the configuration from the {@link InputStream} <code>inputStream</code>.
	 *
	 * @param inputStream the stream to read from
	 * @param charset the charset used to load the configuration
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the configuration
	 * @throws CacheConfigurationException if the configuration couldn't be loaded
	 */
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


	/**
	 * Gets the {@link javax.cache.configuration.Factory} for the
     * {@link EvictionManager}, if any.
     *
	 * @return the {@link javax.cache.configuration.Factory} for the
     * {@link EvictionManager} or null if none has been set.
	 */
	public Factory<EvictionManager<K, V>> getEvictionManagerFactory();

	/**
	 * Gets the Serializer {@link Field} {@link Predicate}, if any.
	 *
	 * @return the {@link Field} {@link Predicate} for the Serializer, or null if none has been set
	 */
	public Predicate<? super Field> getSerializerFieldPredicate();

	/**
	 * Creates a new {@link Builder} for a {@link CacheConfiguration}.
	 *
	 * @param keyType the key type
	 * @param valueType the value type
	 */
	public static <K, V> Builder<K, V> Builder(
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return new Builder.Default<>(keyType, valueType);
	}

	/**
	 * Creates a new {@link Builder} for a {@link CacheConfiguration}, which uses
	 * the <code>storageManager</code> as a backing store.
	 *
	 * @param keyType the key type
	 * @param valueType the value type
	 * @param cacheName the slot name for the data in the {@link EmbeddedStorageManager}'s root,
	 * 	usually the {@link Cache}'s name
	 * @param storageManager the {@link EmbeddedStorageManager} to use as a backing store
	 */
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

	/**
	 * Creates a new {@link Builder} for a {@link CacheConfiguration}, which uses
	 * the <code>storageManager</code> as a backing store.
	 *
	 * @param keyType the key type
	 * @param valueType the value type
	 * @param uri prefix of the slot name for the data in the {@link EmbeddedStorageManager}'s root
	 * @param cacheName suffix of slot name for the data in the {@link EmbeddedStorageManager}'s root
	 * @param storageManager the {@link EmbeddedStorageManager} to use as a backing store
	 */
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
		/**
		 * Add a configuration for a {@link CacheEntryListener}.
		 *
		 * @param listenerConfiguration the
		 *  {@link CacheEntryListenerConfiguration}
		 */
		public Builder<K, V> addListenerConfiguration(CacheEntryListenerConfiguration<K, V> listenerConfiguration);

		/**
	     * Set the {@link CacheLoader} {@link Factory}.
	     *
	     * @param cacheLoaderFactory the {@link CacheLoader} {@link Factory}
	     */
		public Builder<K, V> cacheLoaderFactory(Factory<CacheLoader<K, V>> cacheLoaderFactory);

		/**
	     * Set the {@link CacheWriter} {@link Factory}.
	     *
	     * @param cacheWriterFactory the {@link CacheWriter} {@link Factory}
	     */
		public Builder<K, V> cacheWriterFactory(Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory);

		/**
		 * Set the {@link Factory} for the {@link ExpiryPolicy}. If <code>null</code>
		 * is specified the default {@link ExpiryPolicy} is used.
		 * <p>
		 * Only one expiry policy can be set for a cache. The last policy applied
		 * before cache construction will be the one used.
		 *
		 * @param expiryPolicyFactory
		 *            the {@link ExpiryPolicy} {@link Factory}
		 *
		 * @see CacheConfiguration#DefaultExpiryPolicyFactory()
		 */
		public Builder<K, V> expiryPolicyFactory(Factory<ExpiryPolicy> expiryPolicyFactory);

		/**
	     * Set the {@link EvictionManager} {@link Factory}. If <code>null</code>
		 * is specified the default {@link EvictionManager} is used.
		 * <p>
		 * Only one eviction manager can be set for a cache. The last manager applied
		 * before cache construction will be the one used.
	     *
	     * @param evictionManagerFactory the {@link EvictionManager} {@link Factory}
	     *
	     * @see CacheConfiguration#DefaultEvictionManagerFactory()
	     */
		public Builder<K, V> evictionManagerFactory(Factory<EvictionManager<K, V>> evictionManagerFactory);

		/**
		 * Set that read-through caching should be used.
		 * <p>
		 * It is an invalid configuration to use this without specifying a
		 * {@link CacheLoader} {@link Factory}.
		 */
		public default Builder<K, V> readThrough()
		{
			return this.readThrough(true);
		}

		/**
		 * Set if read-through caching should be used.
		 * <p>
		 * It is an invalid configuration to set this to true without specifying a
		 * {@link CacheLoader} {@link Factory}.
		 *
		 * @param readThrough
		 *            <code>true</code> if read-through is required
		 */
		public Builder<K, V> readThrough(boolean readThrough);

		/**
		 * Set that write-through caching should be used.
		 * <p>
		 * It is an invalid configuration to use this without specifying a
		 * {@link CacheWriter} {@link Factory}.
		 */
		public default Builder<K, V> writeThrough()
		{
			return this.writeThrough(true);
		}

		/**
		 * Set if write-through caching should be used.
		 * <p>
		 * It is an invalid configuration to set this to true without specifying a
		 * {@link CacheWriter} {@link Factory}.
		 *
		 * @param writeThrough
		 *            <code>true</code> if write-through is required
		 */
		public Builder<K, V> writeThrough(boolean writeThrough);

		/**
		 * Set that store-by-value semantics should be used.
		 */
		public default Builder<K, V> storeByValue()
		{
			return this.storeByValue(true);
		}

		/**
		 * Set that store-by-reference semantics should be used.
		 */
		public default Builder<K, V> storeByReference()
		{
			return this.storeByValue(false);
		}

		/**
		 * Set if a configured cache should use store-by-value or store-by-reference
		 * semantics.
		 *
		 * @param storeByValue
		 *            <code>true</code> if store-by-value is required,
		 *            <code>false</code> for store-by-reference
		 */
		public Builder<K, V> storeByValue(boolean storeByValue);

		/**
		 * Enables statistics gathering.
		 * <p>
		 * Statistics may be enabled or disabled at runtime via
		 * {@link CacheManager#enableStatistics(String, boolean)} or
		 * {@link Cache#setStatisticsEnabled(boolean)}.
		 */
		public default Builder<K, V> enableStatistics()
		{
			return this.enableStatistics(true);
		}

		/**
		 * Disables statistics gathering.
		 * <p>
		 * Statistics may be enabled or disabled at runtime via
		 * {@link CacheManager#enableStatistics(String, boolean)} or
		 * {@link Cache#setStatisticsEnabled(boolean)}.
		 */
		public default Builder<K, V> disableStatistics()
		{
			return this.enableStatistics(false);
		}

		/**
		 * Sets whether statistics gathering is enabled.
		 * <p>
		 * Statistics may be enabled or disabled at runtime via
		 * {@link CacheManager#enableStatistics(String, boolean)} or
		 * {@link Cache#setStatisticsEnabled(boolean)}.
		 *
		 * @param statisticsEnabled
		 *            true to enable statistics, false to disable.
		 */
		public Builder<K, V> enableStatistics(final boolean statisticsEnabled);

		/**
		 * Enables the management bean.
		 * <p>
		 * Management may be enabled or disabled at runtime via
		 * {@link CacheManager#enableManagement(String, boolean)} or
		 * {@link Cache#setManagementEnabled(boolean)}.
		 */
		public default Builder<K, V> enableManagement()
		{
			return this.enableManagement(true);
		}

		/**
		 * Disables the management bean.
		 * <p>
		 * Management may be enabled or disabled at runtime via
		 * {@link CacheManager#enableManagement(String, boolean)} or
		 * {@link Cache#setManagementEnabled(boolean)}.
		 */
		public default Builder<K, V> disableManagement()
		{
			return this.enableManagement(false);
		}

		/**
		 * Sets whether the management bean is enabled.
		 * <p>
		 * Management may be enabled or disabled at runtime via
		 * {@link CacheManager#enableManagement(String, boolean)} or
		 * {@link Cache#setManagementEnabled(boolean)}.
		 *
		 * @param managementEnabled
		 *            true to enable statistics, false to disable.
		 */
		public Builder<K, V> enableManagement(final boolean managementEnabled);

		/**
	     * Set the Serializer {@link Field} {@link Predicate}. If <code>null</code>
		 * is specified the default {@link Predicate} is used.
		 * <p>
		 * Only one predicate can be set for a cache. The last one applied
		 * before cache construction will be the one used.
	     *
	     * @param serializerFieldPredicate the Serializer {@link Field} {@link Predicate}
	     *
	     * @see CacheConfiguration#DefaultSerializerFieldPredicate()
	     */
		public Builder<K, V> serializerFieldPredicate(Predicate<? super Field> serializerFieldPredicate);

		/**
		 * Builds a {@link CacheConfiguration} based on the values of this {@link Builder}.
		 */
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
			public Builder<K, V> storeByValue(final boolean storeByValue)
			{
				this.storeByValue = storeByValue;
				return this;
			}

			@Override
			public Builder<K, V> enableStatistics(final boolean statisticsEnabled)
			{
				this.statisticsEnabled = statisticsEnabled;
				return this;
			}

			@Override
			public Builder<K, V> enableManagement(final boolean managementEnabled)
			{
				this.managementEnabled = managementEnabled;
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

	/**
	 * Returns the default {@link ExpiryPolicy} {@link Factory}, which is eternal.
	 */
	public static Factory<ExpiryPolicy> DefaultExpiryPolicyFactory()
	{
		return EternalExpiryPolicy.factoryOf();
	}

	/**
	 * Returns the default {@link EvictionManager} {@link Factory}, which doesn't evict at all.
	 */
	public static <K, V> Factory<EvictionManager<K, V>> DefaultEvictionManagerFactory()
	{
		return () -> null;
	}

	/**
	 * Returns the default Serializer {@link Field} {@link Predicate}, which excludes transient fields.
	 */
	public static Predicate<? super Field> DefaultSerializerFieldPredicate()
	{
		return XReflect::isNotTransient;
	}

	/**
	 * Creates a new {@link CacheConfiguration} based on a {@link Configuration}.
	 */
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
