
package one.microstream.storage.configuration;

import static one.microstream.chars.XChars.notEmpty;
import static one.microstream.math.XMath.positive;
import static one.microstream.math.XMath.positiveMax1;

import java.io.File;
import java.time.Duration;

import one.microstream.math.XMath;
import one.microstream.persistence.internal.FileObjectIdStrategy;
import one.microstream.persistence.internal.FileTypeIdStrategy;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageChannelCountProvider;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.storage.types.StorageHousekeepingController;


public interface Configuration
{
	public default EmbeddedStorageFoundation<?> createEmbeddedStorageFoundation()
	{
		return this.updateEmbeddedStorageFoundation(EmbeddedStorage.Foundation());
	}
	
	public default EmbeddedStorageFoundation<?> updateEmbeddedStorageFoundation(final EmbeddedStorageFoundation<?> foundation)
	{
		ConfigurationConsumer.FoundationUpdater(foundation).accept(this);
		return foundation;
	}
	
	/**
	 * The base directory of the storage in the file system.
	 */
	public Configuration setBaseDirectory(final String baseDirectory);
	
	/**
	 * The base directory of the storage in the file system.
	 *
	 * @param baseDirectoryInUserHome
	 *            relative location in the user home directory
	 */
	public default Configuration setBaseDirectoryInUserHome(final String baseDirectoryInUserHome)
	{
		this.setBaseDirectory(
			new File(new File(System.getProperty("user.home")), baseDirectoryInUserHome).getAbsolutePath());
		return this;
	}
	
	/**
	 * The base directory of the storage in the file system.
	 */
	public String getBaseDirectory();
	
	/**
	 * The deletion directory.
	 */
	public Configuration setDeletionDirectory(final String deletionDirectory);
	
	/**
	 * The deletion directory.
	 */
	public String getDeletionDirectory();
	
	/**
	 * The truncation directory.
	 */
	public Configuration setTruncationDirectory(final String truncationDirectory);
	
	/**
	 * The truncation directory.
	 */
	public String getTruncationDirectory();
	
	/**
	 * The backup directory.
	 */
	public Configuration setBackupDirectory(final String backupDirectory);
	
	/**
	 * The backup directory.
	 */
	public String getBackupDirectory();
	
	/**
	 * The number of threads and number of directories used by the storage
	 * engine. Every thread has exclusive access to its directory. Default is
	 * <code>1</code>.
	 *
	 * @param channelCount
	 *            the new channel count, must be a power of 2
	 */
	public Configuration setChannelCount(int channelCount);
	
	/**
	 * The number of threads and number of directories used by the storage
	 * engine. Every thread has exclusive access to its directory.
	 */
	public int getChannelCount();
	
	/**
	 * Name prefix of the subdirectories used by the channel threads. Default is
	 * <code>"channel_"</code>.
	 *
	 * @param channelDirectoryPrefix
	 *            new prefix
	 */
	public Configuration setChannelDirectoryPrefix(String channelDirectoryPrefix);
	
	/**
	 * Name prefix of the subdirectories used by the channel threads.
	 */
	public String getChannelDirectoryPrefix();
	
	/**
	 * Name prefix of the storage files. Default is <code>"channel_"</code>.
	 *
	 * @param dataFilePrefix
	 *            new prefix
	 */
	public Configuration setDataFilePrefix(String dataFilePrefix);
	
	/**
	 * Name prefix of the storage files.
	 */
	public String getDataFilePrefix();
	
	/**
	 * Name suffix of the storage files. Default is <code>".dat"</code>.
	 *
	 * @param dataFileSuffix
	 *            new suffix
	 */
	public Configuration setDataFileSuffix(String dataFileSuffix);
	
	/**
	 * Name suffix of the storage files.
	 */
	public String getDataFileSuffix();
	
	/**
	 * Name prefix of the storage transaction file. Default is <code>"transactions_"</code>.
	 *
	 * @param transactionFilePrefix
	 *            new prefix
	 */
	public Configuration setTransactionFilePrefix(String transactionFilePrefix);
	
	/**
	 * Name prefix of the storage transaction file.
	 */
	public String getTransactionFilePrefix();
	
	/**
	 * Name suffix of the storage transaction file. Default is <code>".sft"</code>.
	 *
	 * @param storageFileSuffix
	 *            new suffix
	 */
	public Configuration setTransactionFileSuffix(String transactionFileSuffix);
	
	/**
	 * Name suffix of the storage transaction file.
	 */
	public String getTransactionFileSuffix();
	
	/**
	 * The name of the dictionary file. Default is
	 * <code>"PersistenceTypeDictionary.ptd"</code>.
	 *
	 * @param typeDictionaryFilename
	 *            new name
	 */
	public Configuration setTypeDictionaryFilename(String typeDictionaryFilename);
	
	/**
	 * The name of the dictionary file.
	 */
	public String getTypeDictionaryFilename();
	
	/**
	 * The name of the type id file. Default is <code>"TypeId.tid"</code>.
	 *
	 * @param filenameTypeId
	 *            new file name
	 */
	public Configuration setTypeIdFilename(String filenameTypeId);
	
	/**
	 * The name of the type id file.
	 */
	public String getTypeIdFilename();
	
	/**
	 * The name of the object id file. Default is <code>"ObjectId.oid"</code>.
	 *
	 * @param filenameObjectId
	 *            new file name
	 */
	public Configuration setObjectIdFilename(String filenameObjectId);
	
	/**
	 * The name of the object id file.
	 */
	public String getObjectIdFilename();
	
	/**
	 * Interval in milliseconds for the houskeeping. This is work like garbage
	 * collection or cache checking. In combination with
	 * {@link #setHouseKeepingNanoTimeBudget(long)} the maximum processor
	 * time for housekeeping work can be set. Default is <code>1000</code>
	 * (every second).
	 *
	 * @param houseKeepingInterval
	 *            the new interval
	 *
	 * @see #setHouseKeepingNanoTimeBudget(long)
	 */
	public Configuration setHouseKeepingInterval(long houseKeepingInterval);
	
	/**
	 * Interval in milliseconds for the houskeeping. This is work like garbage
	 * collection or cache checking.
	 *
	 * @see #getHouseKeepingNanoTimeBudget()
	 */
	public long getHouseKeepingInterval();
	
	/**
	 * Number of nanoseconds used for each housekeeping cycle. However, no
	 * matter how low the number is, one item of work will always be completed.
	 * But if there is nothing to clean up, no processor time will be wasted.
	 * Default is <code>10000000</code> (10 million nanoseconds = 10
	 * milliseconds = 0.01 seconds).
	 *
	 * @param houseKeepingNanoTimeBudget
	 *            the new time budget
	 *
	 * @see #setHouseKeepingInterval(long)
	 */
	public Configuration setHouseKeepingNanoTimeBudget(long houseKeepingNanoTimeBudget);
	
	/**
	 * Number of nanoseconds used for each housekeeping cycle. However, no
	 * matter how low the number is, one item of work will always be completed.
	 * But if there is nothing to clean up, no processor time will be wasted.
	 *
	 * @see #getHouseKeepingInterval()
	 */
	public long getHouseKeepingNanoTimeBudget();
	
	/**
	 * Abstract threshold value for the lifetime of entities in the cache. See
	 * {@link StorageEntityCacheEvaluator}. Default is <code>1000000000</code>.
	 *
	 * @param entityCacheThreshold
	 *            the new threshold
	 */
	public Configuration setEntityCacheThreshold(long entityCacheThreshold);
	
	/**
	 * Abstract threshold value for the lifetime of entities in the cache. See
	 * {@link StorageEntityCacheEvaluator}.
	 */
	public long getEntityCacheThreshold();
	
	/**
	 * Timeout in milliseconds for the entity cache evaluator. If an entity
	 * wasn't accessed in this timespan it will be removed from the cache.
	 * Default is <code>86400000</code> (1 day).
	 *
	 * @param entityCacheTimeout
	 *
	 * @see Duration
	 */
	public Configuration setEntityCacheTimeout(long entityCacheTimeout);
	
	/**
	 * Timeout in milliseconds for the entity cache evaluator. If an entity
	 * wasn't accessed in this timespan it will be removed from the cache.
	 */
	public long getEntityCacheTimeout();
	
	/**
	 * Minimum file size for a data file to avoid cleaning it up. Default is
	 * 1024^2 = 1 MiB.
	 *
	 * @param dataFileMinSize
	 *            the new minimum file size
	 *
	 * @see #setDataFileDissolveRatio(double)
	 */
	public Configuration setDataFileMinSize(int dataFileMinSize);
	
	/**
	 * Minimum file size for a data file to avoid cleaning it up.
	 *
	 * @see #getDataFileDissolveRatio()
	 */
	public int getDataFileMinSize();
	
	/**
	 * Maximum file size for a data file to avoid cleaning it up. Default is
	 * 1024^2*8 = 8 MiB.
	 *
	 * @param dataFileMaxSize
	 *            the new maximum file size
	 *
	 * @see #setDataFileDissolveRatio(double)
	 */
	public Configuration setDataFileMaxSize(int dataFileMaxSize);
	
	/**
	 * Maximum file size for a data file to avoid cleaning it up.
	 *
	 * @see #getDataFileDissolveRatio()
	 */
	public int getDataFileMaxSize();
	
	/**
	 * The degree of the data payload of a storage file to avoid cleaning it up.
	 * The storage engine only appends newly written records at the
	 * end of a channel. So no data will be overwritten but becomes obsolete
	 * (gaps). If all data file evaluator settings apply to a storage file it
	 * will be cleaned up. Meaning all valid records will be preserved by moving
	 * them to the end of the channel, then the file will be deleted.<br>
	 * Dissolve Ratio 1: If the first gap appears, the file will be cleaned
	 * up<br>
	 * Dissolve Ratio 0: Gaps don't matter, never clean up<br>
	 * Default is <code>0.75</code> (0.75%).
	 *
	 * @param dataFileDissolveRatio
	 *            the new dissolve ration
	 */
	public Configuration setDataFileDissolveRatio(double dataFileDissolveRatio);
	
	/**
	 * The degree of the data payload of a storage file to avoid cleaning it up.
	 * The storage engine only appends newly written records at the
	 * end of a channel. So no data will be overwritten but becomes obsolete
	 * (gaps). If all data file evaluator settings apply to a storage file it
	 * will be cleaned up. Meaning all valid records will be preserved by moving
	 * them to the end of the channel, then the file will be deleted.<br>
	 * Dissolve Ratio 1: If the first gap appears, the file will be cleaned
	 * up<br>
	 * Dissolve Ratio 0: Gaps don't matter, never clean up
	 */
	public double getDataFileDissolveRatio();
	
	public static Configuration Default()
	{
		return new Default();
	}
	
	public static class Default implements Configuration
	{
		private String baseDirectory            = StorageFileProvider.Defaults.defaultStorageDirectory();
		private String deletionDirectory        = StorageFileProvider.Defaults.defaultDeletionDirectory();
		private String truncationDirectory      = StorageFileProvider.Defaults.defaultTruncationDirectory();
		private String backupDirectory          = null; // no on-the-fly backup by default
		private String channelDirectoryPrefix   = StorageFileProvider.Defaults.defaultChannelDirectoryPrefix();
		private String dataFilePrefix           = StorageFileProvider.Defaults.defaultStorageFilePrefix();
		private String dataFileSuffix           = StorageFileProvider.Defaults.defaultStorageFileSuffix();
		private String transactionFilePrefix    = StorageFileProvider.Defaults.defaultTransactionFilePrefix();
		private String transactionFileSuffix    = StorageFileProvider.Defaults.defaultTransactionFileSuffix();
		private String typeDictionaryFilename   = StorageFileProvider.Defaults.defaultTypeDictionaryFileName();
		private int    channelCount             = StorageChannelCountProvider.Defaults.defaultChannelCount();
		private String typeIdFilename           = FileTypeIdStrategy.defaultFilename();
		private String objectIdFilename         = FileObjectIdStrategy.defaultFilename();
		private long   houseKeepingIntervalMs   = StorageHousekeepingController.Defaults.defaultHousekeepingIntervalMs();
		private long   houseKeepingTimeBudgetNs = StorageHousekeepingController.Defaults.defaultHousekeepingTimeBudgetNs();
		private long   entityCacheTimeout       = StorageEntityCacheEvaluator.Defaults.defaultTimeoutMs();
		private long   entityCacheThreshold     = StorageEntityCacheEvaluator.Defaults.defaultCacheThreshold();
		private int    dataFileMinSize          = StorageDataFileEvaluator.Defaults.defaultFileMinimumSize();
		private int    dataFileMaxSize          = StorageDataFileEvaluator.Defaults.defaultFileMaximumSize();
		private double dataFileDissolveRatio    = StorageDataFileEvaluator.Defaults.defaultMinimumUseRatio();
		
		protected Default()
		{
			super();
		}
		
		@Override
		public Configuration setBaseDirectory(final String baseDirectory)
		{
			this.baseDirectory = notEmpty(baseDirectory);
			return this;
		}
		
		@Override
		public String getBaseDirectory()
		{
			return this.baseDirectory;
		}
		
		@Override
		public Configuration setDeletionDirectory(final String deletionDirectory)
		{
			this.deletionDirectory = deletionDirectory;
			return this;
		}
		
		@Override
		public String getDeletionDirectory()
		{
			return this.deletionDirectory;
		}
		
		@Override
		public Configuration setTruncationDirectory(final String truncationDirectory)
		{
			this.truncationDirectory = truncationDirectory;
			return this;
		}
		
		@Override
		public String getTruncationDirectory()
		{
			return this.truncationDirectory;
		}
		
		@Override
		public Configuration setBackupDirectory(final String backupDirectory)
		{
			this.backupDirectory = backupDirectory;
			return this;
		}
		
		@Override
		public String getBackupDirectory()
		{
			return this.backupDirectory;
		}
		
		@Override
		public Configuration setChannelCount(final int channelCount)
		{
			XMath.log2pow2(channelCount);
			this.channelCount = channelCount;
			return this;
		}
		
		@Override
		public int getChannelCount()
		{
			return this.channelCount;
		}
		
		@Override
		public Configuration setChannelDirectoryPrefix(final String channelDirectoryPrefix)
		{
			this.channelDirectoryPrefix = notEmpty(channelDirectoryPrefix);
			return this;
		}
		
		@Override
		public String getChannelDirectoryPrefix()
		{
			return this.channelDirectoryPrefix;
		}
		
		@Override
		public Configuration setDataFilePrefix(final String dataFilePrefix)
		{
			this.dataFilePrefix = notEmpty(dataFilePrefix);
			return this;
		}
		
		@Override
		public String getDataFilePrefix()
		{
			return this.dataFilePrefix;
		}
		
		@Override
		public Configuration setDataFileSuffix(final String dataFileSuffix)
		{
			this.dataFileSuffix = notEmpty(dataFileSuffix);
			return this;
		}
		
		@Override
		public String getDataFileSuffix()
		{
			return this.dataFileSuffix;
		}
		
		@Override
		public Configuration setTransactionFilePrefix(final String transactionFilePrefix)
		{
			this.transactionFilePrefix = transactionFilePrefix;
			return this;
		}
		
		@Override
		public String getTransactionFilePrefix()
		{
			return this.transactionFilePrefix;
		}
		
		@Override
		public Configuration setTransactionFileSuffix(final String transactionFileSuffix)
		{
			this.transactionFileSuffix = transactionFileSuffix;
			return this;
		}
		
		@Override
		public String getTransactionFileSuffix()
		{
			return this.transactionFileSuffix;
		}
		
		@Override
		public Configuration setTypeDictionaryFilename(final String typeDictionaryFilename)
		{
			this.typeDictionaryFilename = notEmpty(typeDictionaryFilename);
			return this;
		}
		
		@Override
		public String getTypeDictionaryFilename()
		{
			return this.typeDictionaryFilename;
		}
		
		@Override
		public Configuration setTypeIdFilename(final String typeIdFilename)
		{
			this.typeIdFilename = notEmpty(typeIdFilename);
			return this;
		}
		
		@Override
		public String getTypeIdFilename()
		{
			return this.typeIdFilename;
		}
		
		@Override
		public Configuration setObjectIdFilename(final String objectIdFilename)
		{
			this.objectIdFilename = notEmpty(objectIdFilename);
			return this;
		}
		
		@Override
		public String getObjectIdFilename()
		{
			return this.objectIdFilename;
		}
		
		@Override
		public Configuration setHouseKeepingInterval(final long houseKeepingInterval)
		{
			this.houseKeepingIntervalMs = positive(houseKeepingInterval);
			return this;
		}
		
		@Override
		public long getHouseKeepingInterval()
		{
			return this.houseKeepingIntervalMs;
		}
		
		@Override
		public Configuration setHouseKeepingNanoTimeBudget(final long houseKeepingNanoTimeBudget)
		{
			this.houseKeepingTimeBudgetNs = positive(houseKeepingNanoTimeBudget);
			return this;
		}
		
		@Override
		public long getHouseKeepingNanoTimeBudget()
		{
			return this.houseKeepingTimeBudgetNs;
		}
		
		@Override
		public Configuration setEntityCacheThreshold(final long entityCacheThreshold)
		{
			this.entityCacheThreshold = positive(entityCacheThreshold);
			return this;
		}
		
		@Override
		public long getEntityCacheThreshold()
		{
			return this.entityCacheThreshold;
		}
		
		@Override
		public Configuration setEntityCacheTimeout(final long entityCacheTimeout)
		{
			this.entityCacheTimeout = positive(entityCacheTimeout);
			return this;
		}
		
		@Override
		public long getEntityCacheTimeout()
		{
			return this.entityCacheTimeout;
		}
		
		@Override
		public Configuration setDataFileMinSize(final int dataFileMinSize)
		{
			this.dataFileMinSize = positive(dataFileMinSize);
			return this;
		}
		
		@Override
		public int getDataFileMinSize()
		{
			return this.dataFileMinSize;
		}
		
		@Override
		public Configuration setDataFileMaxSize(final int dataFileMaxSize)
		{
			this.dataFileMaxSize = positive(dataFileMaxSize);
			return this;
		}
		
		@Override
		public int getDataFileMaxSize()
		{
			return this.dataFileMaxSize;
		}
		
		@Override
		public Configuration setDataFileDissolveRatio(final double dataFileDissolveRatio)
		{
			this.dataFileDissolveRatio = positiveMax1(dataFileDissolveRatio);
			return this;
		}
		
		@Override
		public double getDataFileDissolveRatio()
		{
			return this.dataFileDissolveRatio;
		}
	}
}
