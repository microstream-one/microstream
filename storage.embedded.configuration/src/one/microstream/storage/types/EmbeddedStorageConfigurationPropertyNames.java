
package one.microstream.storage.types;

/**
 * All supported properties for external configuration files.
 *
 * @since 04.02.00
 */
public interface EmbeddedStorageConfigurationPropertyNames
{
	/**
	 * @see EmbeddedStorageConfigurationBuilder#setStorageDirectory(String)
	 */
	public final static String STORAGE_DIRECTORY             = "storageDirectory";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDeletionDirectory(String)
	 */
	public final static String DELETION_DIRECTORY            = "deletionDirectory";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setTruncationDirectory(String)
	 */
	public final static String TRUNCATION_DIRECTORY          = "truncationDirectory";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setBackupDirectory(String)
	 */
	public final static String BACKUP_DIRECTORY              = "backupDirectory";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setChannelCount(int)
	 */
	public final static String CHANNEL_COUNT                 = "channelCount";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setChannelDirectoryPrefix(String)
	 */
	public final static String CHANNEL_DIRECTORY_PREFIX      = "channelDirectoryPrefix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFilePrefix(String)
	 */
	public final static String DATA_FILE_PREFIX              = "dataFilePrefix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileSuffix(String)
	 */
	public final static String DATA_FILE_SUFFIX              = "dataFileSuffix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setTransactionFilePrefix(String)
	 */
	public final static String TRANSACTION_FILE_PREFIX       = "transactionFilePrefix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setTransactionFileSuffix(String)
	 */
	public final static String TRANSACTION_FILE_SUFFIX       = "transactionFileSuffix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setTypeDictionaryFilename(String)
	 */
	public final static String TYPE_DICTIONARY_FILENAME      = "typeDictionaryFilename";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setRescuedFileSuffix(String)
	 */
	public final static String RESCUED_FILE_SUFFIX           = "rescuedFileSuffix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setLockFileName(String)
	 */
	public final static String LOCK_FILE_NAME                = "lockFileName";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setHousekeepingIntervalMs(long)
	 */
	public final static String HOUSEKEEPING_INTERVAL_MS      = "housekeepingIntervalMs";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setHousekeepingTimeBudgetNs(long)
	 */
	public final static String HOUSEKEEPING_TIME_BUDGET_NS   = "housekeepingTimeBudgetNs";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setEntityCacheThreshold(long)
	 */
	public final static String ENTITY_CACHE_THRESHOLD        = "entityCacheThreshold";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setEntityCacheTimeout(long)
	 */
	public final static String ENTITY_CACHE_TIMEOUT_MS       = "entityCacheTimeoutMs";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileMinimumSize(int)
	 */
	public final static String DATA_FILE_MINIMUM_SIZE        = "dataFileMinimumSize";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileMaximumSize(int)
	 */
	public final static String DATA_FILE_MAXIMUM_SIZE        = "dataFileMaximumSize";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileDissolveRatio(double)
	 */
	public final static String DATA_FILE_MINIMUM_USE_RATIO   = "dataFileMinimumUseRatio";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileCleanupHeadFile(boolean)
	 */
	public final static String DATA_FILE_CLEANUP_HEAD_FILE   = "dataFileCleanupHeadFile";

}
