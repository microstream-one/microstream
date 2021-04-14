package one.microstream.storage.types;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFileSystem;
import one.microstream.persistence.types.Persistence;


/**
 * Static utility class containing static pseudo-constructor methods (indicated by a capital first letter)
 * and various utility methods.
 * <p>
 * To setup and start a database, see the class "EmbeddedStorage".
 *
 *
 * 
 * @see Persistence
 * @see StorageChannel
 */
public final class Storage
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	/**
	 * Trivial helper constant.
	 */
	private static final long ONE_MILLION = 1_000_000L;

	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	static final long millisecondsToNanoseconds(final long milliseconds)
	{
		return milliseconds * ONE_MILLION;
	}

	static final long nanosecondsToMilliseconds(final long nanoseconds)
	{
		return nanoseconds / ONE_MILLION;
	}

	static final long millisecondsToSeconds(final long milliseconds)
	{
		return milliseconds / ONE_MILLION;
	}


	
	public static NioFileSystem DefaultFileSystem()
	{
		return NioFileSystem.New();
	}
	
	public static ADirectory defaultStorageDirectory()
	{
		return defaultStorageDirectory(DefaultFileSystem());
	}
	
	/**
	 * Returns the default storage directory in the current working directory and with a filename defined by
	 * {@link StorageLiveFileProvider.Defaults#defaultStorageDirectory}.
	 *
	 * @return the default storage directory located in the current working directory.
	 */
	public static ADirectory defaultStorageDirectory(final AFileSystem fileSystem)
	{
		return fileSystem.ensureRoot(StorageLiveFileProvider.Defaults.defaultStorageDirectory());
	}
	
	/**
	 * {@linkDoc StorageLiveFileProvider#New()}
	 *
	 * @return {@linkDoc StorageLiveFileProvider#New()@return}
	 *
	 * @see Storage#FileProvider(Path)
	 * @see StorageLiveFileProvider#New()
	 * @see StorageLiveFileProvider.Builder
	 * @see StorageLiveFileProvider.Defaults
	 */
	public static final StorageLiveFileProvider FileProvider()
	{
		return StorageLiveFileProvider.New();
	}

	/**
	 * Alias for {@code FileProvider(storageDirectory.toPath())}
	 *
	 * @param storageDirectory {@linkDoc StorageLiveFileProvider#New(ADirectory):}
	 *
	 * @return {@linkDoc StorageLiveFileProvider#New(ADirectory)@return}
	 *
	 * @deprecated replaced by {@link #FileProvider(Path)}
	 */
	@Deprecated
	public static final StorageLiveFileProvider FileProvider(final File storageDirectory)
	{
		return FileProvider(storageDirectory.toPath());
	}

	/**
	 * {@linkDoc StorageLiveFileProvider#New(ADirectory)}
	 *
	 * @param storageDirectory {@linkDoc StorageLiveFileProvider#New(ADirectory):}
	 *
	 * @return {@linkDoc StorageLiveFileProvider#New(ADirectory)@return}
	 *
	 * @see Storage#FileProvider()
	 * @see StorageLiveFileProvider#New(Path)
	 * @see StorageLiveFileProvider.Builder
	 * @see StorageLiveFileProvider.Defaults
	 */
	public static final StorageLiveFileProvider FileProvider(final Path storageDirectory)
	{
		final NioFileSystem nfs = NioFileSystem.New(storageDirectory.getFileSystem());
		
		final ADirectory dir = nfs.ensureDirectory(storageDirectory);
		
		return StorageLiveFileProvider.New(dir);
	}
	
	public static final StorageLiveFileProvider FileProvider(final ADirectory storageDirectory)
	{
		return StorageLiveFileProvider.New(storageDirectory);
	}

	/**
	 * {@linkDoc StorageLiveFileProvider#Builder()}
	 *
	 * @return {@linkDoc StorageLiveFileProvider#Builder()@return}
	 *
	 * @see Storage#FileProvider()
	 * @see Storage#FileProvider(Path)
	 * @see StorageLiveFileProvider.Builder
	 */
	public static final StorageLiveFileProvider.Builder<?> FileProviderBuilder()
	{
		return StorageLiveFileProvider.Builder();
	}
	
	public static final StorageLiveFileProvider.Builder<?> FileProviderBuilder(final AFileSystem fileSystem)
	{
		return StorageLiveFileProvider.Builder(fileSystem);
	}
	
	
	
	
	public static final StorageBackupFileProvider BackupFileProvider()
	{
		return StorageBackupFileProvider.New();
	}

	@Deprecated
	public static final StorageBackupFileProvider BackupFileProvider(final File storageDirectory)
	{
		return BackupFileProvider(storageDirectory.toPath());
	}

	public static final StorageBackupFileProvider BackupFileProvider(final Path storageDirectory)
	{
		// note that the backup's file system may potentially be completely different from the live file system.
		final NioFileSystem nfs = NioFileSystem.New(storageDirectory.getFileSystem());
		final ADirectory    dir = nfs.resolveDirectory(storageDirectory);
		
		return StorageBackupFileProvider.New(dir);
	}
	
	public static final StorageBackupFileProvider BackupFileProvider(final ADirectory storageDirectory)
	{
		return StorageBackupFileProvider.New(storageDirectory);
	}

	public static final StorageBackupFileProvider.Builder<?> BackupFileProviderBuilder()
	{
		return StorageBackupFileProvider.Builder();
	}
	
	public static final StorageBackupFileProvider.Builder<?> BackupFileProviderBuilder(final AFileSystem fileSystem)
	{
		return StorageBackupFileProvider.Builder(fileSystem);
	}
	
	
	
	

	/**
	 * {@linkDoc StorageConfiguration#New()}
	 *
	 * @return {@linkDoc StorageConfiguration#New()@return}
	 *
	 * @see Storage#Configuration(StorageLiveFileProvider)
	 * @see StorageConfiguration#New()
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration Configuration()
	{
		return StorageConfiguration.New();
	}

	/**
	 * {@linkDoc StorageConfiguration#New(StorageLiveFileProvider)}
	 *
	 * @param fileProvider {@linkDoc StorageConfiguration#New(StorageLiveFileProvider):}
	 *
	 * @return {@linkDoc StorageConfiguration#New(StorageLiveFileProvider)@return}
	 *
	 * @see Storage#Configuration()
	 * @see StorageConfiguration#New(StorageLiveFileProvider)
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration Configuration(
		final StorageLiveFileProvider fileProvider
	)
	{
		return StorageConfiguration.New(fileProvider);
	}

	/**
	 * {@linkDoc StorageConfiguration#Builder()}
	 *
	 * @return {@linkDoc StorageConfiguration#Builder()@return}
	 *
	 * @see Storage#Configuration()
	 * @see Storage#Configuration(StorageLiveFileProvider)
	 * @see StorageConfiguration#Builder()
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration.Builder<?> ConfigurationBuilder()
	{
		return StorageConfiguration.Builder();
	}

	/**
	 * {@linkDoc StorageHousekeepingController#New()}
	 *
	 * @return {@linkDoc StorageHousekeepingController#New()@return}
	 *
	 * @see Storage#HousekeepingController(long, long)
	 * @see StorageHousekeepingController#New()
	 * @see StorageHousekeepingController.Defaults
	 */
	public static final StorageHousekeepingController HousekeepingController()
	{
		return StorageHousekeepingController.New();
	}

	/**
	 * {@linkDoc StorageHousekeepingController#New(long, long)}
	 *
	 * @param housekeepingIntervalMs {@linkDoc StorageHousekeepingController#New(long, long):}	 *
	 * @param housekeepingTimeBudgetNs {@linkDoc StorageHousekeepingController#New(long, long):}
	 *
	 * @return {@linkDoc StorageHousekeepingController#New(long, long)@return}
	 *
	 * @see Storage#HousekeepingController()
	 * @see StorageHousekeepingController#New(long, long)
	 */
	public static final StorageHousekeepingController HousekeepingController(
		final long housekeepingIntervalMs  ,
		final long housekeepingTimeBudgetNs
	)
	{
		return StorageHousekeepingController.New(housekeepingIntervalMs, housekeepingTimeBudgetNs);
	}

	/**
	 * {@linkDoc StorageEntityCacheEvaluator#New()}
	 *
	 * @return {@linkDoc StorageEntityCacheEvaluator#New()@return}
	 *
	 * @see Storage#EntityCacheEvaluator(long)
	 * @see Storage#EntityCacheEvaluator(long, long)
	 * @see StorageEntityCacheEvaluator#New()
	 */
	public static final StorageEntityCacheEvaluator EntityCacheEvaluator()
	{
		return StorageEntityCacheEvaluator.New();
	}

	/**
	 * {@linkDoc StorageEntityCacheEvaluator#New(long)}
	 *
	 * @param timeoutMs {@linkDoc StorageEntityCacheEvaluator#New(long):}
	 *
	 * @return {@linkDoc StorageEntityCacheEvaluator#New(long)@return}
	 *
	 * @throws {@linkDoc StorageEntityCacheEvaluator#New(long)@throws}
	 *
	 * @see Storage#EntityCacheEvaluator()
	 * @see Storage#EntityCacheEvaluator(long, long)
	 * @see StorageEntityCacheEvaluator#New(long)
	 */
	public static final StorageEntityCacheEvaluator EntityCacheEvaluator(
		final long timeoutMs
	)
	{
		return StorageEntityCacheEvaluator.New(timeoutMs);
	}

	/**
	 * {@linkDoc StorageEntityCacheEvaluator#New(long, long)}
	 *
	 * @param timeoutMs {@linkDoc StorageEntityCacheEvaluator#New(long, long):}
	 * @param threshold {@linkDoc StorageEntityCacheEvaluator#New(long, long):}
	 *
	 * @return {@linkDoc StorageEntityCacheEvaluator#New(long, long)@return}
	 *
	 * @throws {@linkDoc StorageEntityCacheEvaluator#New(long, long)@throws}
	 *
	 * @see Storage#EntityCacheEvaluator()
	 * @see Storage#EntityCacheEvaluator(long)
	 * @see StorageEntityCacheEvaluator#New(long, long)
	 */
	public static final StorageEntityCacheEvaluator EntityCacheEvaluator(
		final long timeoutMs,
		final long threshold
	)
	{
		return StorageEntityCacheEvaluator.New(timeoutMs, threshold);
	}

	/**
	 * {@linkDoc StorageChannelCountProvider#New()}
	 *
	 * @return {@linkDoc StorageChannelCountProvider#New()@return}
	 *
	 * @see Storage#ChannelCountProvider(int)
	 * @see StorageChannelCountProvider#New()
	 */
	public static final StorageChannelCountProvider ChannelCountProvider()
	{
		return StorageChannelCountProvider.New();
	}

	/**
	 * {@linkDoc StorageChannelCountProvider#New(int)}
	 *
	 * @param channelCount {@linkDoc StorageChannelCountProvider#New(int):}
	 *
	 * @return {@linkDoc StorageChannelCountProvider#New(int)@return}
	 */
	public static final StorageChannelCountProvider ChannelCountProvider(final int channelCount)
	{
		return StorageChannelCountProvider.New(channelCount);
	}

	/**
	 * {@linkDoc StorageDataFileEvaluator#New()}
	 *
	 * @return {@linkDoc StorageDataFileEvaluator#New()@return}
	 *
	 * @see Storage#DataFileEvaluator(int, int)
	 * @see Storage#DataFileEvaluator(int, int, double)
	 * @see StorageDataFileEvaluator#New()
	 */
	public static final StorageDataFileEvaluator DataFileEvaluator()
	{
		return StorageDataFileEvaluator.New();
	}

	/**
	 * {@linkDoc StorageDataFileEvaluator#New(int, int)}
	 *
	 * @param fileMinimumSize {@linkDoc StorageDataFileEvaluator#New(int, int):}
	 * @param fileMaximumSize {@linkDoc StorageDataFileEvaluator#New(int, int):}
	 *
	 * @return {@linkDoc StorageDataFileEvaluator#New(int, int)@return}
	 *
	 * @see Storage#DataFileEvaluator()
	 * @see Storage#DataFileEvaluator(int, int, double)
	 * @see StorageDataFileEvaluator#New(int, int)
	 */
	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int fileMinimumSize,
		final int fileMaximumSize
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize);
	}

	/**
	 * {@linkDoc StorageDataFileEvaluator#New(int, int, double)}
	 *
	 * @param fileMinimumSize {@linkDoc StorageDataFileEvaluator#New(int, int, double):}
	 * @param fileMaximumSize {@linkDoc StorageDataFileEvaluator#New(int, int, double):}
	 *
	 * @return {@linkDoc StorageDataFileEvaluator#New(int, int, double)@return}
	 *
	 * @see Storage#DataFileEvaluator()
	 * @see Storage#DataFileEvaluator(int, int)
	 * @see StorageDataFileEvaluator#New(int, int, double)
	 */
	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int    fileMinimumSize,
		final int    fileMaximumSize,
		final double minimumUseRatio
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize, minimumUseRatio);
	}

	/**
	 * {@linkDoc StorageDataFileEvaluator#New(int, int, double, boolean)}
	 *
	 * @param fileMinimumSize {@linkDoc StorageDataFileEvaluator#New(int, int, double, boolean):}
	 * @param fileMaximumSize {@linkDoc StorageDataFileEvaluator#New(int, int, double, boolean):}
	 * @param minimumUseRatio {@linkDoc StorageDataFileEvaluator#New(int, int, double, boolean):}
	 * @param cleanUpHeadFile {@linkDoc StorageDataFileEvaluator#New(int, int, double, boolean):}
	 *
	 * @return dissolveHeadfile {@linkDoc StorageDataFileEvaluator#New(int, int, double, boolean)@return}
	 *
	 * @see Storage#DataFileEvaluator()
	 * @see Storage#DataFileEvaluator(int, int)
	 * @see Storage#DataFileEvaluator(int, int, double)
	 * @see StorageDataFileEvaluator#New(int, int, double, boolean)
	 */
	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int     fileMinimumSize,
		final int     fileMaximumSize,
		final double  minimumUseRatio,
		final boolean cleanUpHeadFile
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize, minimumUseRatio, cleanUpHeadFile);
	}

	/**
	 * @deprecated replaced by {@link #BackupSetup(Path)}
	 */
	@Deprecated
	public static final StorageBackupSetup BackupSetup(final File backupDirectory)
	{
		return BackupSetup(backupDirectory.toPath());
	}
	
	/**
	 * {@linkDoc StorageBackupSetup#New(ADirectory)}
	 *
	 * @param backupDirectory {@linkDoc StorageBackupSetup#New(ADirectory):}
	 *
	 * @return {@linkDoc StorageBackupSetup#New(ADirectory)@return}
	 *
	 * @see StorageBackupSetup#New(StorageLiveFileProvider)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final Path backupDirectory)
	{
		// note that the backup's file system may potentially be completely different from the live file system.
		final NioFileSystem nfs = NioFileSystem.New(backupDirectory.getFileSystem());
		final ADirectory dir = nfs.ensureDirectory(backupDirectory);
		
		return BackupSetup(dir);
	}
	
	/**
	 * {@linkDoc StorageBackupSetup#New(ADirectory)}
	 *
	 * @param backupDirectory {@linkDoc StorageBackupSetup#New(ADirectory):}
	 *
	 * @return {@linkDoc StorageBackupSetup#New(ADirectory)@return}
	 *
	 * @see StorageBackupSetup#New(StorageLiveFileProvider)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final ADirectory backupDirectory)
	{
		return StorageBackupSetup.New(backupDirectory);
	}

	/**
	 * {@linkDoc StorageBackupSetup#New(ADirectory)}
	 *
	 * @param backupDirectoryPath the path to the backup directory
	 *
	 * @return {@linkDoc StorageBackupSetup#New(ADirectory)@return}
	 *
	 * @see StorageBackupSetup#New(Path)
	 * @see StorageBackupSetup#New(StorageLiveFileProvider)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final String backupDirectoryPath)
	{
		return BackupSetup(Paths.get(backupDirectoryPath));
	}

	/**
	 * {@linkDoc StorageBackupSetup#New(StorageBackupFileProvider)}
	 *
	 * @param backupFileProvider {@linkDoc StorageBackupSetup#New(StorageBackupFileProvider):}
	 *
	 * @return {@linkDoc StorageBackupSetup#New(StorageBackupFileProvider)@return}
	 *
	 * @see StorageBackupSetup#New(Path)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final StorageBackupFileProvider backupFileProvider)
	{
		return StorageBackupSetup.New(backupFileProvider);
	}

	/**
	 * {@linkDoc StorageLockFileSetup#Provider()}
	 *
	 * @return {@linkDoc StorageLockFileSetup#Provider()@return}
	 *
	 * @see StorageLockFileSetup
	 * @see #LockFileSetupProvider(Charset)
	 * @see #LockFileSetupProvider(long)
	 * @see #LockFileSetupProvider(Charset, long)
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider()
	{
		return StorageLockFileSetup.Provider();
	}

	/**
	 * {@linkDoc StorageLockFileSetup#Provider(Charset)}
	 *
	 * @param charset {@linkDoc StorageLockFileSetup#Provider(Charset):}
	 *
	 * @return {@linkDoc StorageLockFileSetup#Provider(Charset)@return}
	 *
	 * @see StorageLockFileSetup
	 * @see #LockFileSetupProvider()
	 * @see #LockFileSetupProvider(long)
	 * @see #LockFileSetupProvider(Charset, long)
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider(
		final Charset charset
	)
	{
		return StorageLockFileSetup.Provider(charset);
	}

	/**
	 * {@linkDoc StorageLockFileSetup#Provider(long)}
	 *
	 * @param updateInterval {@linkDoc StorageLockFileSetup#Provider(long):}
	 *
	 * @return {@linkDoc StorageLockFileSetup#Provider(long)@return}
	 *
	 * @see StorageLockFileSetup
	 * @see #LockFileSetupProvider()
	 * @see #LockFileSetupProvider(Charset)
	 * @see #LockFileSetupProvider(Charset, long)
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider(
		final long updateInterval
	)
	{
		return StorageLockFileSetup.Provider(updateInterval);
	}

	/**
	 * {@linkDoc StorageLockFileSetup#Provider(Charset, long)}
	 *
	 * @param charset {@linkDoc StorageLockFileSetup#Provider(Charset, long):}
	 * @param updateInterval {@linkDoc StorageLockFileSetup#Provider(Charset, long):}
	 *
	 * @return {@linkDoc StorageLockFileSetup#Provider(Charset, long)@return}
	 *
	 * @see StorageLockFileSetup
	 * @see #LockFileSetupProvider()
	 * @see #LockFileSetupProvider(Charset)
	 * @see #LockFileSetupProvider(long)
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider(
		final Charset charset       ,
		final long    updateInterval
	)
	{
		return StorageLockFileSetup.Provider(charset, updateInterval);
	}

	/**
	 * Consolidates the storage system represented by the passed {@link StorageConnection} by calling<br>
	 * {@link StorageConnection#issueFullGarbageCollection()}<br>
	 * {@link StorageConnection#issueFullFileCheck()}<br>
	 * {@link StorageConnection#issueFullCacheCheck(StorageEntityCacheEvaluator)}<br>
	 * in that order.
	 * <p>
	 * Depending on the passed functions, this call can do anything from cleaning/optimizing the storage a little to
	 * fully reorganize/optimize the storage files, clear the complete cache and making the storage virtually dormant
	 * until the next store.
	 *
	 * @param storageConnection The connection to the storage that shall be consolidated.
	 * @param entityEvaluator   The function evaluating whether to clear an entity from the cache.<br>
	 *                          May be {@literal null} to indicate the use of the live configuration as a default.
	 *
	 * @return the passed storageConnection instance.
	 */
	public static final <C extends StorageConnection> C consolidate(
		final C                           storageConnection,
		final StorageEntityCacheEvaluator entityEvaluator
	)
	{
		/* calls must be done in that order to achieve highest (normal) effectivity:
		 * - first discard all unreachable entities.
		 * - then cleanup files depending on the resulting data occupation.
		 * - clean the entity cache.
		 */
		storageConnection.issueFullGarbageCollection();
		storageConnection.issueFullFileCheck();
		storageConnection.issueFullCacheCheck(entityEvaluator);
		return storageConnection;
	}

	/**
	 * Calls {@link Storage#consolidate(StorageConnection, StorageEntityCacheEvaluator)}
	 * with {@literal null} as additional parameters (causing live configuration to be used instead).
	 *
	 * @param storageConnection {@linkDoc Storage#consolidate(StorageConnection, StorageEntityCacheEvaluator):}
	 *
	 * @return {@linkDoc Storage#consolidate(StorageConnection, StorageEntityCacheEvaluator)@return}
	 */
	public static final <C extends StorageConnection> C consolidate(final C storageConnection)
	{
		return consolidate(storageConnection, null);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException
	 */
	private Storage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
