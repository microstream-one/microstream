
package one.microstream.cache;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.cache.configuration.MutableConfiguration;

import one.microstream.storage.types.EmbeddedStorageManager;


public final class CacheConfigurationFactory
{
	public static <K, V> MutableConfiguration<K, V> Create(
		final String cacheName,
		final EmbeddedStorageManager storageManager
	)
	{
		return Create(
			CachingProvider.defaultURI(),
			cacheName,
			storageManager
		);
	}
	
	public static <K, V> MutableConfiguration<K, V> Create(
		final URI uri,
		final String cacheName,
		final EmbeddedStorageManager storageManager
	)
	{
		notNull(uri);
		notEmpty(cacheName);
		notNull(storageManager);
		
		final String                     cacheKey   = uri.toString() + "::" + cacheName;
		final CacheStore<K, V>           cacheStore = CacheStore.New(cacheKey, storageManager);
		
		return new MutableConfiguration<K, V>()
			.setCacheLoaderFactory(() -> cacheStore)
			.setCacheWriterFactory(() -> cacheStore)
			.setReadThrough(true)
			.setWriteThrough(true);
	}
	
	public static String DefaultClasspathFile()
	{
		return "/microstream-cache.properties";
	}
	
	private static URL DefaultUrl()
	{
		final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
		return contextClassloader != null
			? contextClassloader.getResource(DefaultClasspathFile())
			: CacheConfigurationFactory.class.getResource(DefaultClasspathFile())
		;
	}
	
	public static CacheConfiguration<?, ?> Load()
	{
		final URL url = DefaultUrl();		
		return url != null
			? Load(url)
			: null;
	}
	
	public static <K, V> CacheConfiguration<K, V> Load(
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		final URL url = DefaultUrl();		
		return url != null
			? Load(url, keyType, valueType)
			: null;
	}
	
	public static CacheConfiguration<?, ?> Load(
		final Path path
	)
	{
		return CacheConfigurationParser.Default().parse(
			CacheConfigurationLoader.loadFromPath(path)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final Path path, 
		final Charset charset
	)
	{
		return CacheConfigurationParser.Default().parse(
			CacheConfigurationLoader.loadFromPath(path, charset)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final File file
	)
	{
		return CacheConfigurationParser.Default().parse(
			CacheConfigurationLoader.loadFromFile(file)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final File file, 
		final Charset charset
	)
	{
		return CacheConfigurationParser.Default().parse(
			CacheConfigurationLoader.loadFromFile(file, charset)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final URL url
	)
	{
		return CacheConfigurationParser.Default().parse(
			CacheConfigurationLoader.loadFromUrl(url)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final URL url, 
		final Charset charset
	)
	{
		return CacheConfigurationParser.Default().parse(
			CacheConfigurationLoader.loadFromUrl(url, charset)
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final InputStream inputStream
	)
	{
		return CacheConfigurationParser.Default().parse(
			CacheConfigurationLoader.FromInputStream(inputStream).loadConfiguration()
		);
	}
	
	public static CacheConfiguration<?, ?> Load(
		final InputStream inputStream, 
		final Charset charset
	)
	{
		return CacheConfigurationParser.Default().parse(
			CacheConfigurationLoader.FromInputStream(inputStream, charset).loadConfiguration()
		);
	}
	
	public static <K,V> CacheConfiguration<K, V> Load(
		final Path path,
		final Class<K> keyType,
		final Class<V> valueType
	)
	{
		return CacheConfigurationParser.Default().parse(
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
		return CacheConfigurationParser.Default().parse(
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
		return CacheConfigurationParser.Default().parse(
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
		return CacheConfigurationParser.Default().parse(
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
		return CacheConfigurationParser.Default().parse(
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
		return CacheConfigurationParser.Default().parse(
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
		return CacheConfigurationParser.Default().parse(
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
		return CacheConfigurationParser.Default().parse(
			CacheConfigurationLoader.FromInputStream(inputStream, charset).loadConfiguration(),
			keyType,
			valueType
		);
	}
	
	
	private CacheConfigurationFactory()
	{
		throw new Error();
	}
}
