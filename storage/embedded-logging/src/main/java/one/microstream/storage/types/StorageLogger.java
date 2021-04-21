package one.microstream.storage.types;

import java.util.Iterator;
import java.util.ServiceLoader;

import one.microstream.chars.VarString;
import one.microstream.logging.types.Logger;
import one.microstream.logging.types.LoggerFactory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.reference.Lazy;
import one.microstream.reference.LazyReferenceManager;
import one.microstream.reference.LazyReferenceManagerLogging;
import one.microstream.storage.embedded.types.EmbeddedStorageConnectionFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

public interface StorageLogger
{
	public void embeddedStorageManager_beforeStart(EmbeddedStorageManager manager);

	public void embeddedStorageManager_afterStart(EmbeddedStorageManager manager);

	public void embeddedStorageManager_beforeShutdown(EmbeddedStorageManager manager);

	public void embeddedStorageManager_afterShutdown(EmbeddedStorageManager manager);
	
	
	public void embeddedStorageFoundation_beforeCreateEmbeddedStorageManager();

	public void embeddedStorageFoundation_afterCreateEmbeddedStorageManager(EmbeddedStorageManager embeddedStorageManager);
	
	public void embeddedStorageFoundation_beforeCreateStorageSystem();

	public void embeddedStorageFoundation_afterCreateStorageSystem(StorageSystem storageSystem);
	
	
	public void embeddedStorageConnectionFoundation_beforeCreateConnection();

	public void embeddedStorageConnectionFoundation_afterCreateConnection(StorageConnection storageConnection);
	
	public void embeddedStorageConnectionFoundation_beforeCreatePersistenceManager();

	public void embeddedStorageConnectionFoundation_afterPersistenceManager(PersistenceManager<Binary> persistenceManager);
	
		
	public void lazyReferenceManager_beforeStart(LazyReferenceManagerLogging  lazyReferenceManager);

	public void lazyReferenceManager_afterStart(LazyReferenceManagerLogging lazyReferenceManager);

	public void lazyReferenceManager_beforeStop(LazyReferenceManagerLogging lazyReferenceManager);

	public void lazyReferenceManager_afterStop(LazyReferenceManagerLogging lazyReferenceManager);
	
	public void lazyReferenceManager_beforerRegister(Lazy<?> lazyReference);
	
	
	public void lazyChecker_beginCheckCycle();

	public void lazyChecker_beginCheck(Lazy<?> lazyReference);
	
	public void lazyChecker_afterCheck(Lazy<?> lazyReference, boolean checkResult);

	public void lazyChecker_endCheckCycle();
	

	public void storageThreadProvider_beforeProvideChannelThread(StorageChannel storageChannel, StorageThreadNameProvider threadNameProvider);
	
	public void storageThreadProvider_afterProvideChannelThread(final StorageChannel storageChannel, Thread thread);
	
	public void storageBackupThreadProvider_beforeProvideBackupThread(StorageBackupHandler backupHandler,	StorageThreadNameProvider threadNameProvider);

	public void storageBackupThreadProvider_afterProvideBackupThread(StorageBackupHandler backupHandler, Thread thread);

	public void storageLockFileManagerThreadProvider_beforeProvideLockFileManagerThread(StorageLockFileManager lockFileManager, StorageThreadNameProvider threadNameProvider);

	public void storageLockFileManager_afterProvideLockFileManagerThread(StorageLockFileManager lockFileManager, Thread thread);
	
	
	public void storageSystem_beforeCreateChannels(int channelCount);
	
	public void storageSystem_afterCreateChannels(final int channelCount);
	
	
	public void storageChannel_beforeRun(StorageChannel storageChannel);

	public void storageChannel_afterRun(StorageChannel storageChannel);
	
	
	public void storageDataFileEvaluator_beforeNeedsDissolving(StorageLiveDataFile storageFile);

	public void storageDataFileEvaluator_afterNeedsDissolving(StorageLiveDataFile storageFile, boolean needsDissolving);
	
	
	public void logConfiguration(StorageConfiguration configuration);

	
	public void storageHousekeepingBroker_beforePerformFileCleanupCheck(StorageHousekeepingExecutor executor, long nanoTimeBudget);

	public void storageHousekeepingBroker_afterPerformFileCleanupCheck(StorageHousekeepingExecutor executor, boolean result);
	
	public void storageHousekeepingBroker_beforePerformIssuedFileCleanupCheck(StorageHousekeepingExecutor executor,	long nanoTimeBudget);

	public void storageHousekeepingBroker_afterPerformIssuedFileCleanupCheck(StorageHousekeepingExecutor executor, boolean result);

	public void storageHousekeepingBroker_beforePerformIssuedGarbageCollection(StorageHousekeepingExecutor executor, long nanoTimeBudget);

	public void storageHousekeepingBroker_afterPerformIssuedGarbageCollection(StorageHousekeepingExecutor executor,	boolean result);

	public void storageHousekeepingBroker_beforePerformIssuedEntityCacheCheck(StorageHousekeepingExecutor executor,	long nanoTimeBudget);

	public void storageHousekeepingBroker_afterePerformIssuedEntityCacheCheck(StorageHousekeepingExecutor executor,	boolean result);

	public void storageHousekeepingBroker_beforePerformGarbageCollection(StorageHousekeepingExecutor executor, long nanoTimeBudget);

	public void storageHousekeepingBroker_afterPerformGarbageCollection(StorageHousekeepingExecutor executor, boolean result);

	public void storageHousekeepingBroker_beforePerformEntityCacheCheck(StorageHousekeepingExecutor executor, long nanoTimeBudget);

	public void storageHousekeepingBroker_afterPerformEntityCacheCheck(StorageHousekeepingExecutor executor, boolean result);

	
	public void storageEntityCacheEvaluator_afterClearEntityCache_true(StorageEntity entity);
	
	
	public void storageEventLogger_afterLogChannelProcessingDisabled(StorageChannel channel);
	
	public void storageEventLogger_afterLogChannelStoppedWorking(StorageChannel channel);

	public void storageEventLogger_afterLogDisruption();

	public void storageEventLogger_afterLogDisruption(StorageChannel channel, Throwable t);

	public void storageEventLogger_afterLogLiveCheckComplete(StorageEntityCache<?> entityCache);

	public void storageEventLogger_afterLogGarbageCollectorSweepingComplete(StorageEntityCache<?> entityCache);

	public void storageEventLogger_afterLogGarbageCollectorNotNeeded();

	public void storageEventLogger_afterLogGarbageCollectorCompletedHotPhase(long gcHotGeneration, long lastGcHotCompletion);

	public void storageEventLogger_afterLogGarbageCollectorCompleted(long gcColdGeneration, long lastGcColdCompletion);

	public void storageEventLogger_afterLogGarbageCollectorEncounteredZombieObjectId(long objectId);
	
	
	public void storageBackupHandler_beforeInitialize(int channelIndex);

	public void storageBackupHandler_afterInitialize(int channelIndex);

	public void storageBackupHandler_beforeSynchronize(StorageInventory storageInventory);

	public void storageBackupHandler_afterSynchronize(StorageInventory storageInventory);

	public void storageBackupHandler_beforeCopyFilePart(StorageLiveChannelFile<?> sourceFile, long sourcePosition, long length);

	public void storageBackupHandler_afterCopyFilePart(StorageLiveChannelFile<?> sourceFile, long sourcePosition, long length);

	public void storageBackupHandler_beforeTruncateFile(StorageLiveChannelFile<?> file, long newLength);

	public void storageBackupHandler_afterTruncateFile(StorageLiveChannelFile<?> file, long newLength);

	public void storageBackupHandler_beforeDeleteFile(StorageLiveChannelFile<?> file);

	public void storageBackupHandler_afterDeleteFile(StorageLiveChannelFile<?> file);

	public void storageBackupHandler_beforeStart();

	public void storageBackupHandler_afterStart(StorageBackupHandler storageBackupHandler);

	public void storageBackupHandler_beforeStop();

	public void storageBackupHandler_afterStop(StorageBackupHandler storageBackupHandler);

	public void storageBackupHandler_beforeSetRunning(boolean running);

	public void storageBackupHandler_afterSetRunning(StorageBackupHandler storageBackupHandler);

	public void storageBackupHandler_beforeRun();

	public void storageBackupHandler_afterRun();
	

	public static StorageLogger set(final StorageLogger globalStorageLogger)
	{
		return Static.set(globalStorageLogger);
	}

	public static StorageLogger get()
	{
		return Static.get();
	}


	public static class Static
	{
		private static StorageLogger globalStorageLogger = load();

		static synchronized StorageLogger set(final StorageLogger globalStorageLogger)
		{
			final StorageLogger old = Static.globalStorageLogger;
			Static.globalStorageLogger = globalStorageLogger;
			return old;
		}

		static synchronized StorageLogger get()
		{
			if(globalStorageLogger == null)
			{
				globalStorageLogger = StorageLogger.New();
			}
			return globalStorageLogger;
		}

		private static StorageLogger load()
		{
			final Iterator<StorageLoggerProvider> iterator = ServiceLoader
				.load(StorageLoggerProvider.class)
				.iterator();
			return iterator.hasNext()
				? iterator.next().provideStorageLogger()
				: null
			;
		}

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		* Dummy constructor to prevent instantiation of this static-only utility class.
		*
		* @throws UnsupportedOperationException
		*/
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}

	}


	public static StorageLogger New()
	{
		return new Default();
	}


	public static StorageLogger NoOp()
	{
		return new NoOp();
	}


	public static abstract class Abstract implements StorageLogger
	{
		protected Abstract()
		{
			super();
		}

		@Override
		public void embeddedStorageManager_beforeStart(final EmbeddedStorageManager manager)
		{
			// no-op
		}

		@Override
		public void embeddedStorageManager_afterStart(final EmbeddedStorageManager manager)
		{
			// no-op
		}

		@Override
		public void embeddedStorageManager_beforeShutdown(final EmbeddedStorageManager manager)
		{
			// no-op
		}

		@Override
		public void embeddedStorageManager_afterShutdown(final EmbeddedStorageManager manager)
		{
			// no-op
		}
		
		@Override
		public void embeddedStorageFoundation_beforeCreateEmbeddedStorageManager()
		{
			//no-op
		}

		@Override
		public void embeddedStorageFoundation_afterCreateEmbeddedStorageManager(final EmbeddedStorageManager embeddedStorageManager)
		{
			//no-op
		}
		
		@Override
		public void embeddedStorageFoundation_beforeCreateStorageSystem()
		{
			//no-op
		}

		@Override
		public void embeddedStorageFoundation_afterCreateStorageSystem(final StorageSystem storageSystem)
		{
			//no-op
		}
		
		@Override
		public void embeddedStorageConnectionFoundation_beforeCreateConnection()
		{
			// no-op
		}

		@Override
		public void embeddedStorageConnectionFoundation_afterCreateConnection(final StorageConnection storageConnection)
		{
			// no-op
		}
		
		@Override
		public void embeddedStorageConnectionFoundation_beforeCreatePersistenceManager()
		{
			// no-op
		}

		@Override
		public void embeddedStorageConnectionFoundation_afterPersistenceManager(final PersistenceManager<Binary> persistenceManager)
		{
			//no-op
		}

		
		
		@Override
		public void lazyReferenceManager_beforeStart(final LazyReferenceManagerLogging  lazyReferenceManager)
		{
			//no-op
		}

		@Override
		public void lazyReferenceManager_afterStart(final LazyReferenceManagerLogging lazyReferenceManager)
		{
			//no-op
		}

		@Override
		public void lazyReferenceManager_beforeStop(final LazyReferenceManagerLogging lazyReferenceManager)
		{
			//no-op
		}

		@Override
		public void lazyReferenceManager_afterStop(final LazyReferenceManagerLogging lazyReferenceManager)
		{
			//no-op
		}
		
		@Override
		public void lazyReferenceManager_beforerRegister(final Lazy<?> lazyReference)
		{
			//no-op
		}
		
		@Override
		public void lazyChecker_beginCheckCycle()
		{
			//no-op
		}

		@Override
		public void lazyChecker_beginCheck(final Lazy<?> lazyReference)
		{
			//no-op
		}
		
		@Override
		public void lazyChecker_afterCheck(final Lazy<?> lazyReference, final boolean checkResult)
		{
			//no-op
		}

		@Override
		public void lazyChecker_endCheckCycle()
		{
			//no-op
		}

		@Override
		public void storageThreadProvider_beforeProvideChannelThread(final StorageChannel storageChannel, final StorageThreadNameProvider threadNameProvider)
		{
			//no-op
		}

		@Override
		public void storageThreadProvider_afterProvideChannelThread(final StorageChannel storageChannel, final Thread thread)
		{
			//no-op
		}
		
		@Override
		public void storageBackupThreadProvider_beforeProvideBackupThread(final StorageBackupHandler backupHandler,	final StorageThreadNameProvider threadNameProvider)
		{
			//no-op
		}

		@Override
		public void storageBackupThreadProvider_afterProvideBackupThread(final StorageBackupHandler backupHandler, final Thread thread)
		{
			//no-op
		}

		@Override
		public void storageLockFileManagerThreadProvider_beforeProvideLockFileManagerThread(final StorageLockFileManager lockFileManager, final StorageThreadNameProvider threadNameProvider)
		{
			//no-op
		}

		@Override
		public void storageLockFileManager_afterProvideLockFileManagerThread(final StorageLockFileManager lockFileManager, final Thread thread)
		{
			//no-op
		}
		
		@Override
		public void storageSystem_beforeCreateChannels(final int channelCount)
		{
			//no-op
		}
		
		@Override
		public void storageSystem_afterCreateChannels(final int channelCount)
		{
			//no-op
		}
		
		@Override
		public void storageChannel_beforeRun(final StorageChannel storageChannel)
		{
			//no-op
		}

		@Override
		public void storageChannel_afterRun(final StorageChannel storageChannel)
		{
			//no-op
		}
		
		@Override
		public void storageDataFileEvaluator_beforeNeedsDissolving(final StorageLiveDataFile storageFile)
		{
			//no-op
		}

		@Override
		public void storageDataFileEvaluator_afterNeedsDissolving(final StorageLiveDataFile storageFile, final boolean needsDissolving)
		{
			//no-op
		}
		
		@Override
		public void logConfiguration(final StorageConfiguration configuration)
		{
			//no-op
		}
		
		@Override
		public void storageHousekeepingBroker_beforePerformFileCleanupCheck(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_afterPerformFileCleanupCheck(final StorageHousekeepingExecutor executor, final boolean result)
		{
			//no-op
		}
		
		@Override
		public void storageHousekeepingBroker_beforePerformIssuedFileCleanupCheck(final StorageHousekeepingExecutor executor,	final long nanoTimeBudget)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_afterPerformIssuedFileCleanupCheck(final StorageHousekeepingExecutor executor, final boolean result)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_beforePerformIssuedGarbageCollection(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_afterPerformIssuedGarbageCollection(final StorageHousekeepingExecutor executor,	final boolean result)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_beforePerformIssuedEntityCacheCheck(final StorageHousekeepingExecutor executor,	final long nanoTimeBudget)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_afterePerformIssuedEntityCacheCheck(final StorageHousekeepingExecutor executor,	final boolean result)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_beforePerformGarbageCollection(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_afterPerformGarbageCollection(final StorageHousekeepingExecutor executor, final boolean result)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_beforePerformEntityCacheCheck(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			//no-op
		}

		@Override
		public void storageHousekeepingBroker_afterPerformEntityCacheCheck(final StorageHousekeepingExecutor executor, final boolean result)
		{
			//no-op
		}
		
		@Override
		public void storageEntityCacheEvaluator_afterClearEntityCache_true(final StorageEntity entity)
		{
			//no-op
		}

		@Override
		public void storageEventLogger_afterLogChannelProcessingDisabled(final StorageChannel channel)
		{
			//no-op
		}
		
		@Override
		public void storageEventLogger_afterLogChannelStoppedWorking(final StorageChannel channel)
		{
			//no-op
		}

		@Override
		public void storageEventLogger_afterLogDisruption()
		{
			//no-op
		}

		@Override
		public void storageEventLogger_afterLogDisruption(final StorageChannel channel, final Throwable t)
		{
			//no-op
		}

		@Override
		public void storageEventLogger_afterLogLiveCheckComplete(final StorageEntityCache<?> entityCache)
		{
			//no-op
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorSweepingComplete(final StorageEntityCache<?> entityCache)
		{
			//no-op
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorNotNeeded()
		{
			//no-op
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorCompletedHotPhase(final long gcHotGeneration, final long lastGcHotCompletion)
		{
			//no-op
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorCompleted(final long gcColdGeneration, final long lastGcColdCompletion)
		{
			//no-op
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorEncounteredZombieObjectId(final long objectId)
		{
			//no-op
		}
		
		@Override
		public void storageBackupHandler_beforeInitialize(final int channelIndex)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_afterInitialize(final int channelIndex)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_beforeSynchronize(final StorageInventory storageInventory)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_afterSynchronize(final StorageInventory storageInventory)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_beforeCopyFilePart(final StorageLiveChannelFile<?> sourceFile, final long sourcePosition, final long length)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_afterCopyFilePart(final StorageLiveChannelFile<?> sourceFile, final long sourcePosition, final long length)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_beforeTruncateFile(final StorageLiveChannelFile<?> file, final long newLength)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_afterTruncateFile(final StorageLiveChannelFile<?> file, final long newLength)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_beforeDeleteFile(final StorageLiveChannelFile<?> file)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_afterDeleteFile(final StorageLiveChannelFile<?> file)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_beforeStart()
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_afterStart(final StorageBackupHandler storageBackupHandler)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_beforeStop()
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_afterStop(final StorageBackupHandler storageBackupHandler)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_beforeSetRunning(final boolean running)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_afterSetRunning(final StorageBackupHandler storageBackupHandler)
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_beforeRun()
		{
			//no-op
		}

		@Override
		public void storageBackupHandler_afterRun()
		{
			//no-op
		}
	}


	public static class NoOp extends Abstract
	{
		NoOp()
		{
			super();
		}
	}


	public static class Default extends Abstract
	{
		private final Logger loggerEmbeddedStorageManager              = LoggerFactory.get().forClass(EmbeddedStorageManager.class);
		private final Logger loggerEmbeddedStorageConnectionFoundation = LoggerFactory.get().forClass(EmbeddedStorageConnectionFoundation.class);
		private final Logger loggerLazyReferenzeManager                = LoggerFactory.get().forClass(LazyReferenceManager.class);
		private final Logger loggerStorageHousekeeping                 = LoggerFactory.get().forName("one.microstream.StorageHousekeeping");
		private final Logger loggerStorageEventsLogger                 = LoggerFactory.get().forClass(StorageEventLogger.class);
		private final Logger loggerStorageBackupHandler                = LoggerFactory.get().forClass(StorageBackupHandler.class);
		private final String logTag                                    = "microstream_storage";
		

		protected Default()
		{
			super();
		}

		public Logger loggerEmbeddedStorageManager()
		{
			return this.loggerEmbeddedStorageManager;
		}

		public String logTag()
		{
			return this.logTag;
		}

		@Override
		public void embeddedStorageManager_beforeStart(final EmbeddedStorageManager manager)
		{
			this.loggerEmbeddedStorageManager.info()
				.withTag(this.logTag)
				.log("Starting EmbeddedStorageManager: %s", manager)
			;
		}

		@Override
		public void embeddedStorageManager_afterStart(final EmbeddedStorageManager manager)
		{
			this.loggerEmbeddedStorageManager.info()
				.withTag(this.logTag)
				.log("EmbeddedStorageManager started: %s", manager)
			;
		}

		@Override
		public void embeddedStorageManager_beforeShutdown(final EmbeddedStorageManager manager)
		{
			this.loggerEmbeddedStorageManager.info()
				.withTag(this.logTag)
				.log("Shutting down EmbeddedStorageManager: %s", manager)
			;
		}

		@Override
		public void embeddedStorageManager_afterShutdown(final EmbeddedStorageManager manager)
		{
			this.loggerEmbeddedStorageManager.info()
				.withTag(this.logTag)
				.log("EmbeddedStorageManager shut down: %s", manager)
			;
		}

		
				
		@Override
		public void embeddedStorageConnectionFoundation_beforeCreateConnection()
		{
			this.loggerEmbeddedStorageConnectionFoundation.debug()
				.withTag(this.logTag)
				.log("Creating StorageConnection")
			;
			
		}

		@Override
		public void embeddedStorageConnectionFoundation_afterCreateConnection(final StorageConnection storageConnection)
		{
			this.loggerEmbeddedStorageConnectionFoundation.debug()
				.withTag(this.logTag)
				.log("Created StorageConnection: %s" ,  storageConnection)
			;
			
		}
		
		@Override
		public void embeddedStorageConnectionFoundation_beforeCreatePersistenceManager()
		{
			this.loggerEmbeddedStorageConnectionFoundation.debug()
				.withTag(this.logTag)
				.log("Creating PersistenceManager")
			;
		}

		@Override
		public void embeddedStorageConnectionFoundation_afterPersistenceManager(final PersistenceManager<Binary> persistenceManager)
		{
			this.loggerEmbeddedStorageConnectionFoundation.debug()
				.withTag(this.logTag)
				.log("Created PersistenceManager: %s" ,  persistenceManager)
			;
		}
		
		@Override
		public void embeddedStorageFoundation_beforeCreateStorageSystem()
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Creating StorageSystem")
			;
			
		}

		@Override
		public void embeddedStorageFoundation_afterCreateStorageSystem(final StorageSystem storageSystem)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Created StorageSystem %s", storageSystem)
			;
		}

		@Override
		public void embeddedStorageFoundation_beforeCreateEmbeddedStorageManager()
		{
			this.loggerEmbeddedStorageConnectionFoundation.debug()
				.withTag(this.logTag)
				.log("Creating EmbeddedStorageManager")
			;
		}
		
		@Override
		public void embeddedStorageFoundation_afterCreateEmbeddedStorageManager(final EmbeddedStorageManager embeddedStorageManager)
		{
			this.loggerEmbeddedStorageConnectionFoundation.debug()
				.withTag(this.logTag)
				.log("Created PersistenceManager: %s" ,  embeddedStorageManager)
			;
		}
					
		@Override
		public void lazyReferenceManager_beforeStart(final LazyReferenceManagerLogging  lazyReferenceManager)
		{
			this.loggerLazyReferenzeManager.debug()
				.withTag(this.logTag)
				.log("Starting LazyReferenceManager: %s" , lazyReferenceManager)
			;
		}

		@Override
		public void lazyReferenceManager_afterStart(final LazyReferenceManagerLogging lazyReferenceManager)
		{
			this.loggerLazyReferenzeManager.debug()
				.withTag(this.logTag)
				.log("Started LazyReferenceManager: %s" , lazyReferenceManager)
			;
		}
		
		@Override
		public void lazyReferenceManager_beforeStop(final LazyReferenceManagerLogging lazyReferenceManager)
		{
			this.loggerLazyReferenzeManager.debug()
				.withTag(this.logTag)
				.log("Stopping LazyReferenceManager: %s" , lazyReferenceManager)
			;
		}

		@Override
		public void lazyReferenceManager_afterStop(final LazyReferenceManagerLogging lazyReferenceManager)
		{
			this.loggerLazyReferenzeManager.debug()
				.withTag(this.logTag)
				.log("Stopped LazyReferenceManager: %s" , lazyReferenceManager)
			;
		}
		
		@Override
		public void lazyReferenceManager_beforerRegister(final Lazy<?> lazyReference)
		{
			this.loggerLazyReferenzeManager.debug()
				.withTag(this.logTag)
				.log("Registering lazy reference: %s", lazyReference)
			;
		}
		
		@Override
		public void lazyChecker_beginCheckCycle()
		{
			this.loggerLazyReferenzeManager.trace()
				.withTag(this.logTag)
				.log("Begin lazy reference check cycle")
			;
		}

		@Override
		public void lazyChecker_beginCheck(final Lazy<?> lazyReference)
		{
			this.loggerLazyReferenzeManager.trace()
				.withTag(this.logTag)
				.log("Checking : %s", lazyReference)
			;
		}

		@Override
		public void lazyChecker_afterCheck(final Lazy<?> lazyReference, final boolean checkResult)
		{
			this.loggerLazyReferenzeManager.trace()
				.withTag(this.logTag)
				.log("Checking : %s, result = %s", lazyReference, checkResult)
			;
		}
		
		@Override
		public void lazyChecker_endCheckCycle()
		{
			this.loggerLazyReferenzeManager.trace()
				.withTag(this.logTag)
				.log("End lazy reference check cycle")
			;
		}
		
		@Override
		public void storageThreadProvider_beforeProvideChannelThread(final StorageChannel storageChannel, final StorageThreadNameProvider threadNameProvider)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Providing channel thread for storage channel %s using thread name provider %s", storageChannel, threadNameProvider)
			;
		}

		@Override
		public void storageThreadProvider_afterProvideChannelThread(final StorageChannel storageChannel, final Thread thread)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Provided channel thread %s for storage channel %s", thread, storageChannel)
			;
		}
		
		@Override
		public void storageBackupThreadProvider_beforeProvideBackupThread(final StorageBackupHandler backupHandler,	final StorageThreadNameProvider threadNameProvider)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Providing backup thread for backup handler %s using thread name provider %s", backupHandler, threadNameProvider)
			;
		}

		@Override
		public void storageBackupThreadProvider_afterProvideBackupThread(final StorageBackupHandler backupHandler, final Thread thread)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Provided backup thread %s for backup handler %s ", thread, backupHandler)
			;
		}

		@Override
		public void storageLockFileManagerThreadProvider_beforeProvideLockFileManagerThread(final StorageLockFileManager lockFileManager, final StorageThreadNameProvider threadNameProvider)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Providing lock file manager thread for lock file manager %s using thread name provider %s", lockFileManager, threadNameProvider)
			;
		}

		@Override
		public void storageLockFileManager_afterProvideLockFileManagerThread(final StorageLockFileManager lockFileManager, final Thread thread)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Provided lock file manager thread %s for lock file manager %s", thread, lockFileManager)
			;
		}
			
		@Override
		public void storageSystem_beforeCreateChannels(final int channelCount)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Creating %d storage channel(s)", channelCount)
			;
		}
		
		@Override
		public void storageChannel_beforeRun(final StorageChannel storageChannel)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Starting thread for storage channel %s", storageChannel)
			;
		}

		@Override
		public void storageChannel_afterRun(final StorageChannel storageChannel)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Stopped thread for storage channel %s", storageChannel)
			;
		}
		
		
		@Override
		public void storageSystem_afterCreateChannels(final int channelCount)
		{
			this.loggerEmbeddedStorageManager.debug()
				.withTag(this.logTag)
				.log("Created %d storage channel(s)", channelCount)
			;
		}
			
		@Override
		public void logConfiguration(final StorageConfiguration configuration)
		{
			final VarString output = VarString.New(2048);
			output.lf()
				.add("Configuration:").lf()
				.add("Storage location              : ").add(configuration.fileProvider().getStorageLocationIdentifier()).lf()
				.add("Backup location               : ").add(
					configuration.backupSetup() == null ? "not available" :
					configuration.backupSetup().backupFileProvider().baseDirectory().toPathString()
					).lf()
				.add("Read only storage mode        : ").add(!configuration.fileProvider().fileSystem().isWritable()).lf()
				.add("Minimum file size             : ").add(configuration.dataFileEvaluator().fileMinimumSize()).add(" bytes").lf()
				.add("Maximum file size             : ").add(configuration.dataFileEvaluator().fileMaximumSize()).add(" bytes").lf()
				.add("Channel count                 : ").add(configuration.channelCountProvider().getChannelCount()).lf()
				.add("House keeping interval        : ").add(configuration.housekeepingController().housekeepingIntervalMs()).add(" ms").lf()
				.add("House keeping nano time budget: ").add(configuration.housekeepingController().housekeepingTimeBudgetNs()).add(" ns").lf()
				.add("Garbage collection time budget: ").add(configuration.housekeepingController().garbageCollectionTimeBudgetNs()).add(" ns").lf()
				.add("Live check time budget        : ").add(configuration.housekeepingController().liveCheckTimeBudgetNs()).add(" ns").lf()
				.add("File check time budget        : ").add(configuration.housekeepingController().fileCheckTimeBudgetNs()).add(" ns").lf()
				
			;
						
			final StorageEntityCacheEvaluator cacheEvaluator = configuration.entityCacheEvaluator();
			if(cacheEvaluator instanceof StorageEntityCacheEvaluator.Default)
			{
				output
					.add("cache threshold               : ").add(((StorageEntityCacheEvaluator.Default) cacheEvaluator).threshold()).add(" bytes").lf()
					.add("cache timeout                 : ").add(((StorageEntityCacheEvaluator.Default) cacheEvaluator).timeout()).add(" ms").lf()
				;
			}
			
			this.loggerEmbeddedStorageConnectionFoundation.info()
				.withTag(this.logTag)
				.log(output.toString())
			;
		}
		
		@Override
		public void storageDataFileEvaluator_beforeNeedsDissolving(final StorageLiveDataFile storageFile)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Check if %s needs dissolving", storageFile)
			;
		}

		@Override
		public void storageDataFileEvaluator_afterNeedsDissolving(final StorageLiveDataFile storageFile, final boolean needsDissolving)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("%s needs dissolving: %s", storageFile, needsDissolving)
			;
		}
		
		@Override
		public void storageHousekeepingBroker_beforePerformFileCleanupCheck(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Performing file cleanup check %s time budget %d ns", executor, nanoTimeBudget)
			;
		}

		@Override
		public void storageHousekeepingBroker_afterPerformFileCleanupCheck(final StorageHousekeepingExecutor executor, final boolean result)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("File cleanup check %s result: %s", executor, result)
			;
		}
		
		@Override
		public void storageHousekeepingBroker_beforePerformIssuedFileCleanupCheck(final StorageHousekeepingExecutor executor,	final long nanoTimeBudget)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Performing issued file cleanup check %s time budget %d ns", executor, nanoTimeBudget)
			;
		}

		@Override
		public void storageHousekeepingBroker_afterPerformIssuedFileCleanupCheck(final StorageHousekeepingExecutor executor, final boolean result)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Issued file cleanup check %s result: %s", executor, result)
			;
		}

		@Override
		public void storageHousekeepingBroker_beforePerformIssuedGarbageCollection(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Performing issued garbage collection %s time budget %d ns", executor, nanoTimeBudget)
			;
		}

		@Override
		public void storageHousekeepingBroker_afterPerformIssuedGarbageCollection(final StorageHousekeepingExecutor executor,	final boolean result)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Issued garbage collection %s result: %s", executor, result)
			;
		}

		@Override
		public void storageHousekeepingBroker_beforePerformIssuedEntityCacheCheck(final StorageHousekeepingExecutor executor,	final long nanoTimeBudget)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Performing issued entity cache check %s time budget %d ns", executor, nanoTimeBudget)
			;
		}

		@Override
		public void storageHousekeepingBroker_afterePerformIssuedEntityCacheCheck(final StorageHousekeepingExecutor executor,	final boolean result)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Issued entity cache check %s result: %s", executor, result)
			;
		}

		@Override
		public void storageHousekeepingBroker_beforePerformGarbageCollection(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Performing garbage collection %s time budget %d ns", executor, nanoTimeBudget)
			;
		}

		@Override
		public void storageHousekeepingBroker_afterPerformGarbageCollection(final StorageHousekeepingExecutor executor, final boolean result)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Garbage collection %s result: %s", executor, result)
			;
		}

		@Override
		public void storageHousekeepingBroker_beforePerformEntityCacheCheck(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			this.loggerStorageHousekeeping.trace()
			.withTag(this.logTag)
			.log("Performing entity cache check %s time budget %d ns", executor, nanoTimeBudget)
		;
		}

		@Override
		public void storageHousekeepingBroker_afterPerformEntityCacheCheck(final StorageHousekeepingExecutor executor, final boolean result)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("Entity cache check %s result: %s", executor, result)
			;
		}
		
		@Override
		public void storageEntityCacheEvaluator_afterClearEntityCache_true(final StorageEntity entity)
		{
			this.loggerStorageHousekeeping.trace()
				.withTag(this.logTag)
				.log("ClearEntityCache %s", entity)
			;
		}
		
		@Override
		public void storageEventLogger_afterLogChannelProcessingDisabled(final StorageChannel channel)
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("Channel processing disabled for channel %s", channel)
			;
		}
		
		@Override
		public void storageEventLogger_afterLogChannelStoppedWorking(final StorageChannel channel)
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("Channel stopped working %s", channel)
			;
		}

		@Override
		public void storageEventLogger_afterLogDisruption()
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("Channel disrupted")
			;
		}

		@Override
		public void storageEventLogger_afterLogDisruption(final StorageChannel channel, final Throwable t)
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("Channel %s disrupted by %s", channel, t)
			;
		}

		@Override
		public void storageEventLogger_afterLogLiveCheckComplete(final StorageEntityCache<?> entityCache)
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("StorageEntityCache live check completed %s", entityCache)
			;
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorSweepingComplete(final StorageEntityCache<?> entityCache)
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("StorageEntityCache garbage collector sweeping completed %s", entityCache)
			;
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorNotNeeded()
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("StorageEntityCache garbage collection not needed")
			;
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorCompletedHotPhase(final long gcHotGeneration, final long lastGcHotCompletion)
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("Garbage collector completed hot phase")
			;
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorCompleted(final long gcColdGeneration, final long lastGcColdCompletion)
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("Garbage collection completed")
			;
		}

		@Override
		public void storageEventLogger_afterLogGarbageCollectorEncounteredZombieObjectId(final long objectId)
		{
			this.loggerStorageEventsLogger.debug()
				.withTag(this.logTag)
				.log("Garbage collector encountered zombie ObjectID: %d", objectId)
			;
		}

		@Override
		public void storageBackupHandler_beforeInitialize(final int channelIndex)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Initializing StorageBackupHandler for channel %s", channelIndex)
			;
		}

		@Override
		public void storageBackupHandler_afterInitialize(final int channelIndex)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Initialized StorageBackupHandler for channel %s", channelIndex)
			;
		}

		@Override
		public void storageBackupHandler_beforeSynchronize(final StorageInventory storageInventory)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Synchronizing StorageBackupHandler with inventory %s", storageInventory)
			;
		}

		@Override
		public void storageBackupHandler_afterSynchronize(final StorageInventory storageInventory)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Synchronized StorageBackupHandler with inventory %s", storageInventory)
			;
		}

		@Override
		public void storageBackupHandler_beforeCopyFilePart(final StorageLiveChannelFile<?> sourceFile, final long sourcePosition, final long length)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Copy part of file s%", sourceFile)
			;
		}

		@Override
		public void storageBackupHandler_afterCopyFilePart(final StorageLiveChannelFile<?> sourceFile, final long sourcePosition, final long length)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Copied part of file s%", sourceFile)
			;
		}

		@Override
		public void storageBackupHandler_beforeTruncateFile(final StorageLiveChannelFile<?> file, final long newLength)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Truncating file %s", file)
			;
		}

		@Override
		public void storageBackupHandler_afterTruncateFile(final StorageLiveChannelFile<?> file, final long newLength)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Truncated file %s", file)
			;
		}

		@Override
		public void storageBackupHandler_beforeDeleteFile(final StorageLiveChannelFile<?> file)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Deleting file %s", file)
			;
		}

		@Override
		public void storageBackupHandler_afterDeleteFile(final StorageLiveChannelFile<?> file)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Deleted file %s", file)
			;
		}

		@Override
		public void storageBackupHandler_beforeStart()
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Starting StorageBackupHandler")
			;
		}

		@Override
		public void storageBackupHandler_afterStart(final StorageBackupHandler storageBackupHandler)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Started StorageBackupHandler %s", storageBackupHandler)
			;
		}

		@Override
		public void storageBackupHandler_beforeStop()
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Stopping StorageBackupHandler")
			;
		}

		@Override
		public void storageBackupHandler_afterStop(final StorageBackupHandler storageBackupHandler)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Stopped storageBackupHandler")
			;
		}

		@Override
		public void storageBackupHandler_beforeSetRunning(final boolean running)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Setting running status to %s", running)
			;
		}

		@Override
		public void storageBackupHandler_afterSetRunning(final StorageBackupHandler storageBackupHandler)
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Set running status of %s", storageBackupHandler)
			;
		}

		@Override
		public void storageBackupHandler_beforeRun()
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Starting StorageBackupHandler thread")
			;
		}

		@Override
		public void storageBackupHandler_afterRun()
		{
			this.loggerStorageBackupHandler.debug()
				.withTag(this.logTag)
				.log("Started StorageBackupHandler thread")
			;
		}
		
	}
	
}
