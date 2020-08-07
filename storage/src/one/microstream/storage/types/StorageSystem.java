package one.microstream.storage.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.afs.AFileSystem;
import one.microstream.meta.XDebug;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionNotAcceptingTasks;
import one.microstream.storage.exceptions.StorageExceptionNotRunning;

// (21.03.2016 TM)TODO: what is the difference between ~Manager and ~Controller here? Merge into Controller or comment.
public interface StorageSystem extends StorageController
{
	public StorageRequestAcceptor createRequestAcceptor();

	public StorageTypeDictionary typeDictionary();

	// (20.05.2013 TM)TODO: StorageManager#operationController() - not sure this belongs here
	public StorageOperationController operationController();
	
	public default StorageChannelCountProvider channelCountProvider()
	{
		return this.operationController().channelCountProvider();
	}

	public StorageConfiguration configuration();
	
	public default AFileSystem fileSystem()
	{
		return this.configuration().fileProvider().fileSystem();
	}
	

	@Override
	public StorageSystem start();
	
	public StorageIdAnalysis initializationIdAnalysis();
	
	@Override
	public boolean shutdown();

	public StorageObjectIdRangeEvaluator objectIdRangeEvaluator();
		


	public final class Default implements StorageSystem, Unpersistable, StorageKillable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// composite members //
		private final StorageConfiguration                 configuration                 ;
		private final StorageInitialDataFileNumberProvider initialDataFileNumberProvider ;
		private final StorageDataFileEvaluator             fileDissolver                 ;
		private final StorageLiveFileProvider              fileProvider                  ;
		private final StorageFileWriter.Provider           writerProvider                ;
		private final StorageRequestAcceptor.Creator       requestAcceptorCreator        ;
		private final StorageTaskBroker.Creator            taskBrokerCreator             ;
		private final StorageDataChunkValidator.Provider   dataChunkValidatorProvider    ;
		private final StorageChannelsCreator               channelCreator                ;
		private final StorageThreadProvider                threadProvider                ;
		private final StorageEntityCacheEvaluator          entityCacheEvaluator          ;
		private final StorageRequestTaskCreator            requestTaskCreator            ;
		private final StorageTypeDictionary                typeDictionary                ;
		private final StorageOperationController           operationController           ;
		private final StorageRootTypeIdProvider            rootTypeIdProvider            ;
		private final StorageExceptionHandler              exceptionHandler              ;
		private final StorageHousekeepingController        housekeepingController        ;
		private final StorageTimestampProvider             timestampProvider             ;
		private final StorageObjectIdRangeEvaluator        objectIdRangeEvaluator        ;
		private final StorageGCZombieOidHandler            zombieOidHandler              ;
		private final StorageRootOidSelector.Provider      rootOidSelectorProvider       ;
		private final StorageObjectIdMarkQueue.Creator     oidMarkQueueCreator           ;
		private final StorageEntityMarkMonitor.Creator     entityMarkMonitorCreator      ;
		private final StorageDataFileValidator.Creator     backupDataFileValidatorCreator;
		private final StorageBackupSetup                   backupSetup                   ;
		private final StorageLockFileSetup                 lockFileSetup                 ;
		private final StorageLockFileManager.Creator       lockFileManagerCreator        ;
		private final StorageEventLogger                   eventLogger                   ;
		private final boolean                              switchByteOrder               ;


		// state flags //
		private volatile boolean isStartingUp      ;
		private volatile boolean isShuttingDown    ;
		private final    Object  stateLock          = new Object();
		private volatile long    initializationTime;
		private volatile long    operationModeTime ;

		// running state members //
		private volatile StorageTaskBroker taskbroker    ;
		private final    ChannelKeeper[]   channelKeepers;
		
		private          StorageBackupHandler backupHandler;
		private          Thread               backupThread ;
		
		private          Thread               lockFileManagerThread;
		
		private StorageIdAnalysis initializationIdAnalysis;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final StorageConfiguration                 storageConfiguration          ,
			final StorageOperationController.Creator   ocCreator                     ,
			final StorageDataFileValidator.Creator     backupDataFileValidatorCreator,
			final StorageFileWriter.Provider           writerProvider                ,
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider ,
			final StorageRequestAcceptor.Creator       requestAcceptorCreator        ,
			final StorageTaskBroker.Creator            taskBrokerCreator             ,
			final StorageDataChunkValidator.Provider   dataChunkValidatorProvider    ,
			final StorageChannelsCreator               channelCreator                ,
			final StorageThreadProvider                threadProvider                ,
			final StorageRequestTaskCreator            requestTaskCreator            ,
			final StorageTypeDictionary                typeDictionary                ,
			final StorageRootTypeIdProvider            rootTypeIdProvider            ,
			final StorageTimestampProvider             timestampProvider             ,
			final StorageObjectIdRangeEvaluator        objectIdRangeEvaluator        ,
			final StorageGCZombieOidHandler            zombieOidHandler              ,
			final StorageRootOidSelector.Provider      rootOidSelectorProvider       ,
			final StorageObjectIdMarkQueue.Creator     oidMarkQueueCreator           ,
			final StorageEntityMarkMonitor.Creator     entityMarkMonitorCreator      ,
			final boolean                              switchByteOrder               ,
			final StorageLockFileSetup                 lockFileSetup                 ,
			final StorageLockFileManager.Creator       lockFileManagerCreator        ,
			final StorageExceptionHandler              exceptionHandler              ,
			final StorageEventLogger                   eventLogger
		)
		{
			super();

			final StorageChannelCountProvider ccp = storageConfiguration.channelCountProvider();
			
			// validate here, too, in case the StorageChannelCountProvider implementation has been customized.
			final int channelCount = ccp.getChannelCount();
			StorageChannelCountProvider.validateChannelCount(channelCount);

			this.channelKeepers                 = new ChannelKeeper[channelCount]              ;
			this.configuration                  = notNull(storageConfiguration)                ;
			this.operationController            = notNull(ocCreator.createOperationController(ccp, this));
			this.initialDataFileNumberProvider  = notNull(initialDataFileNumberProvider)       ;
			this.fileDissolver                  = storageConfiguration.dataFileEvaluator()     ;
			this.fileProvider                   = storageConfiguration.fileProvider()          ;
			this.entityCacheEvaluator           = storageConfiguration.entityCacheEvaluator()  ;
			this.housekeepingController         = storageConfiguration.housekeepingController();
			this.requestAcceptorCreator         = notNull(requestAcceptorCreator)              ;
			this.taskBrokerCreator              = notNull(taskBrokerCreator)                   ;
			this.dataChunkValidatorProvider     = notNull(dataChunkValidatorProvider)          ;
			this.channelCreator                 = notNull(channelCreator)                      ;
			this.threadProvider                 = notNull(threadProvider)                      ;
			this.requestTaskCreator             = notNull(requestTaskCreator)                  ;
			this.typeDictionary                 = notNull(typeDictionary)                      ;
			this.rootTypeIdProvider             = notNull(rootTypeIdProvider)                  ;
			this.timestampProvider              = notNull(timestampProvider)                   ;
			this.objectIdRangeEvaluator         = notNull(objectIdRangeEvaluator)              ;
			this.writerProvider                 = notNull(writerProvider)                      ;
			this.zombieOidHandler               = notNull(zombieOidHandler)                    ;
			this.rootOidSelectorProvider        = notNull(rootOidSelectorProvider)             ;
			this.oidMarkQueueCreator            = notNull(oidMarkQueueCreator)                 ;
			this.entityMarkMonitorCreator       = notNull(entityMarkMonitorCreator)            ;
			this.exceptionHandler               = notNull(exceptionHandler)                    ;
			this.lockFileSetup                  = mayNull(lockFileSetup)                       ;
			this.lockFileManagerCreator         = notNull(lockFileManagerCreator)              ;
			this.backupSetup                    = getBackupSetup(storageConfiguration)         ;
			this.backupDataFileValidatorCreator = notNull(backupDataFileValidatorCreator)      ;
			this.eventLogger                    = notNull(eventLogger)                         ;
			this.switchByteOrder                =         switchByteOrder                      ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private static StorageBackupSetup getBackupSetup(
			final StorageConfiguration storageConfiguration
		)
		{
			if(!storageConfiguration.fileProvider().fileSystem().isWritable())
			{
				// ignore backup for read-only systems to avoid backup file synching during initialization
				return null;
			}
			
			// may be null
			return storageConfiguration.backupSetup();
		}

		@Override
		public final StorageConfiguration configuration()
		{
			return this.configuration;
		}

		@Override
		public final boolean isRunning()
		{
			return this.isChannelProcessingEnabled();
		}
		
		@Override
		public final boolean isActive()
		{
			synchronized(this.stateLock)
			{
				/*
				 * If running is true, then it must be active as well (or will be shortly)
				 * If not, one or more channels might still be active.
				 * And if there is a backup handler, that might still be active, too.
				 * 
				 * Only if every active part is inactive is the whole storage inactive as well.
				 */
				return this.isRunning()
					|| this.hasActiveChannels()
					|| this.backupHandler != null && this.backupHandler.isActive()
				;
			}
		}
		
		private boolean hasActiveChannels()
		{
			for(final ChannelKeeper keeper : this.channelKeepers)
			{
				if(keeper.isActive())
				{
					return true;
				}
			}
			
			return false;
		}
		
		private boolean isChannelProcessingEnabled()
		{
			synchronized(this.stateLock)
			{
				if(this.operationController.isChannelProcessingEnabled())
				{
					return true;
				}
				
				return false;
			}
		}

		@Override
		public final boolean isAcceptingTasks()
		{
			return this.isChannelProcessingEnabled();
		}

		@Override
		public final boolean isStartingUp()
		{
			return this.isStartingUp;
		}

		@Override
		public final boolean isShuttingDown()
		{
			return this.isShuttingDown;
		}
		
		@Override
		public final long initializationTime()
		{
			return this.initializationTime;
		}
		
		@Override
		public final long operationModeTime()
		{
			return this.operationModeTime;
		}

		private void ensureRunning()
		{
			if(this.isRunning())
			{
				return;
			}
			throw new StorageExceptionNotRunning();
		}

		private StorageIdAnalysis startThreads(final StorageChannelTaskInitialize initializingTask)
			throws InterruptedException
		{
			// (07.07.2016 TM)TODO: StorageThreadStarter instead of hardcoded call
			synchronized(initializingTask)
			{
				for(final ChannelKeeper keeper : this.channelKeepers)
				{
					keeper.channelThread.start();
				}
				initializingTask.waitOnCompletion();
			}
						
			return initializingTask.idAnalysis();
		}
		
		private StorageBackupHandler provideBackupHandler()
		{
			if(this.backupHandler == null && this.backupSetup != null)
			{
				final StorageDataFileValidator validator = this.backupDataFileValidatorCreator
					.createDataFileValidator(this.typeDictionary)
				;
				
				this.backupHandler = this.backupSetup.setupHandler(this.operationController, validator);
			}
			
			return this.backupHandler;
		}
		
		private void startBackupThread()
		{
			final StorageBackupHandler backupHandler = this.provideBackupHandler();
			if(backupHandler == null)
			{
				return;
			}

			// set backup handling state to being running
			backupHandler.start();
			
			// setup a backup thread and start it.
			this.backupThread = this.threadProvider.provideBackupThread(backupHandler);
			this.backupThread.start();
		}
		
		private void initializeLockFileManager()
		{
			if(this.lockFileSetup == null || this.lockFileSetup.updateInterval() == 0)
			{
				// no setup or no interval means lock file is not desired
				return;
			}
			
			final StorageLockFileManager lockFileManager = this.lockFileManagerCreator.createLockFileManager(
				this.lockFileSetup,
				this.operationController
			);

			// initialize lock file manager state to being running
			lockFileManager.start();
			
			// setup a lock file manager thread and start it if initialization (obtaining the "lock") was successful.
			this.lockFileManagerThread = this.threadProvider.provideLockFileManagerThread(lockFileManager);
			// can't start before the operation controller isn't in proper running state...
		}
		
		private void startLockFileManagerThread()
		{
			if(this.lockFileManagerThread == null)
			{
				// can be null if lock file is not desired. See #initializeLockFileManager
				return;
			}
			
			// can't start before the operation controller isn't in proper running state, hence the extra method
			this.lockFileManagerThread.start();
		}



		// "Please do not disturb the Keepers" :-D
		static final class ChannelKeeper implements StorageActivePart
		{
			final int            channelIndex ;
			final StorageChannel channel      ;
			final Thread         channelThread;

			ChannelKeeper(final int channelIndex, final StorageChannel channel, final Thread thread)
			{
				super();
				this.channelIndex  = channelIndex;
				this.channel       = channel     ;
				this.channelThread = thread      ;
			}
			
			@Override
			public final boolean isActive()
			{
				return this.channel.isActive();
			}
		}
		
		

		private void createChannels()
		{
			final StorageFileWriter.Provider effectiveWriterProvider = this.dispatchWriterProvider();
			
			/* (24.09.2014 TM)TODO: check channel directory consistency
			 * run analysis on provided storage base directory to see if there exist any channel folders
			 * that match, are less or are more than the channel count.
			 * Also check if some of the folders are empty.
			 * Give analysis result to configurable callback handler (exception by default).
			 */
			final StorageChannel[] channels = this.channelCreator.createChannels(
				this.channelCount()                        ,
				this.initialDataFileNumberProvider         ,
				this.exceptionHandler                      ,
				this.fileDissolver                         ,
				this.fileProvider                          ,
				this.entityCacheEvaluator                  ,
				this.typeDictionary                        ,
				this.taskbroker                            ,
				this.operationController                   ,
				this.housekeepingController                ,
				this.timestampProvider                     ,
				effectiveWriterProvider                    ,
				this.zombieOidHandler                      ,
				this.rootOidSelectorProvider               ,
				this.oidMarkQueueCreator                   ,
				this.entityMarkMonitorCreator              ,
				this.provideBackupHandler()                ,
				this.eventLogger                           ,
				this.switchByteOrder                       ,
				this.rootTypeIdProvider.provideRootTypeId()
			);

			final ChannelKeeper[] keepers = this.channelKeepers;
			for(int i = 0; i < channels.length; i++)
			{
				keepers[i] = new ChannelKeeper(i, channels[i], this.threadProvider.provideChannelThread(channels[i]));
			}
		}
		
		private StorageFileWriter.Provider dispatchWriterProvider()
		{
			if(this.backupSetup == null)
			{
				return this.writerProvider;
			}
			
			return this.backupSetup.setupWriterProvider(this.writerProvider);
		}

		private int channelCount()
		{
			// once set, the channel count cannot be changed. Might be improved in the future.
			return this.channelKeepers.length;
		}

		private void internalStartUp() throws InterruptedException
		{
			// first of all, the lock file needs to be obtained before any writing action may occur.
			this.initializeLockFileManager();
			
			// thread safety and state consistency ensured prior to calling

			// create channels, setup task processing and start threads
			this.taskbroker = this.taskBrokerCreator.createTaskBroker(this, this.requestTaskCreator);
						
			final StorageChannelTaskInitialize task = this.taskbroker.issueChannelInitialization(
				this.operationController
			);
			this.createChannels();

			final StorageIdAnalysis idAnalysis = this.startThreads(task);
			final Long              maxOid     = idAnalysis.highestIdsPerType().get(Persistence.IdType.OID);

			// only ObjectId is relevant at this point
			this.objectIdRangeEvaluator.evaluateObjectIdRange(0, maxOid == null ? 0 : maxOid);
			
			this.initializationIdAnalysis = idAnalysis;

			// mandatory
			this.startLockFileManagerThread();
			
			// optional
			this.startBackupThread();
		}

		private void internalShutdown() throws InterruptedException
		{
			// note: this method is already entered under a lock protection, so there can't be a race condition here.
			if(this.taskbroker == null)
			{
				XDebug.println("taskbroker is null");
				// storage not started in the first place
				return;
			}
			
//			DEBUGStorage.println("shutting down ...");
			final StorageChannelTaskShutdown task = this.taskbroker.issueChannelShutdown(this.operationController);
			
			synchronized(task)
			{
				// (07.07.2016 TM)FIXME: OGS-23: shutdown doesn't wait for the shutdown to be completed.
				task.waitOnCompletion();
			}
			this.taskbroker = null;

			/* (07.03.2019 TM)FIXME: Shutdown must wait for ongoing activities.
			 * Such as a StorageBackupHandler thread with a non-empty item queue.
			 * There must be a kind of "activity registry" where all activities are registered
			 * and that is checked until all activities have stopped.
			 * Probably with a "forced" / "kill" flag to enforce shutdown.
			 * Or, more object-oriented-ly: an activities checker type that gets passed
			 * the ongoing activity, the time since the shutdown issue, etc.
			 * 
			 * This could probabyl done in the context of a general overhaul of the shutdown process.
			 * There are already a lot of other TODOs and even bugs concerning that.
			 * Maybe channel threads should simply be registered as activities, too.
			 * 
			 */
			
//			DEBUGStorage.println("shutdown complete");
		}

		@Override
		public final void checkAcceptingTasks()
		{
			if(this.isAcceptingTasks())
			{
				return;
			}
			throw new StorageExceptionNotAcceptingTasks();
		}

		@Override
		public final StorageSystem.Default start()
		{
			synchronized(this.stateLock)
			{
				if(this.isRunning())
				{
					throw new StorageException("already starting"); // (05.07.2014 TM)EXCP: proper exception
				}
				
				this.isStartingUp = true;
				try
				{
					this.initializationTime = System.currentTimeMillis();
					// causes the internal state to switch to running
					this.internalStartUp();
					this.operationModeTime = System.currentTimeMillis();
				}
				catch(final InterruptedException e)
				{
					this.operationController.deactivate();
					throw new StorageException(e); // (15.06.2013 TM)EXCP: proper exception
				}
				catch(final Throwable t)
				{
					this.operationController.deactivate();
					throw t;
				}
				finally
				{
					this.isStartingUp = false;
				}
			}
			
			return this;
		}
		
		@Override
		public final StorageIdAnalysis initializationIdAnalysis()
		{
			return this.initializationIdAnalysis;
		}

		@Override
		public final boolean shutdown()
		{
			synchronized(this.stateLock)
			{
				try
				{
					this.internalShutdown();
					return true;
				}
				catch(final InterruptedException e)
				{
					/* (09.12.2019 TM)FIXME: swallows interruption for the outside context.
					 * Once again: How to handle these things?
					 * Pass them through all API layers as checked exceptions? Doesn't feel right.
					 * Wrap them in a runtime exception? Would make things even worse in thise
					 * particular case ...
					 * 
					 * Or does it really make sense to have an interruptible shutdown in the first place?
					 * What if some parts already shut down?
					 * Really do a partial restart instead of just a complete shutdown and a restart?
					 * And can the active threads really be interrupted from the outside so an interruption would
					 * have any meaning to an outside caller?
					 * So many questions ...
					 * 
					 * For now, it's a false ... ^^.
					 */
					// interruption while waiting for shutdown means don't shut down
					return false;
				}
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final StorageTypeDictionary typeDictionary()
		{
			return this.typeDictionary;
		}

		@Override
		public StorageOperationController operationController()
		{
			return this.operationController;
		}

		@Override
		public StorageObjectIdRangeEvaluator objectIdRangeEvaluator()
		{
			return this.objectIdRangeEvaluator;
		}

		@Override
		public final StorageRequestAcceptor createRequestAcceptor()
		{
			this.ensureRunning();

			return this.requestAcceptorCreator.createRequestAcceptor(
				this.dataChunkValidatorProvider.provideDataChunkValidator(this.typeDictionary),
				this.taskbroker
			);
		}
		
		@Override
		public void killStorage(final Throwable cause)
		{
			/*
			 * Immediately deactivates all activities without waiting for currently existing work items to be
			 * completed.
			 * 
			 * Deactivates all threads (Channel threads, lock file thread, backup thread).
			 * All terminating threads cleanup their resources (e.g. opened files).
			 * So this is all that must be necessary
			 */
			this.operationController.deactivate();
			
			// backup handler must be treated specially since it is normally intended to finish its items on its own.
			if(this.backupHandler != null)
			{
				this.backupHandler.setRunning(false);
			}
		}

	}

}
