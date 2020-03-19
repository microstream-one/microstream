package one.microstream.cache.hibernate;

public interface ConfigurationPropertyNames
{
	public static String PREFIX                      = "hibernate.cache.microstream.";
	                                                 
	public static String CACHE_MANAGER               = PREFIX + "cache_manager";
	                                                 
	public static String MISSING_CACHE_STRATEGY      = PREFIX + "missing_cache_strategy";
	
	public static String CONFIGURATION_RESOURCE_NAME = PREFIX + "configurationResourceName";
	
	public static String CACHE_LOCK_TIMEOUT          = PREFIX + "cache_lock_timeout";
}
