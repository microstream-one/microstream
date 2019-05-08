package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;


/**
 * Static utility class containing static pseudo-constructor methods (indicated by a capital first letter)
 * and various utility methods.<p>
 * To setup and start a database, see the class "EmbeddedStorage".
 * 
 * @author TM
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
	 * Returns the dummy file number for transaction files, which is the value {@value Storage#TRANSACTIONS_FILE_NUMBER}.
	 * <p>
	 * Transaction files conceptually don't have a file number, but are subject to the {@link StorageNumberedFile}
	 * mechanics, so a dummy value is required. Since transaction files are planned to be replaced in the future
	 * by meta data inlined directly in the storage files, a dummy value like this is a preferable solution
	 * to restructuring.
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
	 * Checks if the passed {@link StorageNumberedFile} is a data file.
	 * 
	 * @param file the {@link StorageNumberedFile} to be checked.
	 * 
	 * @return whether the passed file is a data file.
	 * 
	 * @see Storage#isTransactionFile(StorageNumberedFile)
	 */
	public static final boolean isDataFile(final StorageNumberedFile file)
	{
		return file.number() > 0;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageFileProvider} instance with a default value
	 * for the storage directory, which is the folder named "storage" in the JVM's working directory.<p>
	 * To specify a custom storage directory, see Storage#FileProvider(File).<p>
	 * For full control over defining a storage's file locations and names, see {@link StorageFileProvider.Builder}.
	 * 
	 * @return a new {@link StorageFileProvider} instance with the default storage directory.
	 * 
	 * @see Storage#FileProvider(File)
	 * @see StorageFileProvider.Builder
	 */
	public static final StorageFileProvider FileProvider()
	{
		return Storage.FileProviderBuilder()
			.createFileProvider()
		;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageFileProvider} instance with the passed {@link File}
	 * as the storage directory<p>
	 * For full control over defining a storage's file locations and names, see {@link StorageFileProvider.Builder}.
	 * 
	 * @return a new {@link StorageFileProvider} instance with the passed {@link File} as the storage directory.
	 * 
	 * @see Storage#FileProvider(File)
	 * @see StorageFileProvider.Builder
	 */
	public static final StorageFileProvider FileProvider(final File storageDirectory)
	{
		/* (07.05.2019 TM)NOTE: string-based paths are planned to be replaced by an abstraction of
		 * storage files and directories that will replace any direct references to the file-system.
		 * Since that work is not completed, yet, the string approach has been used as a working temporary solution.
		 */
		return Storage.FileProviderBuilder()
			.setBaseDirectory(storageDirectory.getPath())
			.createFileProvider()
		;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageFileProvider.Builder} instance.
	 * 
	 * @return a new {@link StorageFileProvider.Builder} instance.
	 */
	public static final StorageFileProvider.Builder<?> FileProviderBuilder()
	{
		return StorageFileProvider.Builder();
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageConfiguration.Builder} instance
	 * using default values.
	 * 
	 * @return a new {@link StorageConfiguration} instance.
	 * 
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration Configuration()
	{
		return StorageConfiguration.Builder()
			.createConfiguration()
		;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageConfiguration.Builder} instance
	 * using the passed {@link StorageFileProvider} and default values for everything else.
	 * 
	 * @return a new {@link StorageConfiguration} instance using the passed {@link StorageFileProvider}.
	 * 
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration Configuration(
		final StorageFileProvider fileProvider
	)
	{
		return StorageConfiguration.Builder()
			.setStorageFileProvider(fileProvider)
			.createConfiguration()
		;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageConfiguration.Builder} instance.
	 * 
	 * @return a new {@link StorageConfiguration.Builder} instance.
	 */
	public static final StorageConfiguration.Builder<?> ConfigurationBuilder()
	{
		return StorageConfiguration.Builder();
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageHousekeepingController} instance
	 * using default values.<p>
	 * To specify custom values, see {@link Storage#HousekeepingController(long, long)}.<p>
	 * 
	 * @return a new {@link StorageHousekeepingController} instance using default values.
	 * 
	 * @see Storage#HousekeepingController(long, long)
	 * @see StorageHousekeepingController#New()
	 */
	public static final StorageHousekeepingController HousekeepingController()
	{
		return StorageHousekeepingController.New();
	}
	

	/**
	 * Pseudo-constructor method to create a new {@link StorageHousekeepingController} instance
	 * using the passed values.<p>
	 * The combination of these two values can be used to define how much percentage of the system's computing power
	 * shall be used for storage housekeeping. Example: 10 Million ns (= 10 ms) housekeeping budget every 1000 ms
	 * means (roughly) 1% of the computing power will be used for storage housekeeping.<p>
	 * Note that in an application where no store occures over a longer period of time will eventually complete
	 * all housekeeping tasks, which reduces the required computing power to 0. When the next store occurs, the
	 * housekeeping starts anew.<br>
	 * How long the housekeeping requires to complete depends on the computing power it is granted here, other
	 * configurations (like cache timeouts) and the amount of data that has to be managed.
	 * <p>
	 * See all "issue~" methods in {@link StorageConnection} for a way to call housekeeping explicitly and causing
	 * it to be executed completely.
	 * 
	 * 
	 * @param housekeepingIntervalMs the interval in milliseconds that the storage threads shall
	 *        execute their various housekeeping actions (like cache clearing checks, file consolidation, etc.)
	 * 
	 * @param housekeepingTimeBudgetNs the time budget in nanoseconds that each storage thread will use to perform
	 *        a housekeeping action. This is a best effort value, not a strictly reliable border value. This means
	 *        a housekeeping action can occasionally take slightly longer than specified here.
	 * 
	 * @return a new {@link StorageHousekeepingController} instance using default values.
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
	 * Pseudo-constructor method to create a new {@link StorageEntityCacheEvaluator} instance
	 * using default values.<p>
	 * To specify custom values, see {@link Storage#EntityCacheEvaluator(long, long)}.<p>
	 * 
	 * @return a new {@link StorageEntityCacheEvaluator} instance using default values.
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
	 * Pseudo-constructor method to create a new {@link StorageEntityCacheEvaluator} instance
	 * using the passed {@code timeoutMs} value and a default threshold value.<br>
	 * See {@link Storage#EntityCacheEvaluator(long, long)}.<p>
	 * 
	 * @param timeoutMs the time (in milliseconds) of not being read (the "age"), after which a particular
	 *        entity's data will be cleared from the Storage's internal cache.
	 * 
	 * @return a new {@link StorageEntityCacheEvaluator} instance.
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
	 * Pseudo-constructor method to create a new {@link StorageEntityCacheEvaluator} instance
	 * using the passed values.
	 * 
	 * @param timeoutMs the time (in milliseconds) of not being read (the "age"), after which a particular
	 *        entity's data will be cleared from the Storage's internal cache.
	 * 
	 * @param threshold an abstract value to evaluate the product of size and age of an entity in relation to the
	 *        current cache size in order to determine if the entity's data shall be cleared from the cache.
	 * 
	 * @return a new {@link StorageEntityCacheEvaluator} instance.
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
	 * Pseudo-constructor method to create a new {@link StorageChannelCountProvider} instance
	 * using the default value of 1 (meaning a single storage thread).<p>
	 * To specify a custom value, see {@link Storage#ChannelCountProvider(int)}.<p>
	 * 
	 * @return a new {@link StorageChannelCountProvider} instance defining a single-threaded storage.
	 * 
	 * @see Storage#ChannelCountProvider(int)
	 * @see StorageChannelCountProvider#New()
	 */
	public static final StorageChannelCountProvider ChannelCountProvider()
	{
		return StorageChannelCountProvider.New();
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageChannelCountProvider} instance
	 * using the passed value to specify the amount of channels (threads and their exclusive storage sub-directory).<p>
	 * 
	 * @param channelCount the amount of channels (threads and their exclusive storage sub-directory).
	 * 
	 * @return a new {@link StorageChannelCountProvider} instance using the passed {@code channelCount}.
	 */
	public static final StorageChannelCountProvider ChannelCountProvider(final int channelCount)
	{
		return StorageChannelCountProvider.New(channelCount);
	}
	
	public static final StorageDataFileEvaluator DataFileEvaluator()
	{
		return StorageDataFileEvaluator.New();
	}

	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int fileMinimumSize,
		final int fileMaximumSize
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize);
	}

	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int    fileMinimumSize,
		final int    fileMaximumSize,
		final double dissolveRatio
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize, dissolveRatio);
	}

	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int     fileMinimumSize ,
		final int     fileMaximumSize ,
		final double  dissolveRatio   ,
		final boolean dissolveHeadfile
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize, dissolveRatio, dissolveHeadfile);
	}
	
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
	\* Consolidates the storage system represented by the passed {@link StorageConnection} by calling<br>
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
	\*/
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
	
	

	private Storage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
