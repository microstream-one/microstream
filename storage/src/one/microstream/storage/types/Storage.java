package one.microstream.storage.types;

import static one.microstream.X.notNull;

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
	 * {@docLink StorageFileProvider#New()}
	 * 
	 * @return {@docLink StorageFileProvider#New()@return}
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
	 * {@docLink StorageFileProvider#New(File)}
	 * 
	 * @param storageDirectory {@docLink StorageFileProvider#New(File):}
	 * 
	 * @return {@docLink StorageFileProvider#New(File)@return}
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
	 * {@docLink StorageFileProvider#Builder()}
	 * 
	 * @return {@docLink StorageFileProvider#Builder()@return}
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
	 * {@docLink StorageConfiguration#New()}
	 * 
	 * @return {@docLink StorageConfiguration#New()@return}
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
	 * {@docLink StorageConfiguration#New(StorageFileProvider)}
	 * 
	 * @param fileProvider {@docLink StorageConfiguration#New(StorageFileProvider):}
	 * 
	 * @return {@docLink StorageConfiguration#New(StorageFileProvider)@return}
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
	 * {@docLink StorageConfiguration#Builder()}
	 * 
	 * @return {@docLink StorageConfiguration#Builder()@return}
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
	 * {@docLink StorageHousekeepingController#New()}
	 * 
	 * @return {@docLink StorageHousekeepingController#New()@return}
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
	 * {@docLink StorageHousekeepingController#New(long, long)}
	 * 
	 * @param housekeepingIntervalMs {@docLink StorageHousekeepingController#New(long, long):}	 *
	 * @param housekeepingTimeBudgetNs {@docLink StorageHousekeepingController#New(long, long):}
	 * 
	 * @return {@docLink StorageHousekeepingController#New(long, long)@return}
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
	 * {@docLink StorageEntityCacheEvaluator#New()}
	 * 
	 * @return {@docLink StorageEntityCacheEvaluator#New()@return}
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
	 * {@docLink StorageEntityCacheEvaluator#New(long)}
	 * 
	 * @param timeoutMs {@docLink StorageEntityCacheEvaluator#New(long):}
	 * 
	 * @return {@docLink StorageEntityCacheEvaluator#New(long)@return}
	 * 
	 * @throws {@docLink StorageEntityCacheEvaluator#New(long)@throws}
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
	 * {@docLink StorageEntityCacheEvaluator#New(long, long)}
	 * 
	 * @param timeoutMs {@docLink StorageEntityCacheEvaluator#New(long, long):}
	 * @param threshold {@docLink StorageEntityCacheEvaluator#New(long, long):}
	 * 
	 * @return {@docLink StorageEntityCacheEvaluator#New(long, long)@return}
	 * 
	 * @throws {@docLink StorageEntityCacheEvaluator#New(long, long)@throws}
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
	 * {@docLink StorageChannelCountProvider#New()}
	 * 
	 * @return {@docLink StorageChannelCountProvider#New()@return}
	 * 
	 * @see Storage#ChannelCountProvider(int)
	 * @see StorageChannelCountProvider#New()
	 */
	public static final StorageChannelCountProvider ChannelCountProvider()
	{
		return StorageChannelCountProvider.New();
	}

	/**
	 * {@docLink StorageChannelCountProvider#New(int)}
	 * 
	 * @param {@docLink StorageChannelCountProvider#New(int):}
	 * 
	 * @return {@docLink StorageChannelCountProvider#New(int)@return}
	 */
	public static final StorageChannelCountProvider ChannelCountProvider(final int channelCount)
	{
		return StorageChannelCountProvider.New(channelCount);
	}
	
	/**
	 * {@docLink StorageDataFileEvaluator#New()}
	 * 
	 * @return {@docLink StorageDataFileEvaluator#New()@return}
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
	 * {@docLink StorageDataFileEvaluator#New(int, int)}
	 * 
	 * @param fileMinimumSize {@docLink StorageDataFileEvaluator#New(int, int):}
	 * @param fileMaximumSize {@docLink StorageDataFileEvaluator#New(int, int):}
	 * 
	 * @return {@docLink StorageDataFileEvaluator#New(int, int)@return}
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
	 * {@docLink StorageDataFileEvaluator#New(int, int, double)}
	 * 
	 * @param fileMinimumSize {@docLink StorageDataFileEvaluator#New(int, int, double):}
	 * @param fileMaximumSize {@docLink StorageDataFileEvaluator#New(int, int, double):}
	 * 
	 * @return {@docLink StorageDataFileEvaluator#New(int, int, double)@return}
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
	 * {@docLink StorageDataFileEvaluator#New(int, int, double, boolean)}
	 * 
	 * @param fileMinimumSize {@docLink StorageDataFileEvaluator#New(int, int, double, boolean):}
	 * @param fileMaximumSize {@docLink StorageDataFileEvaluator#New(int, int, double, boolean):}
	 * @param minimumUseRatio {@docLink StorageDataFileEvaluator#New(int, int, double, boolean):}
	 * @param dissolveHeadfile {@docLink StorageDataFileEvaluator#New(int, int, double, boolean):}
	 * 
	 * @return dissolveHeadfile {@docLink StorageDataFileEvaluator#New(int, int, double, boolean)@return}
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
	
	// (28.05.2019 TM)FIXME: /!\ JavaDoc WIP.
	
	public static final StorageBackupSetup BackupSetup(
		final File backupDirectory
	)
	{
		return BackupSetup(backupDirectory.getPath());
	}
	
	public static final StorageBackupSetup BackupSetup(
		final String backupDirectoryIdentifier
	)
	{
		return StorageBackupSetup.New(
			Storage
			.FileProviderBuilder()
			.setBaseDirectory(backupDirectoryIdentifier)
			.createFileProvider()
		);
	}
	
	public static final StorageBackupSetup BackupSetup(
		final StorageFileProvider fileProvider
	)
	{
		return StorageBackupSetup.New(
			notNull(fileProvider)
		);
	}
			
	public static StorageLockFileSetup.Provider LockFileSetupProvider()
	{
		return StorageLockFileSetup.Provider();
	}
	
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
