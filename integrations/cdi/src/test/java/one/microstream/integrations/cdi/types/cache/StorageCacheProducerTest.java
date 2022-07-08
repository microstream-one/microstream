
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
import one.microstream.integrations.cdi.types.StorageManagerProducer;
import one.microstream.integrations.cdi.types.extension.StorageExtension;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.Set;


@EnableAutoWeld
@AddBeanClasses({StorageCacheProducer.class, StorageManagerProducer.class})  // For @StorageCache
@AddExtensions({StorageExtension.class, ConfigExtension.class})
// SmallRye Config extension And MicroStream extension for StorageManager
public class StorageCacheProducerTest
{
	@Inject
	@StorageCache
	private Cache<Integer, String> cache;
	
	@Inject
	@StorageCache("jcache2")
	private Cache<Integer, String> cacheB;
	
	@Inject
	private BeanManager            beanManager;
	
	@Test
	public void shouldCreateInjectCache()
	{
		this.cache.put(1, "one");
		Assertions.assertEquals("one", this.cache.get(1));
	}
	
	@Test
	public void shouldNotCreateNonQualifierCache()
	{
		final Set<Bean<?>> beans = this.beanManager.getBeans(Cache.class);
		Assertions.assertEquals(0, beans.size());
	}
	
	@Test
	public void shouldShouldHaveDifferentInstance()
	{
		this.cache.put(1, "one");
		Assertions.assertNotEquals(this.cache, this.cacheB);
		Assertions.assertTrue(this.cache.containsKey(1));
		Assertions.assertFalse(this.cacheB.containsKey(1));
	}
	
}
