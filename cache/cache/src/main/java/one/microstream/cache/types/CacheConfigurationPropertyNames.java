package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
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
	public static final String KEY_TYPE                            = "key-type";
	
	/**
	 * @see Configuration#getValueType()
	 */
	public static final String VALUE_TYPE                          = "value-type";
	
	/**
	 * Path for the {@link one.microstream.configuration.types.Configuration} for the backing store.
	 */
	public static final String STORAGE_CONFIGURATION_RESOURCE_NAME = "storage-configuration-resource-name";
	
	/**
	 * Sub-configuration name for the backing store.
	 */
	public static final String STORAGE = "storage";
	
	/**
	 * Storage key of the backing store
	 */
	public static final String STORAGE_KEY = "key";
	
	/**
	 * @see CompleteConfiguration#getCacheLoaderFactory()
	 */
	public static final String CACHE_LOADER_FACTORY                = "cache-loader-factory";
	
	/**
	 * @see CompleteConfiguration#getCacheWriterFactory()
	 */
	public static final String CACHE_WRITER_FACTORY                = "cache-writer-factory";
	
	/**
	 * @see CompleteConfiguration#getExpiryPolicyFactory()
	 */
	public static final String EXPIRY_POLICY_FACTORY               = "expiry-policy-factory";
	
	/**
	 * @see CacheConfiguration#getEvictionManagerFactory()
	 */
	public static final String EVICTION_MANAGER_FACTORY            = "eviction-manager-factory";
	
	/**
	 * @see CompleteConfiguration#isReadThrough()
	 */
	public static final String READ_THROUGH                        = "read-through";
	
	/**
	 * @see CompleteConfiguration#isWriteThrough()
	 */
	public static final String WRITE_THROUGH                       = "write-through";
	
	/**
	 * @see CompleteConfiguration#isStoreByValue()
	 */
	public static final String STORE_BY_VALUE                      = "store-by-value";
	
	/**
	 * @see CompleteConfiguration#isStatisticsEnabled()
	 */
	public static final String STATISTICS_ENABLED                  = "statistics-enabled";
	
	/**
	 * @see CompleteConfiguration#isManagementEnabled()
	 */
	public static final String MANAGEMENT_ENABLED                  = "management-enabled";
	
}
