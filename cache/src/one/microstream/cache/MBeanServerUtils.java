
package one.microstream.cache;

import java.lang.management.ManagementFactory;

import javax.cache.CacheException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


class MBeanServerUtils
{
	private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
	public static void registerCacheObject(final Cache<?, ?> cache, final Object bean)
	{
		final ObjectName objectName = createObjectName(cache, bean);
		
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
	
	public static void unregisterCacheObject(final Cache<?, ?> cache, final Object bean)
	{
		mBeanServer.queryNames(
			createObjectName(cache, bean),
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
	
	private static ObjectName createObjectName(final Cache<?, ?> cache, final Object bean)
	{
		final String cacheManagerName = normalize(cache.getCacheManager().getURI().toString());
		final String cacheName        = normalize(cache.getName());
		
		try
		{
			return new ObjectName("javax.cache:type=" + bean.getClass().getSimpleName()
				+ ",CacheManager=" + cacheManagerName + ",Cache=" + cacheName);
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
			: string.replaceAll(",|:|=|\n", ".").replace("//", "");
	}
	
	private MBeanServerUtils()
	{
		throw new Error();
	}
}
