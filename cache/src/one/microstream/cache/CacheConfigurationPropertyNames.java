package one.microstream.cache;

public interface CacheConfigurationPropertyNames
{                                                 
	public static final String KEY_TYPE                            = "keyType";
	public static final String VALUE_TYPE                          = "valueType";
	public static final String STORAGE_CONFIGURATION_RESOURCE_NAME = "storageConfigurationResourceName";
	public static final String CACHE_LOADER_FACTORY                = "cacheLoaderFactory";
	public static final String CACHE_WRITER_FACTORY                = "cacheWriterFactory";
	public static final String EXPIRY_POLICY_FACTORY               = "expiryPolicyFactory";
	public static final String EVICTION_MANAGER_FACTORY            = "evictionManagerFactory";
	public static final String READ_THROUGH                        = "readThrough";
	public static final String WRITE_THROUGH                       = "writeThrough";
	public static final String STORE_BY_VALUE                      = "storeByValue";
	public static final String STATISTICS_ENABLED                  = "statisticsEnabled";
	public static final String MANAGEMENT_ENABLED                  = "managementEnabled";
}
