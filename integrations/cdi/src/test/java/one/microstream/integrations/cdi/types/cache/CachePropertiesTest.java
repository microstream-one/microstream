
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


import io.smallrye.config.inject.ConfigExtension;
import one.microstream.integrations.cdi.types.config.StorageManagerProducer;
import one.microstream.integrations.cdi.types.extension.StorageExtension;
import org.eclipse.microprofile.config.Config;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.cache.configuration.Factory;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import javax.inject.Inject;


@EnableAutoWeld
@AddBeanClasses({StorageCacheProducer.class, StorageManagerProducer.class})  // For @StorageCache
@AddExtensions({StorageExtension.class, ConfigExtension.class})
// SmallRye Config extension And MicroStream extension for StorageManager
class CachePropertiesTest
{
	@Inject
	private Config config;
	
	@Test
	@DisplayName("Should get the default value in the storeByValue")
	public void shouldReturnStoreByValue()
	{
		final boolean storeByValue = CacheProperties.isStoreByValue(this.config);
		Assertions.assertFalse(storeByValue);
	}
	
	@Test
	public void shouldReturnErrorWhenConfigIsNull()
	{
		Assertions.assertThrows(NullPointerException.class, () -> CacheProperties.isStoreByValue(null));
		Assertions.assertThrows(NullPointerException.class, () -> CacheProperties.isWriteThrough(null));
		Assertions.assertThrows(NullPointerException.class, () -> CacheProperties.isReadThrough(null));
		Assertions.assertThrows(NullPointerException.class, () -> CacheProperties.isManagementEnabled(null));
		Assertions.assertThrows(NullPointerException.class, () -> CacheProperties.isStatisticsEnabled(null));
		Assertions.assertThrows(NullPointerException.class, () -> CacheProperties.getLoaderFactory(null));
		Assertions.assertThrows(NullPointerException.class, () -> CacheProperties.getWriterFactory(null));
		Assertions.assertThrows(NullPointerException.class, () -> CacheProperties.getExpiryFactory(null));
		Assertions.assertThrows(NullPointerException.class, () -> CacheProperties.isStorage(null));
	}
	
	@Test
	@DisplayName("Should get storeByValue from Eclipse MicroProfile Config")
	public void shouldReturnStoreByValueConfig()
	{
		System.setProperty(CacheProperties.CACHE_STORE_VALUE.get(), "false");
		Assertions.assertFalse(CacheProperties.isStoreByValue(this.config));
		System.setProperty(CacheProperties.CACHE_STORE_VALUE.get(), "true");
		Assertions.assertTrue(CacheProperties.isStoreByValue(this.config));
		System.clearProperty(CacheProperties.CACHE_STORE_VALUE.get());
	}
	
	@Test
	@DisplayName("Should get the default value in the writeThrough")
	public void shouldReturnWriteThrough()
	{
		final boolean storeByValue = CacheProperties.isWriteThrough(this.config);
		Assertions.assertFalse(storeByValue);
	}
	
	@Test
	@DisplayName("Should get writeThrough from Eclipse MicroProfile Config")
	public void shouldReturnSWriteThroughConfig()
	{
		System.setProperty(CacheProperties.CACHE_WRITE_THROUGH.get(), "false");
		Assertions.assertFalse(CacheProperties.isWriteThrough(this.config));
		System.setProperty(CacheProperties.CACHE_WRITE_THROUGH.get(), "true");
		Assertions.assertTrue(CacheProperties.isWriteThrough(this.config));
		System.clearProperty(CacheProperties.CACHE_WRITE_THROUGH.get());
	}
	
	@Test
	@DisplayName("Should get the default value in the readThrough")
	public void shouldReturnReadThrough()
	{
		final boolean storeByValue = CacheProperties.isReadThrough(this.config);
		Assertions.assertFalse(storeByValue);
	}
	
	@Test
	@DisplayName("Should get readThrough from Eclipse MicroProfile Config")
	public void shouldReturnReadThroughConfig()
	{
		System.setProperty(CacheProperties.CACHE_READ_THROUGH.get(), "false");
		Assertions.assertFalse(CacheProperties.isReadThrough(this.config));
		System.setProperty(CacheProperties.CACHE_READ_THROUGH.get(), "true");
		Assertions.assertTrue(CacheProperties.isReadThrough(this.config));
		System.clearProperty(CacheProperties.CACHE_READ_THROUGH.get());
	}
	
	@Test
	@DisplayName("Should get the default value in the managementEnabled")
	public void shouldReturnManagementEnabled()
	{
		final boolean storeByValue = CacheProperties.isManagementEnabled(this.config);
		Assertions.assertFalse(storeByValue);
	}
	
	@Test
	@DisplayName("Should get managementEnabled from Eclipse MicroProfile Config")
	public void shouldReturnManagementEnabledConfig()
	{
		System.setProperty(CacheProperties.CACHE_MANAGEMENT.get(), "false");
		Assertions.assertFalse(CacheProperties.isManagementEnabled(this.config));
		System.setProperty(CacheProperties.CACHE_MANAGEMENT.get(), "true");
		Assertions.assertTrue(CacheProperties.isManagementEnabled(this.config));
		System.clearProperty(CacheProperties.CACHE_MANAGEMENT.get());
	}
	
	@Test
	@DisplayName("Should get the default value in the statisticsEnabled")
	public void shouldReturnStatisticsEnabled()
	{
		final boolean storeByValue = CacheProperties.isStatisticsEnabled(this.config);
		Assertions.assertFalse(storeByValue);
	}
	
	@Test
	@DisplayName("Should get statisticsEnabled from Eclipse MicroProfile Config")
	public void shouldReturnStatisticsEnabledConfig()
	{
		System.setProperty(CacheProperties.CACHE_STATISTICS.get(), "false");
		Assertions.assertFalse(CacheProperties.isStatisticsEnabled(this.config));
		System.setProperty(CacheProperties.CACHE_STATISTICS.get(), "true");
		Assertions.assertTrue(CacheProperties.isStatisticsEnabled(this.config));
		System.clearProperty(CacheProperties.CACHE_STATISTICS.get());
	}
	
	@Test
	public void shouldReturnNullAsDefaultLoaderFactory()
	{
		Assertions.assertNull(CacheProperties.getLoaderFactory(this.config));
	}
	
	@Test
	public void shouldReturnNullAsDefaultWriterFactory()
	{
		Assertions.assertNull(CacheProperties.getWriterFactory(this.config));
	}
	
	@Test
	public void shouldReturnNullAsDefaultExpireFactory()
	{
		Assertions.assertNull(CacheProperties.getExpiryFactory(this.config));
	}
	
	@Test
	public void shouldReturnErrorWhenIsNotFactoryInstance()
	{
		System.setProperty(CacheProperties.CACHE_LOADER_FACTORY.get(), "java.lang.String");
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> Assertions.assertNull(CacheProperties.getLoaderFactory(this.config)));
		System.clearProperty(CacheProperties.CACHE_LOADER_FACTORY.get());
	}
	
	@Test
	public void shouldCreateLoaderFactory()
	{
		System.setProperty(CacheProperties.CACHE_LOADER_FACTORY.get(), MockLoaderFactory.class.getName());
		final Factory<CacheLoader<Object, Object>> loaderFactory = CacheProperties.getLoaderFactory(this.config);
		Assertions.assertTrue(loaderFactory instanceof MockLoaderFactory);
		System.clearProperty(CacheProperties.CACHE_LOADER_FACTORY.get());
	}
	
	@Test
	public void shouldCreateCacheWriterFactory()
	{
		System.setProperty(CacheProperties.CACHE_WRITER_FACTORY.get(), MockCacheWriter.class.getName());
		final Factory<CacheWriter<Object, Object>> loaderFactory = CacheProperties.getWriterFactory(this.config);
		Assertions.assertTrue(loaderFactory instanceof MockCacheWriter);
		System.clearProperty(CacheProperties.CACHE_WRITER_FACTORY.get());
	}
	
	@Test
	public void shouldCreateExpireFactory()
	{
		System.setProperty(CacheProperties.CACHE_EXPIRES_FACTORY.get(), MockExpiryPolicy.class.getName());
		final Factory<ExpiryPolicy> loaderFactory = CacheProperties.getExpiryFactory(this.config);
		Assertions.assertTrue(loaderFactory instanceof MockExpiryPolicy);
		System.clearProperty(CacheProperties.CACHE_EXPIRES_FACTORY.get());
	}
	
	@Test
	@DisplayName("Should get storage from Eclipse MicroProfile Config")
	public void shouldReturnStorage()
	{
		System.setProperty(CacheProperties.STORAGE.get(), "false");
		Assertions.assertFalse(CacheProperties.isStorage(this.config));
		System.setProperty(CacheProperties.STORAGE.get(), "true");
		Assertions.assertTrue(CacheProperties.isStorage(this.config));
		System.clearProperty(CacheProperties.STORAGE.get());
	}
	
}
