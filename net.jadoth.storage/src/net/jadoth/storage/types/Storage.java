package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.File;

public final class Storage
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	// channels (work and storage distribution accross multiple threads with exclusive directories)
	private static final int    DEFAULT_CHANNELCOUNT                = 1              ;

	// file provider configuration

	// housekeeping time configuration (periodic time for garbage collection, cache and file consolidation)
	private static final long   DEFAULT_HOUSEKEEPING_INTERVAL       = 1_000          ; // hk interval (ms)
	private static final long   DEFAULT_HOUSEKEEPING_NANOTIMEBUDGET =    10_000_000  ; // hk time budget (ns)

	// entity caching configuration (applies per channel)
	private static final long   DEFAULT_CACHE_THRESHOLD             = 1_000_000_000  ; // ~1 GB default threshold
	private static final long   DEFAULT_CACHE_TIMEOUT               =    86_400_000  ; // 1 day default timeout

	// file housekeeping configuration (applies per channel). Relatively small default values
	private static final int    DEFAULT_MIN_FILESIZE                = 1 * 1024 * 1024; // 1 MB
	private static final int    DEFAULT_MAX_FILESIZE                = 8 * 1024 * 1024; // 8 MB
	private static final double DEFAULT_DISSOLVE_RATIO              = 0.75           ; // 75 %

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

	public static final StorageChannelCountProvider ChannelCountProvider()
	{
		return ChannelCountProvider(DEFAULT_CHANNELCOUNT);
	}

	public static final StorageHousekeepingController HousekeepingController()
	{
		return HousekeepingController(DEFAULT_HOUSEKEEPING_INTERVAL, DEFAULT_HOUSEKEEPING_NANOTIMEBUDGET);
	}

	public static final StorageEntityCacheEvaluator EntityCacheEvaluator()
	{
		return EntityCacheEvaluator(DEFAULT_CACHE_THRESHOLD, DEFAULT_CACHE_TIMEOUT);
	}

	public static final StorageEntityCacheEvaluator EntityCacheEvaluatorCustomThreshold(final long threshold)
	{
		return EntityCacheEvaluator(threshold, DEFAULT_CACHE_TIMEOUT);
	}

	public static final StorageEntityCacheEvaluator EntityCacheEvaluatorCustomTimeout(final long millisecondTimeout)
	{
		return EntityCacheEvaluator(DEFAULT_CACHE_THRESHOLD, millisecondTimeout);
	}

	public static final StorageDataFileEvaluator DataFileEvaluator()
	{
		return DataFileEvaluator(
			DEFAULT_MIN_FILESIZE,
			DEFAULT_MAX_FILESIZE,
			DEFAULT_DISSOLVE_RATIO
		);
	}

	public static final StorageChannelCountProvider ChannelCountProvider(final int channelCount)
	{
		return new StorageChannelCountProvider.Implementation(channelCount);
	}

	public static final StorageHousekeepingController HousekeepingController(
		final long housekeepingInterval      ,
		final long housekeepingNanoTimeBudget
	)
	{
		return new StorageHousekeepingController.Implementation(housekeepingInterval, housekeepingNanoTimeBudget);
	}

	public static final StorageEntityCacheEvaluator EntityCacheEvaluator(
		final long threshold         ,
		final long millisecondTimeout
	)
	{
		return StorageEntityCacheEvaluator.New(threshold, millisecondTimeout);
	}

	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int     minFileSize  ,
		final int     maxFileSize  ,
		final double  dissolveRatio
	)
	{
		return StorageDataFileEvaluator.New(minFileSize, maxFileSize, dissolveRatio);
	}

	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int     minFileSize     ,
		final int     maxFileSize     ,
		final double  dissolveRatio   ,
		final boolean dissolveHeadfile
	)
	{
		return StorageDataFileEvaluator.New(minFileSize, maxFileSize, dissolveRatio, dissolveHeadfile);
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
