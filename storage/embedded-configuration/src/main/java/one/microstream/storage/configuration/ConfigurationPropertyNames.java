
package one.microstream.storage.configuration;

/*-
 * #%L
 * microstream-storage-embedded-configuration
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

import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationPropertyNames;

/**
 * 
 * @deprecated replaced by {@link EmbeddedStorageConfigurationPropertyNames}, will be removed in version 8
 * @see one.microstream.storage.configuration
 */
@Deprecated
public interface ConfigurationPropertyNames
{
	/**
	 * @see Configuration#setBaseDirectory(String)
	 */
	public final static String BASE_DIRECTORY                = "baseDirectory";

	/**
	 * @see Configuration#setDeletionDirectory(String)
	 */
	public final static String DELETION_DIRECTORY            = "deletionDirectory";

	/**
	 * @see Configuration#setTruncationDirectory(String)
	 */
	public final static String TRUNCATION_DIRECTORY          = "truncationDirectory";

	/**
	 * @see Configuration#setBackupDirectory(String)
	 */
	public final static String BACKUP_DIRECTORY              = "backupDirectory";

	/**
	 * @see Configuration#setChannelCount(int)
	 */
	public final static String CHANNEL_COUNT                 = "channelCount";

	/**
	 * @see Configuration#setChannelDirectoryPrefix(String)
	 */
	public final static String CHANNEL_DIRECTORY_PREFIX      = "channelDirectoryPrefix";

	/**
	 * @see Configuration#setDataFilePrefix(String)
	 */
	public final static String DATA_FILE_PREFIX              = "dataFilePrefix";

	/**
	 * @see Configuration#setDataFileSuffix(String)
	 */
	public final static String DATA_FILE_SUFFIX              = "dataFileSuffix";

	/**
	 * @see Configuration#setTransactionFilePrefix(String)
	 */
	public final static String TRANSACTION_FILE_PREFIX       = "transactionFilePrefix";

	/**
	 * @see Configuration#setTransactionFileSuffix(String)
	 */
	public final static String TRANSACTION_FILE_SUFFIX       = "transactionFileSuffix";

	/**
	 * @see Configuration#setTypeDictionaryFilename(String)
	 */
	public final static String TYPE_DICTIONARY_FILENAME      = "typeDictionaryFilename";

	/**
	 * @see Configuration#setRescuedFileSuffix(String)
	 */
	public final static String RESCUED_FILE_SUFFIX           = "rescuedFileSuffix";

	/**
	 * @see Configuration#setLockFileName(String)
	 */
	public final static String LOCK_FILE_NAME                = "lockFileName";

	/**
	 * @deprecated replaced by {@link #HOUSEKEEPING_INTERVAL_MS}, will be removed in version 8
	 */
	@Deprecated
	public final static String HOUSEKEEPING_INTERVAL         = "houseKeepingInterval";

	/**
	 * @see Configuration#setHousekeepingIntervalMs(long)
	 */
	public final static String HOUSEKEEPING_INTERVAL_MS      = "housekeepingIntervalMs";

	/**
	 * @deprecated replaced by {@link #HOUSEKEEPING_TIME_BUDGET_NS}, will be removed in version 8
	 */
	@Deprecated
	public final static String HOUSEKEEPING_NANO_TIME_BUDGET = "houseKeepingNanoTimeBudget";

	/**
	 * @see Configuration#setHousekeepingTimeBudgetNs(long)
	 */
	public final static String HOUSEKEEPING_TIME_BUDGET_NS   = "housekeepingTimeBudgetNs";

	/**
	 * @see Configuration#setEntityCacheThreshold(long)
	 */
	public final static String ENTITY_CACHE_THRESHOLD        = "entityCacheThreshold";

	/**
	 * @deprecated replaced by {@link #ENTITY_CACHE_TIMEOUT_MS}, will be removed in version 8
	 */
	@Deprecated
	public final static String ENTITY_CACHE_TIMEOUT          = "entityCacheTimeout";

	/**
	 * @see Configuration#setEntityCacheTimeout(long)
	 */
	public final static String ENTITY_CACHE_TIMEOUT_MS       = "entityCacheTimeoutMs";

	/**
	 * @deprecated replaced by {@link #DATA_FILE_MINIMUM_SIZE}, will be removed in version 8
	 */
	@Deprecated
	public final static String DATA_FILE_MIN_SIZE            = "dataFileMinSize";

	/**
	 * @see Configuration#setDataFileMinimumSize(int)
	 */
	public final static String DATA_FILE_MINIMUM_SIZE        = "dataFileMinimumSize";

	/**
	 * @deprecated replaced by {@link #DATA_FILE_MAXIMUM_SIZE}, will be removed in version 8
	 */
	@Deprecated
	public final static String DATA_FILE_MAX_SIZE            = "dataFileMaxSize";

	/**
	 * @see Configuration#setDataFileMaximumSize(int)
	 */
	public final static String DATA_FILE_MAXIMUM_SIZE        = "dataFileMaximumSize";

	/**
	 * @deprecated replaced by {@link #DATA_FILE_MINIMUM_USE_RATIO}, will be removed in version 8
	 */
	@Deprecated
	public final static String DATA_FILE_DISSOLVE_RATIO      = "dataFileDissolveRatio";

	/**
	 * @see Configuration#setDataFileDissolveRatio(double)
	 */
	public final static String DATA_FILE_MINIMUM_USE_RATIO   = "dataFileMinimumUseRatio";

	/**
	 * @see Configuration#setDataFileCleanupHeadFile(boolean)
	 */
	public final static String DATA_FILE_CLEANUP_HEAD_FILE   = "dataFileCleanupHeadFile";

}
