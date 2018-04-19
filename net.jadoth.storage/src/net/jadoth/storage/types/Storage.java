package net.jadoth.storage.types;

import java.io.File;

import net.jadoth.collections.types.XGettingTable;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceRefactoringMappingProvider;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.util.chars.StringTable;
import net.jadoth.util.file.JadothFiles;

public final class Storage
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	// channels (work and storage distribution accross multiple threads with exclusive directories)
	private static final int    DEFAULT_CHANNELCOUNT                = 1              ;

	// file provider configuration
	private static final String DEFAULT_PARENT_DIRECTORY            = "storage"      ;
	private static final String DEFAULT_DIRECTORY_BASENAME          = "channel_"     ;
	private static final String DEFAULT_FILE_STORAGE_BASENAME       = "channel_"     ;
	private static final String DEFAULT_FILE_STORAGE_SUFFIX         = "dat"          ;
	private static final String DEFAULT_FILE_TRANSACTIONS_BASENAME  = "transactions_";
	private static final String DEFAULT_FILE_TRANSACTIONS_SUFFIX    = "sft"          ; // storage file transactions

	// housekeeping time configuration (periodic time for garbage collection, cache and file consolidation)
	private static final long   DEFAULT_HOUSEKEEPING_INTERVAL       = 1_000          ; // hk interval (ms)
	private static final long   DEFAULT_HOUSEKEEPING_NANOTIMEBUDGET =    10_000_000  ; // hk time budget (ns)

	// entity caching configuration (applies per channel)
	private static final long   DEFAULT_CACHE_THRESHOLD             = 1_000_000_000  ; // ~1 GB default threshold
	private static final long   DEFAULT_CACHE_TIMEOUT               =    86_400_000  ; // 1 day default timeout

	// file housekeeping configuration (applies per channel). Relatively small default values
	private static final int    DEFAULT_MIN_FILESIZE                = 1 * 1024 * 1024    ; // 1 MB
	private static final int    DEFAULT_MAX_FILESIZE                = 8 * 1024 * 1024    ; // 8 MB
	private static final double DEFAULT_DISSOLVE_RATIO              = 0.75               ; // 75 %

	private static final long   ONE_MILLION                         = 1_000_000L;


	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final String defaultDirectoryName()
	{
		return DEFAULT_PARENT_DIRECTORY;
	}

	public static final StorageFileProvider FileProvider(
		final File   parentDirectory    ,
		final String storageFileBaseName,
		final String storageFileSuffix
	)
	{
		return FileProvider(
			parentDirectory                   ,
			storageFileBaseName               ,
			storageFileBaseName               ,
			storageFileSuffix                 ,
			DEFAULT_FILE_TRANSACTIONS_BASENAME,
			DEFAULT_FILE_TRANSACTIONS_SUFFIX
		);
	}

	public static final StorageFileProvider FileProvider(
		final File   parentDirectory         ,
		final String channelDirectoryBaseName,
		final String storageFileBaseName     ,
		final String storageFileSuffix
	)
	{
		return FileProvider(
			parentDirectory                   ,
			channelDirectoryBaseName          ,
			storageFileBaseName               ,
			storageFileSuffix                 ,
			DEFAULT_FILE_TRANSACTIONS_BASENAME,
			DEFAULT_FILE_TRANSACTIONS_SUFFIX
		);
	}

	public static final StorageFileProvider FileProvider(
		final File   parentDirectory         ,
		final String channelDirectoryBaseName,
		final String storageFileBaseName     ,
		final String storageFileSuffix       ,
		final String transactionsFileBaseName,
		final String transactionsFileSuffix
	)
	{
		return new StorageFileProvider.Implementation(
			parentDirectory         ,
			channelDirectoryBaseName,
			storageFileBaseName     ,
			storageFileSuffix       ,
			transactionsFileBaseName,
			transactionsFileSuffix
		);
	}

	// (25.11.2013 TM)TODO: StorageConfigurationBuilder
	public static final StorageConfiguration Configuration(
		final StorageFileProvider           fileProvider          ,
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageDataFileEvaluator      fileDissolver         ,
		final StorageEntityCacheEvaluator   entityCacheEvaluator
	)
	{
		return new StorageConfiguration.Implementation(
			channelCountProvider   != null ? channelCountProvider   : ChannelCountProvider()  ,
			housekeepingController != null ? housekeepingController : HousekeepingController(),
			fileProvider           != null ? fileProvider           : FileProvider()          ,
			fileDissolver          != null ? fileDissolver          : DataFileEvaluator()     ,
			entityCacheEvaluator   != null ? entityCacheEvaluator   : EntityCacheEvaluator()
		);
	}

	public static final StorageConfiguration Configuration()
	{
		return Configuration(
			FileProvider()          ,
			ChannelCountProvider()  ,
			HousekeepingController(),
			DataFileEvaluator()     ,
			EntityCacheEvaluator()
		);
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

	public static final StorageFileProvider FileProvider(final File directory)
	{
		return FileProvider(
			directory                         ,
			DEFAULT_DIRECTORY_BASENAME        ,
			DEFAULT_FILE_STORAGE_BASENAME     ,
			DEFAULT_FILE_STORAGE_SUFFIX       ,
			DEFAULT_FILE_TRANSACTIONS_BASENAME,
			DEFAULT_FILE_TRANSACTIONS_SUFFIX
		);
	}

	public static final StorageFileProvider FileProvider()
	{
		return FileProvider(
			new File(DEFAULT_PARENT_DIRECTORY)
		);
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
		return new StorageEntityCacheEvaluator.Implementation(threshold, millisecondTimeout);
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

	public static final PersistenceRootResolver RootResolver(final String rootIdentifier, final Object rootInstance)
	{
		return PersistenceRootResolver.New(rootIdentifier, () -> rootInstance);
	}

	public static final PersistenceRootResolver RootResolver(final Object rootInstance)
	{
		return RootResolver("root", rootInstance);
	}
	
	public static final PersistenceRootResolver RootResolver(
		final String                                rootIdentifier    ,
		final Object                                rootInstance      ,
		final PersistenceRefactoringMappingProvider refactoringMapping
	)
	{
		return PersistenceRootResolver.Wrap(
			RootResolver(rootIdentifier, rootInstance),
			refactoringMapping
		);
	}
	
	public static final PersistenceRootResolver RootResolver(
		final Object                                rootInstance      ,
		final PersistenceRefactoringMappingProvider refactoringMapping
	)
	{
		return PersistenceRootResolver.Wrap(
			RootResolver(rootInstance),
			refactoringMapping
		);
	}
	
	public static final PersistenceRootResolver.Builder RootResolverBuilder()
	{
		return PersistenceRootResolver.Builder();
	}
	
	public static final PersistenceRefactoringMappingProvider RefactoringMapping(final File refactoringsFile)
	{
		return RefactoringMapping(
			readRefactoringMappings(refactoringsFile)
		);
	}
	
	public static final PersistenceRefactoringMappingProvider RefactoringMapping(
		final XGettingTable<String, String> refactoringMappings
	)
	{
		return PersistenceRefactoringMappingProvider.New(refactoringMappings);
	}
	
	public static XGettingTable<String, String> readRefactoringMappings(final File file)
	{
		// (19.04.2018 TM)EXCP: proper exception
		final String fileContent = JadothFiles.readStringFromFile(
			file,
			Persistence.standardCharset(),
			RuntimeException::new
		);
		final StringTable stringTable                     = StringTable.Static.parse(fileContent);
		final XGettingTable<String, String> keyValueTable = stringTable.toKeyValueTable(
			row ->
				row[0], // debuggability linebreak, do not rewrite!
			row ->
				row[1] // debuggability linebreak, do not rewrite!
		);
		
		return keyValueTable;
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
