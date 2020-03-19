package one.microstream.cache;

public interface CacheConfigurationPropertyNames
{
	public static final String PREFIX                   = "one.microstream.cache.";
	
	public static final String KEY_TYPE                 = PREFIX + "keyType";
	public static final String VALUE_TYPE               = PREFIX + "valueType";
	public static final String CACHE_LOADER_FACTORY     = PREFIX + "cacheLoaderFactory";
	public static final String CACHE_WRITER_FACTORY     = PREFIX + "cacheWriterFactory";
	public static final String EXPIRY_POLICY_FACTORY    = PREFIX + "expiryPolicyFactory";
	public static final String EVICTION_MANAGER_FACTORY = PREFIX + "evictionManagerFactory";
	public static final String READ_THROUGH             = PREFIX + "readThrough";
	public static final String WRITE_THROUGH            = PREFIX + "writeThrough";
	public static final String STORE_BY_VALUE           = PREFIX + "storeByValue";
	public static final String STATISTICS_ENABLED       = PREFIX + "statisticsEnabled";
	public static final String MANAGEMENT_ENABLED       = PREFIX + "managementEnabled";
}
