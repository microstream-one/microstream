package one.microstream.cache.hibernate;

public interface ConfigurationPropertyNames
{
	public static final String PREFIX                      = "hibernate.cache.microstream.";
	                                                 
	public static final String CACHE_MANAGER               = PREFIX + "cache_manager";	                                                 
	public static final String MISSING_CACHE_STRATEGY      = PREFIX + "missing_cache_strategy";	
	public static final String CACHE_LOCK_TIMEOUT          = PREFIX + "cache_lock_timeout";
	public static final String CONFIGURATION_RESOURCE_NAME = PREFIX + "configurationResourceName";	
}
