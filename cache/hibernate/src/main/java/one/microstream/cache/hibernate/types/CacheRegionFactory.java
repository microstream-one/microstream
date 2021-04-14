package one.microstream.cache.hibernate.types;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.cfg.spi.DomainDataRegionBuildingContext;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.DomainDataRegion;
import org.hibernate.cache.spi.SecondLevelCacheLogger;
import org.hibernate.cache.spi.support.DomainDataRegionImpl;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.cache.spi.support.RegionFactoryTemplate;
import org.hibernate.cache.spi.support.RegionNameQualifier;
import org.hibernate.cache.spi.support.SimpleTimestamper;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import one.microstream.cache.types.Cache;
import one.microstream.cache.types.CacheConfiguration;
import one.microstream.cache.types.CacheConfigurationBuilderConfigurationBased;
import one.microstream.cache.types.CacheManager;
import one.microstream.cache.types.CachingProvider;
import one.microstream.chars.XChars;
import one.microstream.configuration.types.ConfigurationMapperMap;


public class CacheRegionFactory extends RegionFactoryTemplate
{
	private final    CacheKeysFactory                   cacheKeysFactory;
	private volatile CacheManager                       cacheManager;
	private volatile boolean                            isExplicitCacheManager;
	private volatile CacheConfiguration<Object, Object> cacheConfiguration;
	private volatile MissingCacheStrategy               missingCacheStrategy;
	private volatile long                               cacheLockTimeout;

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
			properties.get(ConfigurationPropertyNames.MISSING_CACHE_STRATEGY)
		);

		final Object cacheLockTimeoutConfigValue = properties.get(
			ConfigurationPropertyNames.CACHE_LOCK_TIMEOUT
		);
		if(cacheLockTimeoutConfigValue != null)
		{
			final int lockTimeoutInMillis = cacheLockTimeoutConfigValue instanceof String
				? Integer.decode((String)cacheLockTimeoutConfigValue)
				: ((Number)cacheLockTimeoutConfigValue).intValue()
			;
			this.cacheLockTimeout = SimpleTimestamper.ONE_MS * lockTimeoutInMillis;
		}
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

		try
		{
			final Class<? extends CacheManager> cacheManagerClass = setting instanceof Class
				? (Class<? extends CacheManager>)setting
				: this.loadClass(setting.toString(), settings)
			;
			return cacheManagerClass.newInstance();
		}
		catch(ClassNotFoundException | InstantiationException | IllegalAccessException e)
		{
			throw new CacheException("Could not use explicit CacheManager : " + setting, e);
		}
	}

	@SuppressWarnings({"rawtypes", "resource"})
	protected CacheManager createCacheManager(
		final SessionFactoryOptions settings,
		final Map properties)
	{
		return new CachingProvider().getCacheManager();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected CacheConfiguration<Object, Object> resolveCacheConfiguration(
		final SessionFactoryOptions settings,
		final Map properties
	)
	{
		// 1. Check for configured external resource
		final String configurationResourceName = properties != null
			? (String)properties.get(ConfigurationPropertyNames.CONFIGURATION_RESOURCE_NAME)
			: null;
		if(!XChars.isEmpty(configurationResourceName))
		{
			final URL url = this.loadResource(configurationResourceName, settings);
			if(url == null)
			{
				throw new CacheException("Storage configuration not found: " + configurationResourceName);
			}
			return CacheConfiguration.load(url, Object.class, Object.class);
		}

		// 2. Check for properties in context config
		final String              prefix            = "hibernate.cache.microstream.";
		final Map<String, String> msCacheProperties = ((Map<Object, Object>)properties).entrySet().stream()
			.filter(kv -> kv.getKey().toString().startsWith(prefix))
			.collect(Collectors.toMap(
				kv -> kv.getKey().toString().substring(prefix.length()),
				kv -> kv.getValue().toString()
			))
		;
		if(msCacheProperties.size() > 0L)
		{
			return (CacheConfiguration<Object, Object>)CacheConfigurationBuilderConfigurationBased.New(
				className -> this.loadClass(className, settings)
			)
			.buildCacheConfiguration(
				ConfigurationMapperMap.New()
					.mapConfiguration(msCacheProperties)
					.buildConfiguration()
			)
			.build();
		}

		// 3. Check for default property resource
		final CacheConfiguration<Object, Object> configuration =
			CacheConfiguration.load(Object.class, Object.class);
		return configuration != null
			? configuration
		// 4. Otherwise use simple default configuration
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
			.locateResource(configurationResourceName)
		;

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
					catch(final MalformedURLException e)
					{
						// ignore
					}
				}
			}
		}

		return url;
	}

	@SuppressWarnings("unchecked")
	protected <T> Class<T> loadClass(
		final String configurationClassName,
		final SessionFactoryOptions settings
	) throws ClassNotFoundException
	{
		if(!super.isStarted())
		{
			throw new IllegalStateException("Cannot load class through a non-started CacheRegionFactory");
		}

		Class<T> clazz = settings.getServiceRegistry()
			.getService(ClassLoaderService.class)
			.classForName(configurationClassName)
		;

		if(clazz == null)
		{
			final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
			if(contextClassloader != null)
			{
				clazz = (Class<T>)contextClassloader.loadClass(configurationClassName);
			}
			if(clazz == null)
			{
				clazz = (Class<T>)Class.forName(configurationClassName);
			}
		}

		return clazz;
	}

	@Override
	public long getTimeout()
	{
		return this.cacheLockTimeout;
	}

	@Override
	protected DomainDataStorageAccess createDomainDataStorageAccess(
		final DomainDataRegionConfig regionConfig,
		final DomainDataRegionBuildingContext buildingContext)
	{
		return StorageAccess.New(
			this.getOrCreateCache(regionConfig.getRegionName(), buildingContext.getSessionFactory())
		);
	}

	@Override
	public DomainDataRegion buildDomainDataRegion(
		final DomainDataRegionConfig regionConfig,
		final DomainDataRegionBuildingContext buildingContext
	)
	{
		return new DomainDataRegionImpl(
			regionConfig,
			this,
			this.createDomainDataStorageAccess(regionConfig, buildingContext),
			this.cacheKeysFactory,
			buildingContext
		);
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
			for(final String legacyDefaultRegionName : legacyDefaultRegionNames)
			{
				if(this.cacheExists(legacyDefaultRegionName, sessionFactory))
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
				throw new IllegalStateException("Unsupported missing cache strategy: " + this.missingCacheStrategy);
		}
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

}
