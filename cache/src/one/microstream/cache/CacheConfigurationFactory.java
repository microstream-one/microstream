
package one.microstream.cache;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.net.URI;

import javax.cache.configuration.MutableConfiguration;

import one.microstream.storage.types.EmbeddedStorageManager;


public final class CacheConfigurationFactory
{
	public static <K, V> MutableConfiguration<K, V> createCacheConfiguration(
		final String cacheName,
		final EmbeddedStorageManager storageManager
	)
	{
		return createCacheConfiguration(
			CachingProvider.defaultURI(),
			cacheName,
			storageManager
		);
	}
	
	public static <K, V> MutableConfiguration<K, V> createCacheConfiguration(
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
	
	private CacheConfigurationFactory()
	{
		throw new Error();
	}
}
