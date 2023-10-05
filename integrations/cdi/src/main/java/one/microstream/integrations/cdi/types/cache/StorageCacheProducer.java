
package one.microstream.integrations.cdi.types.cache;

/*-
 * #%L
 * microstream-integrations-cdi
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.Objects;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;

import one.microstream.storage.types.StorageManager;


@ApplicationScoped
class StorageCacheProducer
{
	private static final Logger      LOGGER         = Logger.getLogger(StorageCacheProducer.class.getName());
	
	private static final String      CACHE_PROVIDER = "one.microstream.cache.types.CachingProvider";
	
	private CachingProvider          provider;
	
	private CacheManager             cacheManager;
	
	@Inject
	private Config                   config;
	
	@Inject
	private Instance<StorageManager> storageManager;
	
	@PostConstruct
	void setUp()
	{
		this.provider     = Caching.getCachingProvider(CACHE_PROVIDER);
		this.cacheManager = this.provider.getCacheManager();
	}
	
	@Produces
	@StorageCache
	@ApplicationScoped
	CachingProvider getProvider()
	{
		return this.provider;
	}
	
	@Produces
	@StorageCache
	@ApplicationScoped
	CacheManager getManager()
	{
		return this.cacheManager;
	}
	
	@SuppressWarnings("resource")
	@Produces
	@StorageCache
	public <K, V> Cache<K, V> producer(final InjectionPoint injectionPoint)
	{
		final StorageCacheProperty<K, V> cacheProperty = StorageCacheProperty.of(injectionPoint);
		final String                     name          = cacheProperty.getName();
		final Class<K>                   key           = cacheProperty.getKey();
		final Class<V>                   value         = cacheProperty.getValue();
		
		LOGGER.info("Loading cache: " + name + " the current caches: " + this.cacheManager.getCacheNames());
		
		Cache<K, V> cache;
		if(Objects.isNull(this.cacheManager.getCache(name, key, value)))
		{
			final MutableConfigurationSupplier<K, V> supplier =
				MutableConfigurationSupplier.of(cacheProperty, this.config, this.storageManager);
			LOGGER.info("Cache " + name + " does not exist. Creating with configuration: " + supplier);
			final MutableConfiguration<K, V> configuration = supplier.get();
			cache = this.cacheManager.createCache(name, configuration);
		}
		else
		{
			cache = this.cacheManager.getCache(name);
		}
		return cache;
	}
	
	public void close(@Disposes @StorageCache final CachingProvider provider)
	{
		provider.close();
	}
	
	public void close(@Disposes @StorageCache final CacheManager manager)
	{
		manager.close();
	}
}
