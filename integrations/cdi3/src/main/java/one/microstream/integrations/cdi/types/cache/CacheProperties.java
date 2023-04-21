
package one.microstream.integrations.cdi.types.cache;

/*-
 * #%L
 * microstream-integrations-cdi3
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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
import java.util.function.Supplier;

import javax.cache.configuration.Factory;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

import org.eclipse.microprofile.config.Config;

import one.microstream.reflect.XReflect;


/**
 * The relation with the properties from MicroStream docs:
 * <a href="https://docs.microstream.one/manual/cache/configuration/properties.html">Configuration properties</a>
 */
public enum CacheProperties implements Supplier<String>
{
	PREFIX("one.microstream."),
	/**
	 * cacheLoaderFactory - A CacheLoader should be configured
	 * for "Read Through" caches to load values when a cache miss occurs.
	 */
	CACHE_LOADER_FACTORY(PREFIX.get() + "cache.loader.factory"),
	/**
	 * cacheWriterFactory - A CacheWriter is used for write-through to an external resource.
	 */
	CACHE_WRITER_FACTORY(PREFIX.get() + "cache.writer.factory"),
	/**
	 * expiryPolicyFactory - Determines when cache entries will expire based on creation,
	 * access and modification operations.
	 */
	CACHE_EXPIRES_FACTORY(PREFIX.get() + "cache.expires.factory"),
	/**
	 * readThrough - When in "read-through" mode, cache misses that occur due to cache entries not existing
	 * as a result of performing a "get" will appropriately cause the configured CacheLoader to be invoked.
	 */
	CACHE_READ_THROUGH(PREFIX.get() + "cache.read.through"),
	/**
	 * writeThrough - When in "write-through" mode, cache updates that occur as a result
	 * of performing "put" operations will appropriately cause the configured CacheWriter to be invoked.
	 */
	CACHE_WRITE_THROUGH(PREFIX.get() + "cache.write.through"),
	/**
	 * storeByValue - When a cache is storeByValue,
	 * any mutation to the key or value does not affect the key of value stored in the cache.
	 */
	CACHE_STORE_VALUE(PREFIX.get() + "cache.store.value"),
	/**
	 * statisticsEnabled - Checks whether statistics collection is enabled in this cache.
	 */
	CACHE_STATISTICS(PREFIX.get() + "cache.statistics"),
	/**
	 * managementEnabled - Checks whether management is enabled on this cache.
	 */
	CACHE_MANAGEMENT(PREFIX.get() + "cache.management"),
	/**
	 * MicroStream’s storage can be used as a backing store for the cache.
	 * It functions as a CacheWriter as well as a CacheReader, depending on the writeThrough
	 * and readThrough configuration. Per default, it is used for both.
	 */
	STORAGE(PREFIX.get() + "store");
	
	private final String value;
	
	CacheProperties(final String value)
	{
		this.value = value;
	}
	
	/**
	 * Loads the properties {@link CacheProperties#CACHE_STORE_VALUE} from {@link Config}
	 * the respective value; it will return false by default.
	 *
	 * @param config
	 *            the Eclipse Microprofile instance
	 * @return the properties from {@link Config} or false
	 * @throws NullPointerException
	 *             when config is null
	 */
	static boolean isStoreByValue(final Config config)
	{
		return getBoolean(config, CACHE_STORE_VALUE);
	}
	
	/**
	 * Loads the properties {@link CacheProperties#CACHE_WRITE_THROUGH} from {@link Config}
	 * the respective value; it will return false by default.
	 *
	 * @param config
	 *            the Eclipse Microprofile instance
	 * @return the properties from {@link Config} or false
	 * @throws NullPointerException
	 *             when config is null
	 */
	static boolean isWriteThrough(final Config config)
	{
		return getBoolean(config, CACHE_WRITE_THROUGH);
	}
	
	/**
	 * Loads the properties {@link CacheProperties#CACHE_READ_THROUGH} from {@link Config}
	 * the respective value; it will return false by default.
	 *
	 * @param config
	 *            the Eclipse Microprofile instance
	 * @return the properties from {@link Config} or false
	 * @throws NullPointerException
	 *             when config is null
	 */
	static boolean isReadThrough(final Config config)
	{
		return getBoolean(config, CACHE_READ_THROUGH);
	}
	
	/**
	 * Loads the properties {@link CacheProperties#CACHE_MANAGEMENT} from {@link Config}
	 * the respective value; it will return false by default.
	 *
	 * @param config
	 *            the Eclipse Microprofile instance
	 * @return the properties from {@link Config} or false
	 * @throws NullPointerException
	 *             when config is null
	 */
	static boolean isManagementEnabled(final Config config)
	{
		return getBoolean(config, CACHE_MANAGEMENT);
	}
	
	/**
	 * Loads the properties {@link CacheProperties#CACHE_STATISTICS} from {@link Config}
	 * the respective value; it will return false by default.
	 *
	 * @param config
	 *            the Eclipse Microprofile instance
	 * @return the properties from {@link Config} or false
	 * @throws NullPointerException
	 *             when config is null
	 */
	static boolean isStatisticsEnabled(final Config config)
	{
		return getBoolean(config, CACHE_STATISTICS);
	}
	
	/**
	 * MicroStream’s storage can be used as a backing store for the cache.
	 *
	 * @param config
	 *            the Eclipse Microprofile instance
	 * @return the properties from {@link Config} or false
	 * @throws NullPointerException
	 *             when config is null
	 */
	static boolean isStorage(final Config config)
	{
		return getBoolean(config, STORAGE);
	}
	
	/**
	 * Loads the properties {@link CacheProperties#CACHE_LOADER_FACTORY} from {@link Config}
	 * the respective value
	 *
	 * @param config
	 *            the Eclipse Microprofile instance
	 * @return the Factory from {@link Config} or null
	 * @throws NullPointerException
	 *             when config is null
	 */
	static <V, K> Factory<CacheLoader<K, V>> getLoaderFactory(final Config config)
	{
		Objects.requireNonNull(config, "Config is required");
		final String factoryClass = config.getOptionalValue(CACHE_LOADER_FACTORY.get(), String.class).orElse("");
		return getFactoryClass(factoryClass);
	}
	
	/**
	 * Loads the properties {@link CacheProperties#CACHE_WRITER_FACTORY} from {@link Config}
	 * the respective value
	 *
	 * @param config
	 *            the Eclipse Microprofile instance
	 * @return the Factory from {@link Config} or null
	 * @throws NullPointerException
	 *             when config is null
	 */
	static <V, K> Factory<CacheWriter<K, V>> getWriterFactory(final Config config)
	{
		Objects.requireNonNull(config, "Config is required");
		final String factoryClass = config.getOptionalValue(CACHE_WRITER_FACTORY.get(), String.class).orElse("");
		return getFactoryClass(factoryClass);
	}
	
	/**
	 * Loads the properties {@link CacheProperties#CACHE_EXPIRES_FACTORY} from {@link Config}
	 * the respective value
	 *
	 * @param config
	 *            the Eclipse Microprofile instance
	 * @return the Factory from {@link Config} or null
	 * @throws NullPointerException
	 *             when config is null
	 */
	static Factory<ExpiryPolicy> getExpiryFactory(final Config config)
	{
		Objects.requireNonNull(config, "Config is required");
		final String factoryClass = config.getOptionalValue(CACHE_EXPIRES_FACTORY.get(), String.class).orElse("");
		
		return getFactoryClass(factoryClass);
	}
	
	private static Boolean getBoolean(final Config config, final CacheProperties cacheStatistics)
	{
		Objects.requireNonNull(config, "Config is required");
		return config.getOptionalValue(cacheStatistics.get(), boolean.class).orElse(false);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getFactoryClass(final String className)
	{
		if(className.isBlank())
		{
			return null;
		}
		try
		{
			final Class<T> factory  = (Class<T>)Class.forName(className);
			final T        instance = XReflect.defaultInstantiate(factory);
			if(instance instanceof Factory)
			{
				return instance;
			}
			throw new IllegalArgumentException(
				"The instance class must be a "
					+ Factory.class.getName()
					+ " implementation, please check the class: "
					+ className);
		}
		catch(final ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String get()
	{
		return this.value;
	}
}
