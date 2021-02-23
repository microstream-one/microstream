
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
	public final static String STORAGE_DIRECTORY             = "storage-directory";
	
	public final static String STORAGE_FILESYSTEM            = "storage-filesystem";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDeletionDirectory(String)
	 */
	public final static String DELETION_DIRECTORY            = "deletion-directory";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setTruncationDirectory(String)
	 */
	public final static String TRUNCATION_DIRECTORY          = "truncation-directory";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setBackupDirectory(String)
	 */
	public final static String BACKUP_DIRECTORY              = "backup-directory";

	public final static String BACKUP_FILESYSTEM             = "backup-filesystem";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setChannelCount(int)
	 */
	public final static String CHANNEL_COUNT                 = "channel-count";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setChannelDirectoryPrefix(String)
	 */
	public final static String CHANNEL_DIRECTORY_PREFIX      = "channel-directory-prefix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFilePrefix(String)
	 */
	public final static String DATA_FILE_PREFIX              = "data-file-prefix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileSuffix(String)
	 */
	public final static String DATA_FILE_SUFFIX              = "data-file-suffix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setTransactionFilePrefix(String)
	 */
	public final static String TRANSACTION_FILE_PREFIX       = "transaction-file-prefix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setTransactionFileSuffix(String)
	 */
	public final static String TRANSACTION_FILE_SUFFIX       = "transaction-file-suffix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setTypeDictionaryFilename(String)
	 */
	public final static String TYPE_DICTIONARY_FILENAME      = "type-dictionary-filename";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setRescuedFileSuffix(String)
	 */
	public final static String RESCUED_FILE_SUFFIX           = "rescued-file-suffix";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setLockFileName(String)
	 */
	public final static String LOCK_FILE_NAME                = "lock-file-name";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setHousekeepingIntervalMs(long)
	 */
	public final static String HOUSEKEEPING_INTERVAL_MS      = "housekeeping-interval-ms";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setHousekeepingTimeBudgetNs(long)
	 */
	public final static String HOUSEKEEPING_TIME_BUDGET_NS   = "housekeeping-time-budget-ns";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setEntityCacheThreshold(long)
	 */
	public final static String ENTITY_CACHE_THRESHOLD        = "entity-cache-threshold";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setEntityCacheTimeout(long)
	 */
	public final static String ENTITY_CACHE_TIMEOUT_MS       = "entity-cache-timeout-ms";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileMinimumSize(int)
	 */
	public final static String DATA_FILE_MINIMUM_SIZE        = "data-file-minimum-size";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileMaximumSize(int)
	 */
	public final static String DATA_FILE_MAXIMUM_SIZE        = "data-file-maximum-size";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileDissolveRatio(double)
	 */
	public final static String DATA_FILE_MINIMUM_USE_RATIO   = "data-file-minimum-use-ratio";

	/**
	 * @see EmbeddedStorageConfigurationBuilder#setDataFileCleanupHeadFile(boolean)
	 */
	public final static String DATA_FILE_CLEANUP_HEAD_FILE   = "data-file-cleanup-head-file";

}
