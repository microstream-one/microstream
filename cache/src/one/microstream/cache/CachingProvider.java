
package one.microstream.cache;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.cache.configuration.OptionalFeature;


public class CachingProvider implements javax.cache.spi.CachingProvider
{
	public static URI defaultURI()
	{
		try
		{
			return new URI("microstream");
		}
		catch(final URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private final WeakHashMap<ClassLoader, HashMap<URI, CacheManager>> cacheManagers = new WeakHashMap<>();
	
	public CachingProvider()
	{
		super();
	}
	
	@Override
	public ClassLoader getDefaultClassLoader()
	{
		return this.getClass().getClassLoader();
	}
	
	@Override
	public URI getDefaultURI()
	{
		return defaultURI();
	}
	
	@Override
	public Properties getDefaultProperties()
	{
		return null;
	}
	
	@Override
	public CacheManager getCacheManager()
	{
		return this.getCacheManager(this.getDefaultURI(), this.getDefaultClassLoader());
	}
	
	@Override
	public CacheManager getCacheManager(final URI uri, final ClassLoader classLoader)
	{
		return this.getCacheManager(uri, classLoader, this.getDefaultProperties());
	}
	
	@Override
	public synchronized CacheManager
		getCacheManager(final URI uri, final ClassLoader classLoader, final Properties properties)
	{
		final URI         managerURI         = uri == null
			? this.getDefaultURI()
			: uri;
		
		final ClassLoader managerClassLoader = classLoader == null
			? this.getDefaultClassLoader()
			: classLoader;
		
		final Properties  managerProperties  = properties == null
			? new Properties()
			: properties;
		
		return this.cacheManagers.computeIfAbsent(managerClassLoader, cl -> new HashMap<>())
			.computeIfAbsent(managerURI,
				key -> CacheManager.New(this, managerURI, managerClassLoader, managerProperties));
	}
	
	@Override
	public synchronized void close()
	{
		this.cacheManagers.values().stream()
			.flatMap(kv -> kv.values().stream())
			.forEach(CacheManager::close);
		
		this.cacheManagers.clear();
	}
	
	@Override
	public synchronized void close(final ClassLoader classLoader)
	{
		final ClassLoader                managerClassLoader = classLoader == null
			? this.getDefaultClassLoader()
			: classLoader;
		
		final HashMap<URI, CacheManager> cacheManagersByURI = this.cacheManagers.remove(managerClassLoader);
		if(cacheManagersByURI != null)
		{
			cacheManagersByURI.values().forEach(CacheManager::close);
		}
	}
	
	@Override
	public synchronized void close(final URI uri, final ClassLoader classLoader)
	{
		final URI                        managerURI         = uri == null
			? this.getDefaultURI()
			: uri;
		
		final ClassLoader                managerClassLoader = classLoader == null
			? this.getDefaultClassLoader()
			: classLoader;
		
		final HashMap<URI, CacheManager> cacheManagersByURI = this.cacheManagers.get(managerClassLoader);
		if(cacheManagersByURI != null)
		{
			final CacheManager cacheManager = cacheManagersByURI.remove(managerURI);
			if(cacheManager != null)
			{
				cacheManager.close();
				
				if(cacheManagersByURI.size() == 0)
				{
					this.cacheManagers.remove(managerClassLoader);
				}
			}
		}
	}
	
	synchronized void remove(final URI uri, final ClassLoader classLoader)
	{
		final HashMap<URI, CacheManager> cacheManagersByURI = this.cacheManagers.get(classLoader);
		if(cacheManagersByURI != null)
		{
			final CacheManager cacheManager = cacheManagersByURI.remove(uri);
			if(cacheManager != null && cacheManagersByURI.size() == 0)
			{
				this.cacheManagers.remove(classLoader);
			}
		}
	}
	
	@Override
	public boolean isSupported(final OptionalFeature optionalFeature)
	{
		switch(optionalFeature)
		{
			case STORE_BY_REFERENCE:
				return true;
		}
		
		return false;
	}
	
}
