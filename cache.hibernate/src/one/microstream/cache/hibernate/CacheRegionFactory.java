package one.microstream.cache.hibernate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.ehcache.ConfigSettings;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.SecondLevelCacheLogger;
import org.hibernate.cache.spi.support.RegionFactoryTemplate;
import org.hibernate.cache.spi.support.RegionNameQualifier;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import one.microstream.cache.Cache;
import one.microstream.cache.CacheConfiguration;
import one.microstream.cache.CacheConfigurationFactory;
import one.microstream.cache.CacheManager;
import one.microstream.cache.CachingProvider;
import one.microstream.chars.XChars;


public class CacheRegionFactory extends RegionFactoryTemplate
{
	private final    CacheKeysFactory                   cacheKeysFactory;
	private volatile CacheManager                       cacheManager;
	private volatile boolean                            isExplicitCacheManager;
	private volatile CacheConfiguration<Object, Object> cacheConfiguration;
	private volatile MissingCacheStrategy               missingCacheStrategy;
	
	public CacheRegionFactory()
	{
		this(DefaultCacheKeysFactory.INSTANCE);
	}
	
	public CacheRegionFactory(
		final CacheKeysFactory cacheKeysFactory
	)
	{
		super();
		this.cacheKeysFactory = cacheKeysFactory;
	}
	
	@Override
	protected CacheKeysFactory getImplicitCacheKeysFactory() 
	{
		return this.cacheKeysFactory;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void prepareForUse(
		final SessionFactoryOptions settings, 
		final Map properties
	)
	{
		this.cacheManager         = this.resolveCacheManager(
			settings, 
			properties
		);
		
		this.cacheConfiguration   = this.resolveCacheConfiguration(
			settings, 
			properties
		);
		
		this.missingCacheStrategy = MissingCacheStrategy.ofSetting(
			properties.get(ConfigSettings.MISSING_CACHE_STRATEGY)
		);
	}

	@SuppressWarnings("rawtypes") 
	protected CacheManager resolveCacheManager(
		final SessionFactoryOptions settings, 
		final Map properties
	)
	{
		final Object explicitCacheManager = properties.get(ConfigurationPropertyNames.CACHE_MANAGER);
		this.isExplicitCacheManager = explicitCacheManager != null;
		return this.isExplicitCacheManager
			? this.useExplicitCacheManager(settings, explicitCacheManager)
			: this.createCacheManager     (settings, properties)
		;
	}

	@SuppressWarnings("unchecked")
	protected CacheManager useExplicitCacheManager(
		final SessionFactoryOptions settings, 
		final Object setting
	)
	{
		if(setting instanceof CacheManager) 
		{
			return (CacheManager)setting;
		}

		final Class<? extends CacheManager> cacheManagerClass = setting instanceof Class
			? (Class<? extends CacheManager>)setting
			: settings.getServiceRegistry()
				.getService(ClassLoaderService.class)
				.classForName(setting.toString())
		;
		
		try
		{
			return cacheManagerClass.newInstance();
		}
		catch(InstantiationException | IllegalAccessException e) 
		{
			throw new CacheException("Could not use explicit CacheManager : " + setting);
		}
	}

	@SuppressWarnings({"rawtypes", "resource"})
	protected CacheManager createCacheManager(
		final SessionFactoryOptions settings, 
		final Map properties) 
	{
		return new CachingProvider().getCacheManager();
	}
	
	@SuppressWarnings("rawtypes")
	protected CacheConfiguration<Object, Object> resolveCacheConfiguration(
		final SessionFactoryOptions settings, 
		final Map properties
	)
	{
		final String configurationResourceName = properties != null
			? (String)properties.get(ConfigurationPropertyNames.CONFIGURATION_RESOURCE_NAME)
			: null;
		if(!XChars.isEmpty(configurationResourceName))
		{
			final URL url = this.loadResource(configurationResourceName, settings);
			if(url != null)
			{
				return CacheConfigurationFactory.Load(url, Object.class, Object.class);
			}
		}
		CacheConfiguration<Object, Object> configuration = 
			CacheConfigurationFactory.Load(Object.class, Object.class);
		return configuration != null
			? configuration
			: CacheConfiguration.Builder(Object.class, Object.class)
				.storeByReference()
				.build();
	}
	
	protected URL loadResource(
		final String configurationResourceName,
		final SessionFactoryOptions settings
	) 
	{
		if(!super.isStarted()) 
		{
			throw new IllegalStateException("Cannot load resource through a non-started CacheRegionFactory");
		}
		
		URL url = settings.getServiceRegistry()
			.getService(ClassLoaderService.class)
			.locateResource(configurationResourceName);

		if(url == null) 
		{
			final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
			if(contextClassloader != null) 
			{
				url = contextClassloader.getResource(configurationResourceName);
			}
			if(url == null) 
			{
				url = this.getClass().getResource(configurationResourceName);
				
				if(url == null) 
				{
					try 
					{
						url = new URL(configurationResourceName);
					}
					catch(MalformedURLException e) 
					{
						// ignore
					}
				}
			}
		}
		
		return url;
	}

	@Override
	protected void releaseFromUse()
	{
		if(this.cacheManager != null)
		{
			try
			{
				if(!this.isExplicitCacheManager)
				{
					this.cacheManager.close();
				}
			}
			finally
			{
				this.cacheManager = null;
			}
		}
	}

	@Override
	protected StorageAccess createQueryResultsRegionStorageAccess(
		final String regionName,
		final SessionFactoryImplementor sessionFactory
	)
	{
		final String defaultedRegionName = this.defaultRegionName(
			regionName,
			sessionFactory,
			DEFAULT_QUERY_RESULTS_REGION_UNQUALIFIED_NAME,
			LEGACY_QUERY_RESULTS_REGION_UNQUALIFIED_NAMES
		);
		return StorageAccess.New(
			this.getOrCreateCache(defaultedRegionName, sessionFactory) 
		);
	}

	@Override
	protected StorageAccess createTimestampsRegionStorageAccess(
		final String regionName,
		final SessionFactoryImplementor sessionFactory	
	)
	{
		final String defaultedRegionName = this.defaultRegionName(
			regionName,
			sessionFactory,
			DEFAULT_UPDATE_TIMESTAMPS_REGION_UNQUALIFIED_NAME,
			LEGACY_UPDATE_TIMESTAMPS_REGION_UNQUALIFIED_NAMES
		);
		return StorageAccess.New(
			this.getOrCreateCache(defaultedRegionName, sessionFactory)
		);
	}

	protected final String defaultRegionName(
		final String regionName, 
		final SessionFactoryImplementor sessionFactory,
		final String defaultRegionName, 
		final List<String> legacyDefaultRegionNames
	) 
	{
		if(defaultRegionName.equals(regionName) && !this.cacheExists(regionName, sessionFactory)) 
		{
			for(String legacyDefaultRegionName : legacyDefaultRegionNames) 
			{
				if(cacheExists(legacyDefaultRegionName, sessionFactory)) 
				{
					SecondLevelCacheLogger.INSTANCE.usingLegacyCacheName( 
						defaultRegionName, 
						legacyDefaultRegionName
					);
					return legacyDefaultRegionName;
				}
			}
		}

		return regionName;
	}

	protected boolean cacheExists(
		final String unqualifiedRegionName, 
		final SessionFactoryImplementor sessionFactory
	) 
	{
		final String qualifiedRegionName = RegionNameQualifier.INSTANCE.qualify(
			unqualifiedRegionName,
			sessionFactory.getSessionFactoryOptions()
		);
		return this.cacheManager.getCache(qualifiedRegionName) != null;
	}

	protected Cache<Object, Object> getOrCreateCache(
		final String unqualifiedRegionName, 
		final SessionFactoryImplementor sessionFactory
	) 
	{
		this.verifyStarted();

		final String qualifiedRegionName = RegionNameQualifier.INSTANCE.qualify(
			unqualifiedRegionName,
			sessionFactory.getSessionFactoryOptions()
		);

		final Cache<Object, Object> cache = this.cacheManager.getCache(qualifiedRegionName);
		return cache != null
			? cache
			: this.createCache(qualifiedRegionName)
		;
	}

	protected Cache<Object, Object> createCache(
		final String regionName
	)
	{
		switch(this.missingCacheStrategy) 
		{
			case CREATE_WARN:
				SecondLevelCacheLogger.INSTANCE.missingCacheCreated(
					regionName,
					ConfigurationPropertyNames.MISSING_CACHE_STRATEGY, 
					MissingCacheStrategy.CREATE.getExternalRepresentation()
				);
				// fall-through to create
				
			case CREATE:
				return this.cacheManager.createCache(
					regionName, 
					this.cacheConfiguration
				);
				
			case FAIL:
				throw new CacheException("On-the-fly creation of MicroStream Cache objects is not supported [" + regionName + "]");
				
			default:
				throw new IllegalStateException("Unsupported missing cache strategy: " + missingCacheStrategy);
		}
	}
	
}
