package one.microstream.storage.types;

import java.io.File;
import java.nio.charset.Charset;

import one.microstream.persistence.types.Persistence;


/**
 * Static utility class containing static pseudo-constructor methods (indicated by a capital first letter)
 * and various utility methods.
 * <p>
 * To setup and start a database, see the class "EmbeddedStorage".
 * 
 * see {@link StorageChannel}
 * <p>
 * this is {@literal true}<br>
 * this is {@value #ONE_MILLION}
 * 
 * 
 * @author TM
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
	
	/**
	 * Dummy file number for the transactions file.
	 */
	private static final long TRANSACTIONS_FILE_NUMBER = -1;
	


	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Returns the dummy file number for transaction files, which is the value {@value #TRANSACTIONS_FILE_NUMBER}.
	 * <p>
	 * Transaction files conceptually don't have a file number, but are subject to the {@link StorageNumberedFile}
	 * type, so a dummy value is required. Since transaction files are planned to be replaced in the future
	 * by meta data inlined directly in the storage files, a dummy value like this is a preferable solution
	 * to an elaborate restructuring.
	 * 
	 * @return the dummy file number for transaction files.
	 */
	public static final long transactionsFileNumber()
	{
		return TRANSACTIONS_FILE_NUMBER;
	}
	
	/**
	 * Checks if the passed {@link StorageNumberedFile} is a transaction file by comparing its file number to
	 * {@link Storage#transactionsFileNumber()}.
	 * 
	 * @param file the {@link StorageNumberedFile} to be checked.
	 * 
	 * @return whether the passed file is a transactions file.
	 * 
	 * @see Storage#transactionsFileNumber()
	 * @see Storage#isDataFile(StorageNumberedFile)
	 */
	public static final boolean isTransactionFile(final StorageNumberedFile file)
	{
		return file.number() == TRANSACTIONS_FILE_NUMBER;
	}
	
	/**
	 * Checks if the passed {@link StorageNumberedFile} is a storage data file.
	 * 
	 * @param file the {@link StorageNumberedFile} to be checked.
	 * 
	 * @return whether the passed file is a storage data file.
	 * 
	 * @see Storage#isTransactionFile(StorageNumberedFile)
	 */
	public static final boolean isDataFile(final StorageNumberedFile file)
	{
		return file.number() > 0;
	}
	
	/**
	 * {@linkDoc StorageFileProvider#New()}
	 * 
	 * @return {@linkDoc StorageFileProvider#New()@return}
	 * 
	 * @see Storage#FileProvider(File)
	 * @see StorageFileProvider#New()
	 * @see StorageFileProvider.Builder
	 * @see StorageFileProvider.Defaults
	 */
	public static final StorageFileProvider FileProvider()
	{
		return StorageFileProvider.New();
	}
	
	/**
	 * {@linkDoc StorageFileProvider#New(File)}
	 * 
	 * @param storageDirectory {@linkDoc StorageFileProvider#New(File):}
	 * 
	 * @return {@linkDoc StorageFileProvider#New(File)@return}
	 * 
	 * @see Storage#FileProvider()
	 * @see StorageFileProvider#New(File)
	 * @see StorageFileProvider.Builder
	 * @see StorageFileProvider.Defaults
	 */
	public static final StorageFileProvider FileProvider(final File storageDirectory)
	{
		return StorageFileProvider.New(storageDirectory);
	}
		
	/**
	 * {@linkDoc StorageFileProvider#Builder()}
	 * 
	 * @return {@linkDoc StorageFileProvider#Builder()@return}
	 * 
	 * @see Storage#FileProvider()
	 * @see Storage#FileProvider(File)
	 * @see StorageFileProvider.Builder
	 */
	public static final StorageFileProvider.Builder<?> FileProviderBuilder()
	{
		return StorageFileProvider.Builder();
	}

	/**
	 * {@linkDoc StorageConfiguration#New()}
	 * 
	 * @return {@linkDoc StorageConfiguration#New()@return}
	 * 
	 * @see Storage#Configuration(StorageFileProvider)
	 * @see StorageConfiguration#New()
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration Configuration()
	{
		return StorageConfiguration.New();
	}
	
	/**
	 * {@linkDoc StorageConfiguration#New(StorageFileProvider)}
	 * 
	 * @param fileProvider {@linkDoc StorageConfiguration#New(StorageFileProvider):}
	 * 
	 * @return {@linkDoc StorageConfiguration#New(StorageFileProvider)@return}
	 * 
	 * @see StorageConfiguration#Configuration()
	 * @see StorageConfiguration#New(StorageFileProvider)
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration Configuration(
		final StorageFileProvider fileProvider
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
	 * @see Storage#Configuration(StorageFileProvider)
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
	 * @param {@linkDoc StorageChannelCountProvider#New(int):}
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
	 * @param dissolveHeadfile {@linkDoc StorageDataFileEvaluator#New(int, int, double, boolean):}
	 * 
	 * @return dissolveHeadfile {@linkDoc StorageDataFileEvaluator#New(int, int, double, boolean)@return}
	 * 
	 * @see Storage#DataFileEvaluator()
	 * @see Storage#DataFileEvaluator(int, int)
	 * @see Storage#DataFileEvaluator(double)
	 * @see Storage#DataFileEvaluator(int, int, double)
	 * @see StorageDataFileEvaluator#New(int, int, double, boolean)
	 */
	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int     fileMinimumSize ,
		final int     fileMaximumSize ,
		final double  minimumUseRatio ,
		final boolean dissolveHeadfile
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize, minimumUseRatio, dissolveHeadfile);
	}
	
	/**
	 * {@linkDoc StorageBackupSetup#New(File)}
	 * 
	 * @param backupDirectory {@linkDoc StorageBackupSetup#New(File):}
	 * 
	 * @return {@linkDoc StorageBackupSetup#New(File)@return}
	 * 
	 * @see StorageBackupSetup#New(StorageFileProvider)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final File backupDirectory)
	{
		return StorageBackupSetup.New(backupDirectory);
	}
	
	/**
	 * {@linkDoc StorageBackupSetup#New(File)}
	 * 
	 * @param backupDirectoryPath the path to the backup directory
	 * 
	 * @return {@linkDoc StorageBackupSetup#New(File)@return}
	 * 
	 * @see StorageBackupSetup#New(StorageFileProvider)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final String backupDirectoryPath)
	{
		return StorageBackupSetup.New(new File(backupDirectoryPath));
	}

	/**
	 * {@linkDoc StorageBackupSetup#New(StorageFileProvider)}
	 * 
	 * @param fileProvider {@linkDoc StorageBackupSetup#New(StorageFileProvider):}
	 * 
	 * @return {@linkDoc StorageBackupSetup#New(StorageFileProvider)@return}
	 * 
	 * @see StorageBackupSetup#New(File)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final StorageFileProvider fileProvider)
	{
		return StorageBackupSetup.New(fileProvider);
	}
	
	// (28.05.2019 TM)FIXME: /!\ JavaDoc WIP.
	
	/**
	 * {@linkDoc StorageLockFileSetup#Provider()}
	 * 
	 * @return {@linkDoc StorageLockFileSetup#Provider()@return}
	 * 
	 * @see StorageLockFileSetup
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider()
	{
		return StorageLockFileSetup.Provider();
	}
	
	/**
	 * {@linkDoc StorageLockFileSetup#Provider(Charset)}
	 * 
	 * 
	 * @param {@linkDoc StorageLockFileSetup#Provider(Charset):}
	 * 
	 * @return {@linkDoc StorageLockFileSetup#Provider(Charset)@return}
	 * 
	 * @see StorageLockFileSetup
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider(
		final Charset charset
	)
	{
		return StorageLockFileSetup.Provider(charset);
	}
	
	public static StorageLockFileSetup.Provider LockFileSetupProvider(
		final long updateInterval
	)
	{
		return StorageLockFileSetup.Provider(updateInterval);
	}
	
	public static StorageLockFileSetup.Provider LockFileSetupProvider(
		final Charset charset       ,
		final long    updateInterval
	)
	{
		return StorageLockFileSetup.Provider(charset, updateInterval);
	}
	

	/**
	 * Calls {@link Storage#consolidate(StorageConnection, StorageDataFileDissolvingEvaluator, StorageEntityCacheEvaluator)}
	 * with {@literal null} as additional parameters (causing live configuration to be used instead).
	 *
	 * @param storageConnection the connection to the storage that shall be consolidated.
	 * @return the passed storageConnection instance.
	 */
	public static final <C extends StorageConnection> C consolidate(final C storageConnection)
	{
		return consolidate(storageConnection, null, null);
	}


	/**
	 * Consolidates the storage system represented by the passed {@link StorageConnection} by calling<br>
	 * {@link StorageConnection#issueFullGarbageCollection()}<br>
	 * {@link StorageConnection#issueFullFileCheck(StorageDataFileDissolvingEvaluator)}<br>
	 * {@link StorageConnection#issueFullCacheCheck(StorageEntityCacheEvaluator)}<br>
	 * in that order.
	 * <p>
	 * Depending on the passed functions, this call can do anything from cleaning/optimizing the storage a little to
	 * fully reorganize/optimize the storage files, clear the complete cache and making the storage virtually dormant
	 * until the next store.
	 *
	 * @param storageConnection the connection to the storage that shall be consolidated.
	 * @param fileDissolver     the function evaluating whether to dissolve a file.
	 *                          may be {@literal null} to indicate the use of the live configuration as a default.
	 * @param entityEvaluator   the function evaluating whether to clear an entity from the cache.
	 *                          may be {@literal null} to indicate the use of the live configuration as a default.
	 * @return the passed storageConnection instance.
	 */
	public static final <C extends StorageConnection> C consolidate(
		final C                                  storageConnection,
		final StorageDataFileDissolvingEvaluator fileDissolver    ,
		final StorageEntityCacheEvaluator        entityEvaluator
	)
	{
		/* calls must be done in that order to achieve highest (normal) effectivity:
		 * - first discard all unreachable entities.
		 * - then cleanup files depending on the resulting data occupation.
		 * - clean the entity cache.
		 */
		storageConnection.issueFullGarbageCollection();
		storageConnection.issueFullFileCheck(fileDissolver);
		storageConnection.issueFullCacheCheck(entityEvaluator);
		return storageConnection;
	}

	public static final <C extends StorageConnection> C consolidate(
		final C                                  storageConnection,
		final StorageDataFileDissolvingEvaluator fileDissolver
	)
	{
		return consolidate(storageConnection, fileDissolver, null);
	}

	public static final <C extends StorageConnection> C consolidate(
		final C                           storageConnection,
		final StorageEntityCacheEvaluator entityEvaluator
	)
	{
		return consolidate(storageConnection, null, entityEvaluator);
	}
	
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
