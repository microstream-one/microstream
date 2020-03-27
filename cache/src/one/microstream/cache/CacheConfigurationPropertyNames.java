package one.microstream.cache;

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;

/**
 * All supported properties for external configuration files.
 * 
 */
public interface CacheConfigurationPropertyNames
{
	/**
	 * @see Configuration#getKeyType()
	 */
	public static final String KEY_TYPE                            = "keyType";
	
	/**
	 * @see Configuration#getValueType()
	 */
	public static final String VALUE_TYPE                          = "valueType";
	
	/**
	 * Path for the {@link one.microstream.storage.configuration.Configuration} for the backing store.
	 */
	public static final String STORAGE_CONFIGURATION_RESOURCE_NAME = "storageConfigurationResourceName";
	
	/**
	 * @see CompleteConfiguration#getCacheLoaderFactory()
	 */
	public static final String CACHE_LOADER_FACTORY                = "cacheLoaderFactory";
	
	/**
	 * @see CompleteConfiguration#getCacheWriterFactory()
	 */
	public static final String CACHE_WRITER_FACTORY                = "cacheWriterFactory";
	
	/**
	 * @see CompleteConfiguration#getExpiryPolicyFactory()
	 */
	public static final String EXPIRY_POLICY_FACTORY               = "expiryPolicyFactory";
	
	/**
	 * @see CacheConfiguration#getEvictionManagerFactory()
	 */
	public static final String EVICTION_MANAGER_FACTORY            = "evictionManagerFactory";
	
	/**
	 * @see CompleteConfiguration#isReadThrough()
	 */
	public static final String READ_THROUGH                        = "readThrough";
	
	/**
	 * @see CompleteConfiguration#isWriteThrough()
	 */
	public static final String WRITE_THROUGH                       = "writeThrough";
	
	/**
	 * @see CompleteConfiguration#isStoreByValue()
	 */
	public static final String STORE_BY_VALUE                      = "storeByValue";
	
	/**
	 * @see CompleteConfiguration#isStatisticsEnabled()
	 */
	public static final String STATISTICS_ENABLED                  = "statisticsEnabled";
	
	/**
	 * @see CompleteConfiguration#isManagementEnabled()
	 */
	public static final String MANAGEMENT_ENABLED                  = "managementEnabled";
	
}
