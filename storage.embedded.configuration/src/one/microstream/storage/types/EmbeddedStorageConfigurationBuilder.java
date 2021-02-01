package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;
import java.time.Duration;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationValueMapperProvider;
import one.microstream.typing.KeyValue;

public interface EmbeddedStorageConfigurationBuilder extends Configuration.Builder
{
	/**
	 * The base directory of the storage in the file system.
	 */
	public EmbeddedStorageConfigurationBuilder setStorageDirectory(String storageDirectory);
	
	/**
	 * The base directory of the storage in the file system.
	 *
	 * @param storageDirectoryInUserHome
	 *            relative location in the user home directory
	 */
	public default EmbeddedStorageConfigurationBuilder setStorageDirectoryInUserHome(final String storageDirectoryInUserHome)
	{
		final File userHomeDir = new File(System.getProperty("user.home"));
		this.setStorageDirectory(new File(userHomeDir, storageDirectoryInUserHome).getAbsolutePath());
		return this;
	}
	
	/**
	 * The deletion directory.
	 */
	public EmbeddedStorageConfigurationBuilder setDeletionDirectory(String deletionDirectory);
	
	/**
	 * The truncation directory.
	 */
	public EmbeddedStorageConfigurationBuilder setTruncationDirectory(String truncationDirectory);
	
	/**
	 * The backup directory.
	 */
	public EmbeddedStorageConfigurationBuilder setBackupDirectory(String backupDirectory);

	/**
	 * The backup directory.
	 *
	 * @param backupDirectoryInUserHome
	 *            relative location in the user home directory
	 */
	public default EmbeddedStorageConfigurationBuilder setBackupDirectoryInUserHome(final String backupDirectoryInUserHome)
	{
		final File userHomeDir = new File(System.getProperty("user.home"));
		this.setBackupDirectory(new File(userHomeDir, backupDirectoryInUserHome).getAbsolutePath());
		return this;
	}

	/**
	 * The number of threads and number of directories used by the storage
	 * engine. Every thread has exclusive access to its directory. Default is
	 * <code>1</code>.
	 *
	 * @param channelCount
	 *            the new channel count, must be a power of 2
	 */
	public EmbeddedStorageConfigurationBuilder setChannelCount(int channelCount);

	/**
	 * Name prefix of the subdirectories used by the channel threads. Default is
	 * <code>"channel_"</code>.
	 *
	 * @param channelDirectoryPrefix
	 *            new prefix
	 */
	public EmbeddedStorageConfigurationBuilder setChannelDirectoryPrefix(String channelDirectoryPrefix);

	/**
	 * Name prefix of the storage files. Default is <code>"channel_"</code>.
	 *
	 * @param dataFilePrefix
	 *            new prefix
	 */
	public EmbeddedStorageConfigurationBuilder setDataFilePrefix(String dataFilePrefix);

	/**
	 * Name suffix of the storage files. Default is <code>".dat"</code>.
	 *
	 * @param dataFileSuffix
	 *            new suffix
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileSuffix(String dataFileSuffix);

	/**
	 * Name prefix of the storage transaction file. Default is <code>"transactions_"</code>.
	 *
	 * @param transactionFilePrefix
	 *            new prefix
	 */
	public EmbeddedStorageConfigurationBuilder setTransactionFilePrefix(String transactionFilePrefix);

	/**
	 * Name suffix of the storage transaction file. Default is <code>".sft"</code>.
	 *
	 * @param transactionFileSuffix
	 *            new suffix
	 */
	public EmbeddedStorageConfigurationBuilder setTransactionFileSuffix(String transactionFileSuffix);

	/**
	 * The name of the dictionary file. Default is
	 * <code>"PersistenceTypeDictionary.ptd"</code>.
	 *
	 * @param typeDictionaryFilename
	 *            new name
	 */
	public EmbeddedStorageConfigurationBuilder setTypeDictionaryFilename(String typeDictionaryFilename);

	public EmbeddedStorageConfigurationBuilder setRescuedFileSuffix(String rescuedFileSuffix);

	public EmbeddedStorageConfigurationBuilder setLockFileName(String lockFileName);

	/**
	 * Interval in milliseconds for the houskeeping. This is work like garbage
	 * collection or cache checking. In combination with
	 * {@link #setHousekeepingTimeBudgetNs(long)} the maximum processor
	 * time for housekeeping work can be set. Default is <code>1000</code>
	 * (every second).
	 *
	 * @param houseKeepingIntervalMs
	 *            the new interval
	 *
	 * @see #setHousekeepingTimeBudgetNs(long)
	 */
	public EmbeddedStorageConfigurationBuilder setHousekeepingIntervalMs(long houseKeepingIntervalMs);

	/**
	 * Number of nanoseconds used for each housekeeping cycle. However, no
	 * matter how low the number is, one item of work will always be completed.
	 * But if there is nothing to clean up, no processor time will be wasted.
	 * Default is <code>10000000</code> (10 million nanoseconds = 10
	 * milliseconds = 0.01 seconds).
	 *
	 * @param housekeepingTimeBudgetNs
	 *            the new time budget
	 *
	 * @see #setHousekeepingIntervalMs(long)
	 */
	public EmbeddedStorageConfigurationBuilder setHousekeepingTimeBudgetNs(long housekeepingTimeBudgetNs);

	/**
	 * Abstract threshold value for the lifetime of entities in the cache. See
	 * {@link StorageEntityCacheEvaluator}. Default is <code>1000000000</code>.
	 *
	 * @param entityCacheThreshold
	 *            the new threshold
	 */
	public EmbeddedStorageConfigurationBuilder setEntityCacheThreshold(long entityCacheThreshold);

	/**
	 * Timeout in milliseconds for the entity cache evaluator. If an entity
	 * wasn't accessed in this timespan it will be removed from the cache.
	 * Default is <code>86400000</code> (1 day).
	 *
	 * @param entityCacheTimeoutMs
	 *
	 * @see Duration
	 */
	public EmbeddedStorageConfigurationBuilder setEntityCacheTimeoutMs(long entityCacheTimeoutMs);

	/**
	 * Minimum file size for a data file to avoid cleaning it up. Default is
	 * 1024^2 = 1 MiB.
	 *
	 * @param dataFileMinimumSize
	 *            the new minimum file size
	 *
	 * @see #setDataFileMinimumUseRatio(double)
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileMinimumSize(int dataFileMinimumSize);

	/**
	 * Maximum file size for a data file to avoid cleaning it up. Default is
	 * 1024^2*8 = 8 MiB.
	 *
	 * @param dataFileMaximumSize
	 *            the new maximum file size
	 *
	 * @see #setDataFileMinimumUseRatio(double)
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileMaximumSize(int dataFileMaximumSize);

	/**
	 * The ratio (value in ]0.0;1.0]) of non-gap data contained in a storage file to prevent
	 * the file from being dissolved. "Gap" data is anything that is not the latest version of an entity's data,
	 * inluding older versions of an entity and "comment" bytes (a sequence of bytes beginning with its length
	 * as a negative value length header).<br>
	 * The closer this value is to 1.0 (100%), the less disk space is occupied by storage files, but the more
	 * file dissolving (data transfers to new files) is required and vice versa.
	 *
	 * @param dataFileMinimumUseRatio
	 *            the new minimum use ratio
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileMinimumUseRatio(double dataFileMinimumUseRatio);

	/**
	 * A flag defining wether the current head file (the only file actively written to)
	 * shall be subjected to file cleanups as well.
	 *
	 * @param dataFileCleanupHeadFile
	 */
	public EmbeddedStorageConfigurationBuilder setDataFileCleanupHeadFile(boolean dataFileCleanupHeadFile);
	
	
	public default EmbeddedStorageFoundation<?> createEmbeddedStorageFoundation()
	{
		return EmbeddedStorageFoundationCreatorConfigurationBased.New(
			this.buildConfiguration()
		)
		.createEmbeddedStorageFoundation()
		;
	}
	
	
	
	
	public static EmbeddedStorageConfigurationBuilder New()
	{
		return new Default(Configuration.Builder());
	}
	
	public static EmbeddedStorageConfigurationBuilder New(
		final Configuration.Builder delegate
	)
	{
		return new Default(
			notNull(delegate)
		);
	}
	
	
	public static class Default implements EmbeddedStorageConfigurationBuilder, EmbeddedStorageConfigurationProperties
	{
		private final Configuration.Builder delegate;

		Default(
			final Configuration.Builder delegate
		)
		{
			super();
			this.delegate = delegate;
		}
		
		// ############################
		// Delegate methods
		// ############################
		
		@Override
		public EmbeddedStorageConfigurationBuilder mapperProvider(
			final ConfigurationValueMapperProvider mapperProvider
		)
		{
			this.delegate.mapperProvider(mapperProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConfigurationBuilder set(
			final String key  ,
			final String value
		)
		{
			this.delegate.set(key, value);
			return this;
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setAll(
			final XGettingCollection<KeyValue<String, String>> properties
		)
		{
			this.delegate.setAll(properties);
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public EmbeddedStorageConfigurationBuilder setAll(
			final KeyValue<String, String>... properties
		)
		{
			this.delegate.setAll(properties);
			return this;
		}

		@Override
		public EmbeddedStorageConfigurationBuilder child(
			final String key
		)
		{
			this.delegate.child(key);
			return this;
		}

		@Override
		public Configuration buildConfiguration()
		{
			return this.delegate.buildConfiguration();
		}
		
		// ############################
		// Builder methods
		// ############################

		@Override
		public EmbeddedStorageConfigurationBuilder setStorageDirectory(
			final String storageDirectory
		)
		{
			return this.set(STORAGE_DIRECTORY, storageDirectory);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDeletionDirectory(
			final String deletionDirectory
		)
		{
			return this.set(DELETION_DIRECTORY, deletionDirectory);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setTruncationDirectory(
			final String truncationDirectory
		)
		{
			return this.set(TRUNCATION_DIRECTORY, truncationDirectory);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setBackupDirectory(
			final String backupDirectory
		)
		{
			return this.set(BACKUP_DIRECTORY, backupDirectory);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setChannelCount(
			final int channelCount
		)
		{
			return this.set(CHANNEL_COUNT, Integer.toString(channelCount));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setChannelDirectoryPrefix(
			final String channelDirectoryPrefix
		)
		{
			return this.set(CHANNEL_DIRECTORY_PREFIX, channelDirectoryPrefix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFilePrefix(
			final String dataFilePrefix
		)
		{
			return this.set(DATA_FILE_PREFIX, dataFilePrefix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileSuffix(
			final String dataFileSuffix
		)
		{
			return this.set(DATA_FILE_SUFFIX, dataFileSuffix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setTransactionFilePrefix(
			final String transactionFilePrefix
		)
		{
			return this.set(TRANSACTION_FILE_PREFIX, transactionFilePrefix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setTransactionFileSuffix(
			final String transactionFileSuffix
		)
		{
			return this.set(TRANSACTION_FILE_SUFFIX, transactionFileSuffix);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setTypeDictionaryFilename(
			final String typeDictionaryFilename
		)
		{
			return this.set(TYPE_DICTIONARY_FILENAME, typeDictionaryFilename);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setRescuedFileSuffix(
			final String rescuedFileSuffix
		)
		{
			this.set(RESCUED_FILE_SUFFIX, rescuedFileSuffix);
			return this;
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setLockFileName(
			final String lockFileName
		)
		{
			return this.set(LOCK_FILE_NAME, lockFileName);
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setHousekeepingIntervalMs(
			final long houseKeepingIntervalMs
		)
		{
			return this.set(HOUSEKEEPING_INTERVAL_MS, Long.toString(houseKeepingIntervalMs));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setHousekeepingTimeBudgetNs(
			final long housekeepingTimeBudgetNs
		)
		{
			return this.set(HOUSEKEEPING_TIME_BUDGET_NS, Long.toString(housekeepingTimeBudgetNs));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setEntityCacheThreshold(
			final long entityCacheThreshold
		)
		{
			return this.set(ENTITY_CACHE_THRESHOLD, Long.toString(entityCacheThreshold));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setEntityCacheTimeoutMs(
			final long entityCacheTimeoutMs
		)
		{
			return this.set(ENTITY_CACHE_TIMEOUT_MS, Long.toString(entityCacheTimeoutMs));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileMinimumSize(
			final int dataFileMinimumSize
		)
		{
			return this.set(DATA_FILE_MINIMUM_SIZE, Integer.toString(dataFileMinimumSize));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileMaximumSize(
			final int dataFileMaximumSize
		)
		{
			return this.set(DATA_FILE_MAXIMUM_SIZE, Integer.toString(dataFileMaximumSize));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileMinimumUseRatio(
			final double dataFileMinimumUseRatio
		)
		{
			return this.set(DATA_FILE_MINIMUM_USE_RATIO, Double.toString(dataFileMinimumUseRatio));
		}

		@Override
		public EmbeddedStorageConfigurationBuilder setDataFileCleanupHeadFile(
			final boolean dataFileCleanupHeadFile
		)
		{
			return this.set(DATA_FILE_CLEANUP_HEAD_FILE, Boolean.toString(dataFileCleanupHeadFile));
		}
		
	}
	
}
