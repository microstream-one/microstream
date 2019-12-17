
package one.microstream.cache;

import java.nio.file.Paths;
import java.util.Random;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestCache
{
	static class Entity
	{
		String string;
		double number;
		
		Entity(final String string, final double number)
		{
			super();
			this.string = string;
			this.number = number;
		}
	}
	
	public static void main(final String[] args)
	{
		final EmbeddedStorageManager               storageManager =
			EmbeddedStorage.start(Paths.get(System.getProperty("user.home"), "cache-storage"));
		final MutableConfiguration<String, Entity> configuration  =
			CacheConfigurationFactory.createCacheConfiguration("test", storageManager);
		configuration.setStatisticsEnabled(true);
		configuration.setStoreByValue(false);
		
		final Cache<String, Entity> cache  =
			Caching.getCachingProvider().getCacheManager().createCache("test", configuration);
		
		final Random                random = new Random();
		for(int i = 0; i < 1_000; i++)
		{
			cache.put("e" + i, new Entity("entity" + i, random.nextDouble()));
		}
		
		System.out.println(cache.get("e500"));
				
		cache.close();
		storageManager.shutdown();
	}
}
