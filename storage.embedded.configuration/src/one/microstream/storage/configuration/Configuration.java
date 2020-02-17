
package one.microstream.storage.configuration;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;

import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageChannelCountProvider;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.storage.types.StorageHousekeepingController;


public interface Configuration
{
	public static Configuration LoadIni(final Path path)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromPath(path)
		);
	}
	
	public static Configuration LoadIni(final Path path, final Charset charset)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromPath(path, charset)
		);
	}
	
	public static Configuration LoadIni(final File file)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromFile(file)
		);
	}
	
	public static Configuration LoadIni(final File file, final Charset charset)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromFile(file, charset)
		);
	}
	
	public static Configuration LoadIni(final URL url)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromUrl(url)
		);
	}
	
	public static Configuration LoadIni(final URL url, final Charset charset)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromUrl(url, charset)
		);
	}
	
	public static Configuration LoadIni(final InputStream inputStream)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.FromInputStream(inputStream).loadConfiguration()
		);
	}
	
	public static Configuration LoadIni(final InputStream inputStream, final Charset charset)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.FromInputStream(inputStream, charset).loadConfiguration()
		);
	}
	
	public static Configuration LoadXml(final Path path)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromPath(path)
		);
	}
	
	public static Configuration LoadXml(final Path path, final Charset charset)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromPath(path, charset)
		);
	}
	
	public static Configuration LoadXml(final File file)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromFile(file)
		);
	}
	
	public static Configuration LoadXml(final File file, final Charset charset)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromFile(file, charset)
		);
	}
	
	public static Configuration LoadXml(final URL url)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromUrl(url)
		);
	}
	
	public static Configuration LoadXml(final URL url, final Charset charset)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromUrl(url, charset)
		);
	}
	
	public static Configuration LoadXml(final InputStream inputStream)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.FromInputStream(inputStream).loadConfiguration()
		);
	}
	
	public static Configuration LoadXml(final InputStream inputStream, final Charset charset)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.FromInputStream(inputStream, charset).loadConfiguration()
		);
	}
	
	public default EmbeddedStorageFoundation<?> createEmbeddedStorageFoundation()
	{
		return EmbeddedStorageFoundationCreator.New().createFoundation(this);
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
		final File userHomeDir = new File(System.getProperty("user.home"));
		this.setBaseDirectory(new File(userHomeDir, baseDirectoryInUserHome).getAbsolutePath());
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
	 *
	 * @param backupDirectoryInUserHome
	 *            relative location in the user home directory
	 */
	public default Configuration setBackupDirectoryInUserHome(final String backupDirectoryInUserHome)
	{
		final File userHomeDir = new File(System.getProperty("user.home"));
		this.setBackupDirectory(new File(userHomeDir, backupDirectoryInUserHome).getAbsolutePath());
		return this;
	}
	
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
	 * @param transactionFileSuffix
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
	 * @deprecated replaced by {@link #setHousekeepingIntervalMs(long)}, will be removed in a future release
	 */
	@Deprecated
	public default Configuration setHouseKeepingInterval(final long houseKeepingInterval)
	{
		return this.setHousekeepingIntervalMs(houseKeepingInterval);
	}
	
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
	public Configuration setHousekeepingIntervalMs(long houseKeepingIntervalMs);
	
	/**
	 * @deprecated replaced by {@link #getHousekeepingIntervalMs()}, will be removed in a future release
	 */
	@Deprecated
	public default long getHouseKeepingInterval()
	{
		return this.getHousekeepingIntervalMs();
	}
	
	/**
	 * Interval in milliseconds for the houskeeping. This is work like garbage
	 * collection or cache checking.
	 *
	 * @see #getHousekeepingTimeBudgetNs()
	 */
	public long getHousekeepingIntervalMs();
	
	/**
	 * @deprecated replaced by {@link #setHousekeepingTimeBudgetNs(long)}, will be removed in a future release
	 */
	@Deprecated
	public default Configuration setHouseKeepingNanoTimeBudget(final long houseKeepingNanoTimeBudget)
	{
		return this.setHousekeepingTimeBudgetNs(houseKeepingNanoTimeBudget);
	}
	
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
	public Configuration setHousekeepingTimeBudgetNs(long housekeepingTimeBudgetNs);
	
	/**
	 * @deprecated replaced by {@link #getHousekeepingTimeBudgetNs()}, will be removed in a future release
	 */
	@Deprecated
	public default long getHouseKeepingNanoTimeBudget()
	{
		return this.getHousekeepingTimeBudgetNs();
	}
	
	/**
	 * Number of nanoseconds used for each housekeeping cycle. However, no
	 * matter how low the number is, one item of work will always be completed.
	 * But if there is nothing to clean up, no processor time will be wasted.
	 *
	 * @see #getHousekeepingIntervalMs()
	 */
	public long getHousekeepingTimeBudgetNs();
	
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
	 * @deprecated replaced by {@link #setEntityCacheTimeoutMs(long)}, will be removed in a future release
	 */
	@Deprecated
	public default Configuration setEntityCacheTimeout(final long entityCacheTimeout)
	{
		return this.setEntityCacheTimeoutMs(entityCacheTimeout);
	}
	
	/**
	 * Timeout in milliseconds for the entity cache evaluator. If an entity
	 * wasn't accessed in this timespan it will be removed from the cache.
	 * Default is <code>86400000</code> (1 day).
	 *
	 * @param entityCacheTimeoutMs
	 *
	 * @see Duration
	 */
	public Configuration setEntityCacheTimeoutMs(long entityCacheTimeoutMs);
	
	/**
	 * @deprecated replaced by {@link #getEntityCacheTimeoutMs()}, will be removed in a future release
	 */
	@Deprecated
	public default long getEntityCacheTimeout()
	{
		return this.getEntityCacheTimeoutMs();
	}
	
	/**
	 * Timeout in milliseconds for the entity cache evaluator. If an entity
	 * wasn't accessed in this timespan it will be removed from the cache.
	 */
	public long getEntityCacheTimeoutMs();
	
	/**
	 * @deprecated replaced by {@link #setDataFileMinimumSize(int)}, will be removed in a future release
	 */
	@Deprecated
	public default Configuration setDataFileMinSize(final int dataFileMinSize)
	{
		return this.setDataFileMinimumSize(dataFileMinSize);
	}
	
	/**
	 * Minimum file size for a data file to avoid cleaning it up. Default is
	 * 1024^2 = 1 MiB.
	 *
	 * @param dataFileMinimumSize
	 *            the new minimum file size
	 *
	 * @see #setDataFileMinimumUseRatio(double)
	 */
	public Configuration setDataFileMinimumSize(int dataFileMinimumSize);
	
	/**
	 * @deprecated replaced by {@link #getDataFileMinimumSize()}, will be removed in a future release
	 */
	@Deprecated
	public default int getDataFileMinSize()
	{
		return this.getDataFileMinimumSize();
	}
	
	/**
	 * Minimum file size for a data file to avoid cleaning it up.
	 *
	 * @see #getDataFileMinimumUseRatio()
	 */
	public int getDataFileMinimumSize();
	
	/**
	 * @deprecated replaced by {@link #setDataFileMaximumSize(int)}, will be removed in a future release
	 */
	@Deprecated
	public default Configuration setDataFileMaxSize(final int dataFileMaxSize)
	{
		return this.setDataFileMaximumSize(dataFileMaxSize);
	}
	
	/**
	 * Maximum file size for a data file to avoid cleaning it up. Default is
	 * 1024^2*8 = 8 MiB.
	 *
	 * @param dataFileMaximumSize
	 *            the new maximum file size
	 *
	 * @see #setDataFileMinimumUseRatio(double)
	 */
	public Configuration setDataFileMaximumSize(int dataFileMaximumSize);
	
	/**
	 * @deprecated replaced by {@link #getDataFileMaximumSize()}, will be removed in a future release
	 */
	@Deprecated
	public default int getDataFileMaxSize()
	{
		return this.getDataFileMaximumSize();
	}
	
	/**
	 * Maximum file size for a data file to avoid cleaning it up.
	 *
	 * @see #getDataFileMinimumUseRatio()
	 */
	public int getDataFileMaximumSize();
	
	/**
	 * @deprecated replaced by {@link #setDataFileMinimumUseRatio(double)}, will be removed in a future release
	 */
	@Deprecated
	public default Configuration setDataFileDissolveRatio(final double dataFileDissolveRatio)
	{
		return this.setDataFileMinimumUseRatio(dataFileDissolveRatio);
	}
	
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
	public Configuration setDataFileMinimumUseRatio(double dataFileMinimumUseRatio);
	
	/**
	 * @deprecated replaced by {@link #getDataFileMinimumUseRatio()}, will be removed in a future release
	 */
	@Deprecated
	public default double getDataFileDissolveRatio()
	{
		return this.getDataFileMinimumUseRatio();
	}
	
	/**
	 * The ratio (value in ]0.0;1.0]) of non-gap data contained in a storage file to prevent
	 * the file from being dissolved. "Gap" data is anything that is not the latest version of an entity's data,
	 * inluding older versions of an entity and "comment" bytes (a sequence of bytes beginning with its length
	 * as a negative value length header).<br>
	 * The closer this value is to 1.0 (100%), the less disk space is occupied by storage files, but the more
	 * file dissolving (data transfers to new files) is required and vice versa.
	 */
	public double getDataFileMinimumUseRatio();
	
	/**
	 * A flag defining wether the current head file (the only file actively written to)
	 * shall be subjected to file cleanups as well.
	 * 
	 * @param dataFileCleanupHeadFile
	 */
	public Configuration setDataFileCleanupHeadFile(boolean dataFileCleanupHeadFile);
	
	/**
	 * A flag defining wether the current head file (the only file actively written to)
	 * shall be subjected to file cleanups as well.
	 */
	public boolean getDataFileCleanupHeadFile();
	
	
	public static Configuration Default()
	{
		return new Default();
	}
	
	
	public static class Default implements Configuration
	{
		private String  baseDirectory            = StorageFileProvider.Defaults.defaultStorageDirectory();
		private String  deletionDirectory        = StorageFileProvider.Defaults.defaultDeletionDirectory();
		private String  truncationDirectory      = StorageFileProvider.Defaults.defaultTruncationDirectory();
		private String  backupDirectory          = null; // no on-the-fly backup by default
		private String  channelDirectoryPrefix   = StorageFileProvider.Defaults.defaultChannelDirectoryPrefix();
		private String  dataFilePrefix           = StorageFileProvider.Defaults.defaultStorageFilePrefix();
		private String  dataFileSuffix           = StorageFileProvider.Defaults.defaultStorageFileSuffix();
		private String  transactionFilePrefix    = StorageFileProvider.Defaults.defaultTransactionFilePrefix();
		private String  transactionFileSuffix    = StorageFileProvider.Defaults.defaultTransactionFileSuffix();
		private String  typeDictionaryFilename   = StorageFileProvider.Defaults.defaultTypeDictionaryFileName();
		
		private int     channelCount             = StorageChannelCountProvider.Defaults.defaultChannelCount();
		
		private long    housekeepingIntervalMs   = StorageHousekeepingController.Defaults.defaultHousekeepingIntervalMs();
		private long    housekeepingTimeBudgetNs = StorageHousekeepingController.Defaults.defaultHousekeepingTimeBudgetNs();
	
		private long    entityCacheTimeoutMs     = StorageEntityCacheEvaluator.Defaults.defaultTimeoutMs();
		private long    entityCacheThreshold     = StorageEntityCacheEvaluator.Defaults.defaultCacheThreshold();
		
		private int     dataFileMinimumSize      = StorageDataFileEvaluator.Defaults.defaultFileMinimumSize();
		private int     dataFileMaximumSize      = StorageDataFileEvaluator.Defaults.defaultFileMaximumSize();
		private double  dataFileMinimumUseRatio  = StorageDataFileEvaluator.Defaults.defaultMinimumUseRatio();
		private boolean dataFileCleanupHeadFile  = StorageDataFileEvaluator.Defaults.defaultResolveHeadfile();
		
		
		Default()
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
			StorageChannelCountProvider.Validation.validateParameters(channelCount);
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
			this.channelDirectoryPrefix = notNull(channelDirectoryPrefix);
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
			this.dataFilePrefix = notNull(dataFilePrefix);
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
			this.dataFileSuffix = notNull(dataFileSuffix);
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
			this.transactionFilePrefix = notNull(transactionFilePrefix);
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
			this.transactionFileSuffix = notNull(transactionFileSuffix);
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
		public Configuration setHousekeepingIntervalMs(final long housekeepingIntervalMs)
		{
			StorageHousekeepingController.Validation.validateParameters(
				housekeepingIntervalMs,
				StorageHousekeepingController.Defaults.defaultHousekeepingTimeBudgetNs()
			);
			this.housekeepingIntervalMs = housekeepingIntervalMs;
			return this;
		}
		
		@Override
		public long getHousekeepingIntervalMs()
		{
			return this.housekeepingIntervalMs;
		}
		
		@Override
		public Configuration setHousekeepingTimeBudgetNs(final long housekeepingNanoTimeBudgetNs)
		{
			StorageHousekeepingController.Validation.validateParameters(
				StorageHousekeepingController.Defaults.defaultHousekeepingIntervalMs(),
				housekeepingNanoTimeBudgetNs
			);
			this.housekeepingTimeBudgetNs = housekeepingNanoTimeBudgetNs;
			return this;
		}
		
		@Override
		public long getHousekeepingTimeBudgetNs()
		{
			return this.housekeepingTimeBudgetNs;
		}
		
		@Override
		public Configuration setEntityCacheThreshold(final long entityCacheThreshold)
		{
			StorageEntityCacheEvaluator.Validation.validateParameters(
				StorageEntityCacheEvaluator.Defaults.defaultTimeoutMs(),
				entityCacheThreshold
			);
			this.entityCacheThreshold = entityCacheThreshold;
			return this;
		}
		
		@Override
		public long getEntityCacheThreshold()
		{
			return this.entityCacheThreshold;
		}
		
		@Override
		public Configuration setEntityCacheTimeoutMs(final long entityCacheTimeoutMs)
		{
			StorageEntityCacheEvaluator.Validation.validateParameters(
				entityCacheTimeoutMs,
				StorageEntityCacheEvaluator.Defaults.defaultCacheThreshold()
			);
			this.entityCacheTimeoutMs = entityCacheTimeoutMs;
			return this;
		}
		
		@Override
		public long getEntityCacheTimeoutMs()
		{
			return this.entityCacheTimeoutMs;
		}
		
		@Override
		public Configuration setDataFileMinimumSize(final int dataFileMinimumSize)
		{
			StorageDataFileEvaluator.Validation.validateParameters(
				dataFileMinimumSize,
				dataFileMinimumSize + 1,
				StorageDataFileEvaluator.Defaults.defaultMinimumUseRatio()
			);
			this.dataFileMinimumSize = dataFileMinimumSize;
			return this;
		}
		
		@Override
		public int getDataFileMinimumSize()
		{
			return this.dataFileMinimumSize;
		}
		
		@Override
		public Configuration setDataFileMaximumSize(final int dataFileMaximumSize)
		{
			StorageDataFileEvaluator.Validation.validateParameters(
				dataFileMaximumSize - 1,
				dataFileMaximumSize,
				StorageDataFileEvaluator.Defaults.defaultMinimumUseRatio()
			);
			this.dataFileMaximumSize = dataFileMaximumSize;
			return this;
		}
		
		@Override
		public int getDataFileMaximumSize()
		{
			return this.dataFileMaximumSize;
		}
		
		@Override
		public Configuration setDataFileMinimumUseRatio(final double dataFileMinimumUseRatio)
		{
			StorageDataFileEvaluator.Validation.validateParameters(
				StorageDataFileEvaluator.Defaults.defaultFileMinimumSize(),
				StorageDataFileEvaluator.Defaults.defaultFileMaximumSize(),
				dataFileMinimumUseRatio
			);
			this.dataFileMinimumUseRatio = dataFileMinimumUseRatio;
			return this;
		}
		
		@Override
		public double getDataFileMinimumUseRatio()
		{
			return this.dataFileMinimumUseRatio;
		}
		
		@Override
		public Configuration setDataFileCleanupHeadFile(
			final boolean dataFileCleanupHeadFile
		)
		{
			this.dataFileCleanupHeadFile = dataFileCleanupHeadFile;
			return this;
		}
		
		@Override
		public boolean getDataFileCleanupHeadFile()
		{
			return this.dataFileCleanupHeadFile;
		}
		
	}
	
}
