
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

import io.smallrye.config.inject.ConfigExtension;
import one.microstream.integrations.cdi.types.config.StorageManagerProducer;
import one.microstream.integrations.cdi.types.extension.StorageExtension;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import jakarta.inject.Inject;


@EnableAutoWeld
@AddBeanClasses({StorageCacheProducer.class, StorageManagerProducer.class})  // For @StorageCache
@AddExtensions({StorageExtension.class, ConfigExtension.class})
// SmallRye Config extension And MicroStream extension for StorageManager
public class CacheProducerTest
{
	@Inject
	@StorageCache
	private CachingProvider provider;
	
	@Inject
	@StorageCache
	private CacheManager    cacheManager;
	
	@Test
	public void shouldNotBeNullProvider()
	{
		Assertions.assertNotNull(this.provider);
	}
	
	@Test
	public void shouldNotBeNullManager()
	{
		Assertions.assertNotNull(this.cacheManager);
	}
	
}
