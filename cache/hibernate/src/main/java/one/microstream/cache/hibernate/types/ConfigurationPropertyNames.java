package one.microstream.cache.hibernate.types;

public interface ConfigurationPropertyNames
{
	public static final String PREFIX                      = "hibernate.cache.microstream.";
	                                                 
	public static final String CACHE_MANAGER               = PREFIX + "cache-manager";
	public static final String MISSING_CACHE_STRATEGY      = PREFIX + "missing-cache-strategy";
	public static final String CACHE_LOCK_TIMEOUT          = PREFIX + "cache-lock-timeout";
	public static final String CONFIGURATION_RESOURCE_NAME = PREFIX + "configuration-resource-name";
}
