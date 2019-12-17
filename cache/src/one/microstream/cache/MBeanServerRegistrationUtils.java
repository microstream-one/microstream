
package one.microstream.cache;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.cache.CacheException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


class MBeanServerRegistrationUtils
{
	private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
	public enum ObjectNameType
	{
		Statistics,
		Configuration
	}
	
	public static void registerCacheObject(
		final Cache<?, ?> cache,
		final ObjectNameType objectNameType)
	{
		// these can change during runtime, so always look it up
		final ObjectName registeredObjectName = calculateObjectName(cache, objectNameType);
		try
		{
			if(objectNameType.equals(ObjectNameType.Configuration))
			{
				if(!isRegistered(cache, objectNameType))
				{
					mBeanServer.registerMBean(cache.getCacheMXBean(), registeredObjectName);
				}
			}
			else if(objectNameType.equals(ObjectNameType.Statistics))
			{
				if(!isRegistered(cache, objectNameType))
				{
					mBeanServer.registerMBean(cache.getCacheStatisticsMXBean(), registeredObjectName);
				}
			}
		}
		catch(final Exception e)
		{
			throw new CacheException("Error registering cache MXBeans for CacheManager "
				+ registeredObjectName + " . Error was " + e.getMessage(), e);
		}
	}
	
	static boolean isRegistered(final Cache<?, ?> cache, final ObjectNameType objectNameType)
	{
		
		Set<ObjectName>  registeredObjectNames = null;
		
		final ObjectName objectName            = calculateObjectName(cache, objectNameType);
		registeredObjectNames = mBeanServer.queryNames(objectName, null);
		
		return !registeredObjectNames.isEmpty();
	}
	
	public static void unregisterCacheObject(
		final Cache<?, ?> cache,
		final ObjectNameType objectNameType)
	{
		
		Set<ObjectName>  registeredObjectNames = null;
		
		final ObjectName objectName            = calculateObjectName(cache, objectNameType);
		registeredObjectNames = mBeanServer.queryNames(objectName, null);
		
		// should just be one
		for(final ObjectName registeredObjectName : registeredObjectNames)
		{
			try
			{
				mBeanServer.unregisterMBean(registeredObjectName);
			}
			catch(final Exception e)
			{
				throw new CacheException("Error unregistering object instance "
					+ registeredObjectName + " . Error was " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Creates an object name using the scheme
	 * "javax.cache:type=Cache&lt;Statistics|Configuration&gt;,CacheManager=&lt;cacheManagerName&gt;,name=&lt;cacheName&gt;"
	 */
	private static ObjectName calculateObjectName(final Cache<?, ?> cache, final ObjectNameType objectNameType)
	{
		final String cacheManagerName = mbeanSafe(cache.getCacheManager().getURI().toString());
		final String cacheName        = mbeanSafe(cache.getName());
		
		try
		{
			return new ObjectName("javax.cache:type=Cache" + objectNameType + ",CacheManager="
				+ cacheManagerName + ",Cache=" + cacheName);
		}
		catch(final MalformedObjectNameException e)
		{
			throw new CacheException("Illegal ObjectName for Management Bean. " +
				"CacheManager=[" + cacheManagerName + "], Cache=[" + cacheName + "]", e);
		}
	}
	
	/**
	 * Filter out invalid ObjectName characters from string.
	 *
	 * @param string
	 *            input string
	 * @return A valid JMX ObjectName attribute value.
	 */
	private static String mbeanSafe(final String string)
	{
		return string == null ? "" : string.replaceAll(",|:|=|\n", ".");
	}
	
	private MBeanServerRegistrationUtils()
	{
		throw new Error();
	}
}
