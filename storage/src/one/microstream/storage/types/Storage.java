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
	 * Pseudo-constructor method to create a new {@link StorageFileProvider} instance with default values
	 * provided by {@link StorageFileProvider.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageFileProvider.Builder}.
	 * 
	 * @return {@docLink Storage#FileProvider()@return}
	 * 
	 * @see Storage#FileProvider(File)
	 * @see Storage#FileProviderBuilder()
	 * @see StorageFileProvider.Builder
	 * @see StorageFileProvider.Defaults
	 */
	public static final StorageFileProvider FileProvider()
	{
		return Storage.FileProviderBuilder()
			.createFileProvider()
		;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageFileProvider} instance with the passed {@link File}
	 * as the storage directory and defaults provided by {@link StorageFileProvider.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageFileProvider.Builder}.
	 * 
	 * @param storageDirectory the directory where storage will be located.
	 * 
	 * @return a new {@link StorageFileProvider} instance.
	 * 
	 * @see Storage#FileProvider()
	 * @see Storage#FileProviderBuilder()
	 * @see StorageFileProvider.Builder
	 * @see StorageFileProvider.Defaults
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
	 * Pseudo-constructor method to create a new {@link StorageConfiguration} instance
	 * using default instances for its parts and <code>null</code> as the {@link StorageBackupSetup} part.
	 * <p>
	 * For explanations and customizing values, see {@link StorageConfiguration.Builder}.
	 * 
	 * @return {@docLink Configuration#Configuration(StorageFileProvider)@return}
	 * 
	 * @see Storage#Configuration(StorageFileProvider)
	 * @see Storage#ConfigurationBuilder()
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration Configuration()
	{
		return StorageConfiguration.Builder()
			.createConfiguration()
		;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageConfiguration} instance
	 * using the passed {@link StorageFileProvider}, <code>null</code> as the {@link StorageBackupSetup} part
	 * and default instances for everything else.
	 * <p>
	 * For explanations and customizing values, see {@link StorageConfiguration.Builder}.
	 * 
	 * @return a new {@link StorageConfiguration} instance.
	 * 
	 * @see Storage#Configuration()
	 * @see Storage#ConfigurationBuilder()
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
	 * @param housekeepingIntervalMs {@docLink StorageHousekeepingController#New(long, long):}
	 * 
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
	
	// (27.05.2019 TM)FIXME: /!\ JavaDoc WIP.

	/**
	 * Pseudo-constructor method to create a new {@link StorageEntityCacheEvaluator} instance
	 * using the passed value.<br>
	 * <p>
	 * For explanations and customizing values, see {@link StorageEntityCacheEvaluator#New()}.
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
	 * <p>
	 * For explanations and customizing values, see {@link StorageEntityCacheEvaluator#New(long, long)}.
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
	 * using the default value of 1 (meaning a single storage thread).
	 * <p>
	 * For explanations and customizing values, see {@link StorageChannelCountProvider#New()}.
	 * 
	 * @return a new {@link StorageChannelCountProvider} instance.
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
	 * using the passed value to specify the amount of channels (threads and their exclusive storage sub-directory).
	 * <p>
	 * For explanations and customizing values, see {@link StorageChannelCountProvider#New(int)}.
	 * 
	 * @param channelCount the amount of channels (threads and their exclusive storage sub-directory).
	 * 
	 * @return a new {@link StorageChannelCountProvider} instance.
	 */
	public static final StorageChannelCountProvider ChannelCountProvider(final int channelCount)
	{
		return StorageChannelCountProvider.New(channelCount);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using default values specified by {@link StorageDataFileEvaluator.Defaults}<p>
	 * To specify custom values, see {@link Storage#DataFileEvaluator(int, int, double)}.<p>
	 * 
	 * @return a new {@link StorageDataFileEvaluator} instance using default values.
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
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed values.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int)}.
	 * 
	 * @param fileMinimumSize the minimum file size in bytes that a single storage file must have. Smaller files
	 *        will be dissolved by copying their content to the current head file and being deleted.
	 * 
	 * @param fileMaximumSize the maximum file size in bytes that a single storage file may have. Larger files
	 *        will be dissolved by copying their content to the current head file and being deleted.<br>
	 *        Note that a file can exceed this limit if it contains a single entity that exceeds the limit.
	 *        E.g. an int array with 10 million elements would be about 40 MB in size and would exceed a file size
	 *        limit of anything smaller than that.
	 * 
	 * @return a new {@link StorageDataFileEvaluator} instance.
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
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed values.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.
	 * 
	 * @param fileMinimumSize the minimum file size in bytes that a single storage file must have. Smaller files
	 *        will be dissolved by copying their content to the current head file and being deleted.
	 * 
	 * @param fileMaximumSize the maximum file size in bytes that a single storage file may have. Larger files
	 *        will be dissolved by copying their content to the current head file and being deleted.<br>
	 *        Note that a file can exceed this limit if it contains a single entity that exceeds the limit.
	 *        E.g. an int array with 10 million elements would be about 40 MB in size and would exceed a file size
	 *        limit of anything smaller than that.
	 * 
	 * @param minimumUseRatio the ratio (value in ]0.0;1.0]) of non-gap data contained in a storage file to prevent
	 *        the file from being dissolved. "Gap" data is anything that is not the latest version of an entity's data,
	 *        inluding older versions of an entity and "comment" bytes (a sequence of bytes beginning with its length
	 *        as a negative value length header).<br>
	 *        The closer this value is to 1.0 (100%), the less disk space is occupied by storage files, but the more
	 *        file dissolving (data transfers to new files) is required and vice versa.
	 * 
	 * @return a new {@link StorageDataFileEvaluator} instance.
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

	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int     fileMinimumSize ,
		final int     fileMaximumSize ,
		final double  minimumUseRatio ,
		final boolean dissolveHeadfile
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize, minimumUseRatio, dissolveHeadfile);
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
