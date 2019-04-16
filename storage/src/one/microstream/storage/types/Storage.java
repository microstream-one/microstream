package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;

public final class Storage
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////


	private static final long   ONE_MILLION                         = 1_000_000L;
	
	private static final long   TRANSACTIONS_FILE_NUMBER = -1;
	


	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/* (18.02.2019 TM)NOTE:
	 * It's a rather hacky solution to make a transactions file have a file number in the first place,
	 * but transaction files are bound to be replaced by inlined meta data in the future,
	 * so in order to not overcomplicate API now that would have to be consolidated later,
	 * transaction files are just numbered files with a specific fake number.
	 */
	public static final long transactionsFileNumber()
	{
		return TRANSACTIONS_FILE_NUMBER;
	}
	
	public static final boolean isTransactionFile(final StorageNumberedFile file)
	{
		return file.number() == TRANSACTIONS_FILE_NUMBER;
	}
	
	public static final boolean isDataFile(final StorageNumberedFile file)
	{
		return file.number() > 0;
	}
	
	public static final StorageFileProvider FileProvider()
	{
		return Storage.FileProviderBuilder()
			.createFileProvider()
		;
	}
	
	public static final StorageFileProvider FileProvider(final File mainDirectory)
	{
		return Storage.FileProviderBuilder()
			.setBaseDirectory(mainDirectory.getPath())
			.createFileProvider()
		;
	}
	
	public static final StorageFileProvider.Builder<?> FileProviderBuilder()
	{
		return StorageFileProvider.Builder();
	}

	public static final StorageConfiguration Configuration()
	{
		return StorageConfiguration.Builder()
			.createConfiguration()
		;
	}
	
	public static final StorageConfiguration Configuration(
		final StorageFileProvider fileProvider
	)
	{
		return StorageConfiguration.Builder()
			.setStorageFileProvider(fileProvider)
			.createConfiguration()
		;
	}

	public static final StorageConfiguration.Builder<?> ConfigurationBuilder()
	{
		return StorageConfiguration.Builder();
	}

	public static final StorageHousekeepingController HousekeepingController()
	{
		return StorageHousekeepingController.New();
	}
	
	public static final StorageHousekeepingController HousekeepingController(
		final long housekeepingIntervalMs      ,
		final long housekeepingTimeBudgetNs
	)
	{
		return StorageHousekeepingController.New(housekeepingIntervalMs, housekeepingTimeBudgetNs);
	}

	public static final StorageEntityCacheEvaluator EntityCacheEvaluator()
	{
		return StorageEntityCacheEvaluator.New();
	}

	public static final StorageChannelCountProvider ChannelCountProvider()
	{
		return StorageChannelCountProvider.New();
	}

	public static final StorageChannelCountProvider ChannelCountProvider(final int channelCount)
	{
		return StorageChannelCountProvider.New(channelCount);
	}

	public static final StorageEntityCacheEvaluator EntityCacheEvaluator(
		final long timeoutMs
	)
	{
		return StorageEntityCacheEvaluator.New(timeoutMs);
	}

	public static final StorageEntityCacheEvaluator EntityCacheEvaluator(
		final long timeoutMs,
		final long threshold
	)
	{
		return StorageEntityCacheEvaluator.New(timeoutMs, threshold);
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
	 * Calls {@link #consolidate(StorageConnection, StorageDataFileDissolvingEvaluator, StorageEntityCacheEvaluator)}
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
