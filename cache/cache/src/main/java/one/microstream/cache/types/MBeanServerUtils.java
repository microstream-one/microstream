
package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.lang.management.ManagementFactory;

import javax.cache.CacheException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


class MBeanServerUtils
{
	public static enum MBeanType
	{		
		CacheConfiguration,		
		CacheStatistics	
	}
	
	private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
	public static void registerCacheObject(
		final Cache<?, ?> cache, 
		final Object bean,
		final MBeanType beanType
	)
	{
		final ObjectName objectName = createObjectName(cache, bean, beanType);
		
		try
		{
			if(mBeanServer.queryNames(objectName, null).isEmpty())
			{
				mBeanServer.registerMBean(bean, objectName);
			}
		}
		catch(final Exception e)
		{
			throw new CacheException("Error registering cache MXBeans for CacheManager "
				+ objectName + " . Error was " + e.getMessage(), e);
		}
	}
	
	public static void unregisterCacheObject(
		final Cache<?, ?> cache, 
		final Object bean,
		final MBeanType beanType
	)
	{
		mBeanServer.queryNames(
			createObjectName(cache, bean, beanType),
			null
		)
		.forEach(MBeanServerUtils::unregisterCacheObject);
	}
	
	private static void unregisterCacheObject(final ObjectName objectName)
	{
		try
		{
			mBeanServer.unregisterMBean(objectName);
		}
		catch(final Exception e)
		{
			throw new CacheException("Error unregistering object instance " + objectName, e);
		}
	}
	
	private static ObjectName createObjectName(
		final Cache<?, ?> cache,
		final Object bean,
		final MBeanType beanType
	)
	{
		final String cacheManagerName = normalize(cache.getCacheManager().getURI().toString());
		final String cacheName        = normalize(cache.getName());
		final String name             = "javax.cache:type=" + beanType.name()
			+ ",CacheManager=" + cacheManagerName + ",Cache=" + cacheName;
		try
		{
			return new ObjectName(name);
		}
		catch(final MalformedObjectNameException e)
		{
			throw new CacheException("Illegal ObjectName for Management Bean. " +
				"CacheManager=[" + cacheManagerName + "], Cache=[" + cacheName + "]", e);
		}
	}
	
	private static String normalize(final String string)
	{
		return string == null
			? ""
			: string.replaceAll(":|=|\n|,", ".");
	}
	
	private MBeanServerUtils()
	{
		throw new Error();
	}
}
