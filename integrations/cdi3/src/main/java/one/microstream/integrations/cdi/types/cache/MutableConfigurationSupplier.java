
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

import org.eclipse.microprofile.config.Config;

import jakarta.enterprise.inject.Instance;
import one.microstream.cache.types.CacheStore;
import one.microstream.storage.types.StorageManager;


/**
 * Create a Parser to explore the benefits of Eclipse MicroProfile Configuration.
 *
 * @param <K>
 *            the key type in the cache
 * @param <V>
 *            the value type in the cache
 */
class MutableConfigurationSupplier<K, V> implements Supplier<MutableConfiguration<K, V>>
{
	private static final Logger              LOGGER = Logger.getLogger(MutableConfigurationSupplier.class.getName());
	
	private final StorageCacheProperty<K, V> cacheProperty    ;
	private final boolean                    storeByValue     ;
	private final boolean                    writeThrough     ;
	private final boolean                    readThrough      ;
	private final boolean                    managementEnabled;
	private final boolean                    statisticsEnabled;
	private final boolean                    storage          ;
	private final Factory<CacheLoader<K, V>> loaderFactory    ;
	private final Factory<CacheWriter<K, V>> writerFactory    ;
	private final Factory<ExpiryPolicy>      expiryFactory    ;
	
	private final Instance<StorageManager>   storageManager;
	
	private MutableConfigurationSupplier(
		final StorageCacheProperty<K, V> cacheProperty    ,
		final boolean                    storeByValue     ,
		final boolean                    writeThrough     ,
		final boolean                    readThrough      ,
		final boolean                    managementEnabled,
		final boolean                    statisticsEnabled,
		final boolean                    storage          ,
		final Factory<CacheLoader<K, V>> loaderFactory    ,
		final Factory<CacheWriter<K, V>> writerFactory    ,
		final Factory<ExpiryPolicy>      expiryFactory    ,
		final Instance<StorageManager>   storageManager
	)
	{
		this.cacheProperty     = cacheProperty    ;
		this.storeByValue      = storeByValue     ;
		this.writeThrough      = writeThrough     ;
		this.readThrough       = readThrough      ;
		this.managementEnabled = managementEnabled;
		this.statisticsEnabled = statisticsEnabled;
		this.loaderFactory     = loaderFactory    ;
		this.writerFactory     = writerFactory    ;
		this.expiryFactory     = expiryFactory    ;
		this.storage           = storage          ;
		this.storageManager    = storageManager   ;
	}
	
	public static <K, V> MutableConfigurationSupplier<K, V> of(
		final StorageCacheProperty<K, V> cacheProperty,
		final Config config,
		final Instance<StorageManager> storageManager)
	{
		final boolean                    storeByValue      = CacheProperties.isStoreByValue(config);
		final boolean                    writeThrough      = CacheProperties.isWriteThrough(config);
		final boolean                    readThrough       = CacheProperties.isReadThrough(config);
		final boolean                    managementEnabled = CacheProperties.isManagementEnabled(config);
		final boolean                    statisticsEnabled = CacheProperties.isStatisticsEnabled(config);
		final boolean                    storage           = CacheProperties.isStorage(config);
		final Factory<CacheLoader<K, V>> loaderFactory     = CacheProperties.getLoaderFactory(config);
		final Factory<CacheWriter<K, V>> writerFactory     = CacheProperties.getWriterFactory(config);
		final Factory<ExpiryPolicy>      expiryFactory     = CacheProperties.getExpiryFactory(config);
		
		return new MutableConfigurationSupplier<>(
			cacheProperty,
			storeByValue,
			writeThrough,
			readThrough,
			managementEnabled,
			statisticsEnabled,
			storage,
			loaderFactory,
			writerFactory,
			expiryFactory,
			storageManager);
	}
	
	@Override
	public MutableConfiguration<K, V> get()
	{
		final Class<K>                   key           = this.cacheProperty.getKey();
		final Class<V>                   value         = this.cacheProperty.getValue();
		final MutableConfiguration<K, V> configuration = new MutableConfiguration<>();
		configuration.setTypes(key, value);
		configuration.setStoreByValue(this.storeByValue).setWriteThrough(this.writeThrough).setReadThrough(
			this.readThrough).setManagementEnabled(this.managementEnabled).setStatisticsEnabled(this.statisticsEnabled);
		
		if(Objects.nonNull(this.loaderFactory))
		{
			configuration.setCacheLoaderFactory(this.loaderFactory);
		}
		if(Objects.nonNull(this.writerFactory))
		{
			configuration.setCacheWriterFactory(this.writerFactory);
		}
		if(Objects.nonNull(this.expiryFactory))
		{
			configuration.setExpiryPolicyFactory(this.expiryFactory);
		}
		if(this.storage)
		{
			LOGGER.log(Level.FINE, "Using the storage option to this cache, so it will enable write and read through");
			final StorageManager storageManager = this.storageManager.get();
			final CacheStore<K, V> cacheStore = CacheStore.New(this.cacheProperty.getName(),storageManager);
			configuration.setCacheLoaderFactory(() -> cacheStore);
			configuration.setCacheWriterFactory(() -> cacheStore);
			configuration.setWriteThrough(true);
			configuration.setReadThrough(true);

		}
		return configuration;
	}
	
	@Override
	public String toString()
	{
		return "MutableConfigurationSupplier{"
			+
			"cacheProperty="
			+ this.cacheProperty
			+
			", storeByValue="
			+ this.storeByValue
			+
			", writeThrough="
			+ this.writeThrough
			+
			", readThrough="
			+ this.readThrough
			+
			", managementEnabled="
			+ this.managementEnabled
			+
			", statisticsEnabled="
			+ this.statisticsEnabled
			+
			", storage="
			+ this.storage
			+
			", loaderFactory="
			+ this.loaderFactory
			+
			", writerFactory="
			+ this.writerFactory
			+
			", expiryFactory="
			+ this.expiryFactory
			+
			'}';
	}
	
}
