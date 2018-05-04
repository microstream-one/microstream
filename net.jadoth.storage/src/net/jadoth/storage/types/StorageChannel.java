package net.jadoth.storage.types;

import static net.jadoth.X.notNull;
import static net.jadoth.math.JadothMath.notNegative;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Predicate;

import net.jadoth.X;
import net.jadoth.functional.ThrowingProcedure;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.Chunks;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.ChunksBuffer;
import net.jadoth.persistence.types.BufferSizeProvider;
import net.jadoth.persistence.types.Unpersistable;
import net.jadoth.storage.exceptions.StorageException;
import net.jadoth.swizzling.types.SwizzleIdSet;
import net.jadoth.typing.KeyValue;


public interface StorageChannel extends Runnable, StorageHashChannelPart
{
	public StorageTypeDictionary typeDictionary();

	public Binary collectLoadByOids(SwizzleIdSet loadOids);

	public Binary collectLoadRoots();

	public Binary collectLoadByTids(SwizzleIdSet loadTids);

	public KeyValue<ByteBuffer[], long[]> storeEntities(long timestamp, Chunks chunkData);

	public void rollbackChunkStorage();

	public void commitChunkStorage();

	public void postStoreUpdateEntityCache(ByteBuffer[] chunks, long[] chunksStoragePositions)
		throws InterruptedException;

	public StorageInventory readStorage();

	public boolean issuedGarbageCollection(long nanoTimeBudgetBound);

	public boolean issuedFileCheck(long nanoTimeBudgetBound, StorageDataFileDissolvingEvaluator fileDissolver);

	public boolean issuedCacheCheck(long nanoTimeBudgetBound, StorageEntityCacheEvaluator entityEvaluator);

	public void exportData(StorageIoHandler fileHandler);

	// (19.07.2014)TODO: refactor storage typing to avoid classes in public API
	public StorageEntityCache.Implementation prepareImportData();

	public void importData(StorageChannelImportSourceFile importFile);

	public void rollbackImportData(Throwable cause);

	public void commitImportData(long taskTimestamp);

	public KeyValue<Long, Long> exportTypeEntities(StorageEntityTypeHandler<?> type, StorageLockedFile file)
		throws IOException;

	public KeyValue<Long, Long> exportTypeEntities(
		StorageEntityTypeHandler<?>      type           ,
		StorageLockedFile                file           ,
		Predicate<? super StorageEntity> predicateEntity
	) throws IOException;

	public StorageRawFileStatistics.ChannelStatistics createRawFileStatistics();

	public StorageIdRangeAnalysis initializeStorage(
		long                        taskTimestamp                   ,
		long                        consistentStoreTimestamp        ,
		StorageInventory            storageInventory                ,
		StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
		StorageTypeDictionary       oldTypes
	);

	public void clear();

	public void signalGarbageCollectionSweepCompleted();

	public void truncateData();

	public void cleanupStore();



	public final class Implementation implements StorageChannel, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final int                               channelIndex             ;
		private final StorageExceptionHandler           exceptionHandler         ;
		private final StorageTaskBroker                 taskBroker               ;
		private final StorageChannelController          channelController        ;
		private final StorageHousekeepingController     housekeepingController   ;
		private final StorageFileManager.Implementation fileManager              ;
		private final StorageEntityCache.Implementation entityCache              ;
		private final BufferSizeProvider                loadingBufferSizeProvider;

		private final HousekeepingTask[] housekeepingTasks =
		{
			this::houseKeepingCheckFileCleanup ,
			this::houseKeepingGarbageCollection,
			this::houseKeepingLiveCheck
		};
		private int nextHouseKeepingIndex;

		/**
		 * A nanosecond timestamp marking the calculated end of the current housekeeping interval.
		 * @see {@link StorageHousekeepingController#housekeepingInterval()}
		 */
		private long housekeepingIntervalBoundTimeNs;

		/**
		 * The remaining housekeeping budget in nanoseconds for the current interval.
		 * @see StorageHousekeepingController#housekeepingNanoTimeBudgetBound(long)
		 */
		private long housekeepingIntervalBudgetNs;





		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final int                               hashIndex                ,
			final StorageExceptionHandler           exceptionHandler         ,
			final StorageTaskBroker                 taskBroker               ,
			final StorageChannelController          controller               ,
			final StorageHousekeepingController     housekeepingController   ,
			final StorageEntityCache.Implementation entityCache              ,
			final BufferSizeProvider                loadingBufferSizeProvider,
			final StorageFileManager.Implementation fileManager
		)
		{
			super();
			this.channelIndex              = notNegative(hashIndex)                ;
			this.exceptionHandler          = notNull    (exceptionHandler)         ;
			this.taskBroker                = notNull    (taskBroker)               ;
			this.channelController         = notNull    (controller)               ;
			this.fileManager               = notNull    (fileManager)              ;
			this.entityCache               = notNull    (entityCache)              ;
			this.housekeepingController    = notNull    (housekeepingController)   ;
			this.loadingBufferSizeProvider = notNull    (loadingBufferSizeProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private int getCurrentHouseKeepingIndexAndAdvance()
		{
			if(this.nextHouseKeepingIndex >= this.housekeepingTasks.length)
			{
				this.nextHouseKeepingIndex = 1;
				return 0;
			}
			return this.nextHouseKeepingIndex++;
		}

		private void houseKeeping()
		{
			final long currentNanotime;

			if((currentNanotime = System.nanoTime()) >= this.housekeepingIntervalBoundTimeNs)
			{
				this.housekeepingIntervalBoundTimeNs = currentNanotime
					+ Storage.millisecondsToNanoseconds(this.housekeepingController.housekeepingInterval())
				;
				this.housekeepingIntervalBudgetNs = this.housekeepingController.housekeepingNanoTimeBudgetBound();
//				DEBUGStorage.println(this.channelIndex + " resetting housekeeping budget at " + new java.text.DecimalFormat("00,000,000,000").format(currentNanotime) + " to " + new java.text.DecimalFormat("00,000,000,000").format(this.housekeepingIntervalBoundTimeNs));
			}
			else if(this.housekeepingIntervalBudgetNs <= 0)
			{
//				if(this.channelIndex ==  0)
//				{
//					DEBUGStorage.println(this.channelIndex + " has no time for house keeping.");
//				}
				return;
			}
			else if(this.housekeepingIntervalBoundTimeNs - currentNanotime < this.housekeepingIntervalBudgetNs)
			{
				// cap remaining housekeeping budget at the current interval's housekeeping time bound
				this.housekeepingIntervalBudgetNs = this.housekeepingIntervalBoundTimeNs - currentNanotime;
//				DEBUGStorage.println(this.channelIndex + " capping housekeeping budget to " + new java.text.DecimalFormat("00,000,000,000").format(this.housekeepingIntervalBudgetNs));
			}

			final long budgetOffset = currentNanotime + this.housekeepingIntervalBudgetNs;

//			if(this.channelIndex ==  0)
//			{
//				DEBUGStorage.println(this.channelIndex + " housekeepig budget (ns) = " + new java.text.DecimalFormat("00,000,000,000").format(this.housekeepingIntervalBudgetNs));
//			}

			// execute every task once at most per cycle (therefore the counter, but NOT for selecting the task)
			for(int c = 0; c < this.housekeepingTasks.length; c++)
			{
//				DEBUGStorage.println(this.channelIndex + " house keeping task #" + (c + 1));
				// call the next task (next from last cycle or just another one if there is still time)
				this.housekeepingTasks[this.getCurrentHouseKeepingIndexAndAdvance()].perform();

				// intentionally checked AFTER the first housekeeping task to guarantee at least one task to be executed
				if((this.housekeepingIntervalBudgetNs = budgetOffset - System.nanoTime()) <= 0)
				{
//					DEBUGStorage.println(this.channelIndex + " has no more time for house keeping.");
					break;
				}
			}

//			final long endTime = System.nanoTime();
//			final long duration = endTime - cycleStartTime;
//			final long budget = timeBudgetBound - cycleStartTime;
//			final double ratio = (double)duration / budget * 100;
//			DEBUGStorage.println(this.channelIndex + " ending housekeeping, total time (ns) = " + duration + " of " + budget + "(" + ratio + "%)");
		}

		private long calculateSpecificHousekeepingTimeBudgetBound(final long specificBudget)
		{
//			DEBUGStorage.println(this.channelIndex + " spec budget = " + specificBudget + ", gen budget = " + this.housekeepingIntervalBudgetNs);
			return System.nanoTime() + Math.min(specificBudget, this.housekeepingIntervalBudgetNs);
		}

		final boolean houseKeepingCheckFileCleanup()
		{
			return this.fileManager.incrementalFileCleanupCheck(
				this.calculateSpecificHousekeepingTimeBudgetBound(
					this.housekeepingController.fileCheckNanoTimeBudget()
				)
			);
		}

		final boolean houseKeepingGarbageCollection()
		{
			return this.entityCache.incrementalGarbageCollection(
				this.calculateSpecificHousekeepingTimeBudgetBound(
					this.housekeepingController.garbageCollectionNanoTimeBudget()
				),
				this
			);
		}

		final boolean houseKeepingLiveCheck()
		{
			return this.entityCache.incrementalLiveCheck(
				this.calculateSpecificHousekeepingTimeBudgetBound(
					this.housekeepingController.liveCheckNanoTimeBudget()
				)
			);
		}

		private void work() throws InterruptedException
		{
			final StorageChannelController      channelController      = this.channelController     ;
			final StorageHousekeepingController housekeepingController = this.housekeepingController;

			StorageTask processedTask = new StorageTask.DummyTask();
			StorageTask currentTask   = notNull(this.taskBroker.currentTask());

			while(true)
			{
				// ensure to process every task only once in case no new task came in in time (see below).
				if(currentTask != processedTask)
				{
//					DEBUGStorage.println(this.channelIndex + " processing " + currentTask);
					currentTask.processBy(this);
					processedTask = currentTask;
				}

				/*
				 * Must check immediately after task processing to abort BEFORE houseKeeping is called in case
				 * of shutdown (otherwise NPE on headFile etc.). So do-while not possible.
				 * Also may NOT check before task processing as the first task is initializing which in turn
				 * enables channel processing on success. So no simple while condition possible.
				 */
				if(!channelController.isChannelProcessingEnabled())
				{
					DEBUGStorage.println(this.channelIndex + " processing disabled.");
					break;
				}

//				DEBUGStorage.println(this.channelIndex + " housekeeping");
				// do a little house keeping, either after a new task or use time if no new task came in.
				this.houseKeeping();
//				final long waitStart = System.currentTimeMillis();

				// check and wait for the next task to come in
				if((currentTask = processedTask.awaitNext(housekeepingController.housekeepingInterval())) == null)
				{
//					DEBUGStorage.println(this.channelIndex + " issuing GC");
//					if(waitStart + timeConfiguration.housekeepingInterval() < System.currentTimeMillis())
//					{
//						currentTask = this.taskBroker.issueGarbageCollectionPhaseCheck(processedTask);
//					}
					// revert to processed task to wait on it again for the next task
					currentTask = processedTask;
//					currentTask = this.taskBroker.issueGarbageCollectionPhaseCheck(processedTask);
				}
//				DEBUGStorage.println(this.channelIndex + " current Task: " + currentTask);
			}

			DEBUGStorage.println("Storage channel " + this.channelIndex + " stops working.");
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void run()
		{
			try
			{
				this.work();
			}
			catch(final Throwable t)
			{
				/*
				 * Note that t could be an error or it could even be a checked exception thrown via
				 * Proxy reflective tinkering or Unsafe mechanisms.
				 * However Throwable cannot be rethrown in Runnable#run() without cheating exception checking again.
				 * The whole checked exceptions stuff is just nonsense in the non-simple cases.
				 * Luckily, in this special case, reporting the cause and then dying "silently" is sufficient.
				 *
				 * Note: applies to interruption as well, because on privately managed threads,
				 * interruping ultimately means just stop running in a ordered fashion
				 */
				DEBUGStorage.println(this.channelIndex + " encountered exception " + t);
				this.exceptionHandler.handleException(t, this);
			}
		}

		@Override
		public void commitChunkStorage()
		{
			this.fileManager.commitWrite();
		}

		@Override
		public KeyValue<ByteBuffer[], long[]> storeEntities(final long timestamp, final Chunks chunkData)
		{
			// reset even if there is no new data to account for (potential) new data in other channel
			this.entityCache.registerPendingStoreUpdate();

			final ByteBuffer[] buffers = chunkData.buffers();
			// set new data flag, even if chunk has no data to account for (potential) data in other channels
			return X.KeyValue(buffers, this.fileManager.storeChunks(timestamp, buffers, chunkData.entityCount()));
		}

		@Override
		public void postStoreUpdateEntityCache(final ByteBuffer[] chunks, final long[] chunksStoragePositions)
			throws InterruptedException
		{
			// all chunks were written into the same file, so it is viable to pass the current file right here
			this.entityCache.postStorePutEntities(chunks, chunksStoragePositions, this.fileManager.currentStorageFile());
		}

		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}

		@Override
		public final StorageTypeDictionary typeDictionary()
		{
			return this.entityCache.typeDictionary();
		}
		
		private ChunksBuffer createLoadingChunksBuffer()
		{
			return ChunksBuffer.New(this.loadingBufferSizeProvider);
		}

		@Override
		public final Binary collectLoadByOids(final SwizzleIdSet loadOids)
		{
//			DEBUGStorage.println(this.channelIndex + " loading " + loadOids.size() + " references");

			/* it is probably best to start (any maybe continue) with lots of small, memory-agile
			 * byte buffers than to estimate one sufficiently huge bulky byte buffer.
			 */
			final ChunksBuffer chunks = this.createLoadingChunksBuffer();
			if(!loadOids.isEmpty())
			{
				// progress must have been incremented accordingly at task creation time
				loadOids.iterate(new EntityCollectorByOid(this.entityCache, chunks));
			}
			return chunks.complete();
		}

		@Override
		public final Binary collectLoadRoots()
		{
			// pretty straight forward: cram all root instances the entity cache knows of into the buffer
			final ChunksBuffer chunks = this.createLoadingChunksBuffer();
			this.entityCache.copyRoots(chunks);
			return chunks.complete();
		}

		@Override
		public final Binary collectLoadByTids(final SwizzleIdSet loadTids)
		{
			final ChunksBuffer chunks = this.createLoadingChunksBuffer();
			if(!loadTids.isEmpty())
			{
				// progress must have been incremented accordingly at task creation time
				loadTids.iterate(new EntityCollectorByTid(this.entityCache, chunks));
			}
			return chunks.complete();
		}

		@Override
		public final boolean issuedGarbageCollection(final long nanoTimeBudgetBound)
		{
			return this.entityCache.issuedGarbageCollection(nanoTimeBudgetBound, this);
		}

		@Override
		public boolean issuedFileCheck(
			final long                               nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolver
		)
		{
			return this.fileManager.issuedFileCleanupCheck(nanoTimeBudgetBound, fileDissolver);
		}

		@Override
		public boolean issuedCacheCheck(
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			return this.entityCache.issuedCacheCheck(nanoTimeBudgetBound, entityEvaluator);
		}

		@Override
		public final void exportData(final StorageIoHandler fileHandler)
		{
			this.fileManager.exportData(fileHandler);
		}

		@Override
		public StorageEntityCache.Implementation prepareImportData()
		{
			this.fileManager.prepareImport();
			return this.entityCache;
		}

		@Override
		public void importData(final StorageChannelImportSourceFile importFile)
		{
			this.fileManager.copyData(importFile);
		}

		@Override
		public void rollbackImportData(final Throwable cause)
		{
			this.fileManager.rollbackImport();
		}

		@Override
		public void commitImportData(final long taskTimestamp)
		{
			this.fileManager.commitImport(taskTimestamp);
		}

		@Override
		public final KeyValue<Long, Long> exportTypeEntities(
			final StorageEntityTypeHandler<?>         type           ,
			final StorageLockedFile                file           ,
			final Predicate<? super StorageEntity> predicateEntity
		)
			throws IOException
		{
			final StorageEntityType.Implementation entities = this.entityCache.getType(type.typeId());
			if(entities == null || entities.entityCount() == 0)
			{
				return X.KeyValue(0L, 0L);
			}

			final long byteCount = entities.iterateEntities(
				new ThrowingProcedure<StorageEntity.Implementation, IOException>()
				{
					long byteCount;

					@Override
					public void accept(final StorageEntity.Implementation e) throws IOException
					{
						if(!predicateEntity.test(e))
						{
							return;
						}
						this.byteCount += e.exportTo(file);
					}
				}
			).byteCount;

			return X.KeyValue(byteCount, entities.entityCount());
		}

		// intentionally implemented redundantly to the other exportTypeEntities for performance reasons
		@Override
		public final KeyValue<Long, Long> exportTypeEntities(
			final StorageEntityTypeHandler<?> type,
			final StorageLockedFile           file
		)
			throws IOException
		{
			final StorageEntityType.Implementation entities = this.entityCache.getType(type.typeId());
			if(entities == null || entities.entityCount() == 0)
			{
				return X.KeyValue(0L, 0L);
			}

			final long byteCount = entities.iterateEntities(
				new ThrowingProcedure<StorageEntity.Implementation, IOException>()
				{
					long byteCount;

					@Override
					public void accept(final StorageEntity.Implementation e) throws IOException
					{
						this.byteCount += e.exportTo(file);
					}
				}
			).byteCount;

			return X.KeyValue(byteCount, entities.entityCount());
		}

		@Override
		public final StorageRawFileStatistics.ChannelStatistics createRawFileStatistics()
		{
			return this.fileManager.createRawFileStatistics();
		}

		@Override
		public final void rollbackChunkStorage()
		{
			this.fileManager.rollbackWrite();
		}

		@Override
		public final StorageInventory readStorage()
		{
			return this.fileManager.readStorage();
		}

		@Override
		public final StorageIdRangeAnalysis initializeStorage(
			final long                        taskTimestamp                   ,
			final long                        consistentStoreTimestamp        ,
			final StorageInventory            storageInventory                ,
			final StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
			final StorageTypeDictionary       oldTypes
		)
		{
			// (04.04.2016 TM)NOTE: little performance test for entity iteration per files
//			final StorageIdRangeAnalysis result = this.fileManager.initializeStorage(
//				taskTimestamp                   ,
//				consistentStoreTimestamp        ,
//				storageInventory                ,
//				entityInitializingCacheEvaluator,
//				oldTypes
//			);
//			for(int i = 10; i-- > 0;)
//			{
//				final long tStart = System.nanoTime();
//				final long count = this.fileManager.iterateEntities(new Consumer<StorageEntity.Implementation>()
//				{
//					long count = 0;
//					@Override
//					public void accept(final StorageEntity.Implementation e)
//					{
//						this.count++;
//					}
//				}).count;
//				final long tStop = System.nanoTime();
//				System.out.println(this.channelIndex() + " iterated " + count + ", Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//			}
//			return result;

			return this.fileManager.initializeStorage(
				taskTimestamp                   ,
				consistentStoreTimestamp        ,
				storageInventory                ,
				entityInitializingCacheEvaluator,
				oldTypes
			);
		}

		@Override
		public final void clear()
		{
			this.fileManager.clearRegisteredFiles();
			this.entityCache.clearState();
		}

		@Override
		public void truncateData()
		{
			this.entityCache.clearState();
			this.fileManager.truncateFiles();
		}

		@Override
		public final void signalGarbageCollectionSweepCompleted()
		{
			this.fileManager.resetFileCleanupCursor();
		}

		@Override
		public void cleanupStore()
		{
			this.entityCache.clearPendingStoreUpdate();
		}

	}



	public interface Creator
	{
		public StorageChannel[] createChannels(
			int                                   channelCount                 ,
			StorageInitialDataFileNumberProvider  initialDataFileNumberProvider,
			StorageExceptionHandler               exceptionHandler             ,
			StorageDataFileEvaluator              fileDissolver                ,
			StorageFileProvider                   storageFileProvider          ,
			StorageEntityCacheEvaluator           entityCacheEvaluator         ,
			StorageTypeDictionary                 typeDictionary               , // the connection to the exclusive storage (file or whatever)
			StorageTaskBroker                     taskBroker                   , // the source for new tasks
			StorageChannelController              channelController            , // simple hook to check if processing is still enabled
			StorageHousekeepingController         housekeepingController       ,
			StorageTimestampProvider              timestampProvider            ,
			StorageFileReader.Provider            readerProvider               ,
			StorageFileWriter.Provider            writerProvider               ,
			StorageWriteListener                  writeListener                ,
			StorageGCZombieOidHandler             zombieOidHandler             ,
			StorageRootOidSelector.Provider       rootOidSelectorProvider      ,
			StorageOidMarkQueue.Creator           oidMarkQueueCreator          ,
			StorageEntityMarkMonitor.Creator      entityMarkMonitorCreator     ,
			long                                  rootTypeId
		);



		public static final class Implementation implements Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public final StorageChannel.Implementation[] createChannels(
				final int                                   channelCount                 ,
				final StorageInitialDataFileNumberProvider  initialDataFileNumberProvider,
				final StorageExceptionHandler               exceptionHandler             ,
				final StorageDataFileEvaluator              fileDissolver                ,
				final StorageFileProvider                   storageFileProvider          ,
				final StorageEntityCacheEvaluator           entityCacheEvaluator         ,
				final StorageTypeDictionary                 typeDictionary               ,
				final StorageTaskBroker                     taskBroker                   ,
				final StorageChannelController              channelController            ,
				final StorageHousekeepingController         housekeepingController       ,
				final StorageTimestampProvider              timestampProvider            ,
				final StorageFileReader.Provider            readerProvider               ,
				final StorageFileWriter.Provider            writerProvider               ,
				final StorageWriteListener                  writeListener                ,
				final StorageGCZombieOidHandler             zombieOidHandler             ,
				final StorageRootOidSelector.Provider       rootOidSelectorProvider      ,
				final StorageOidMarkQueue.Creator           oidMarkQueueCreator          ,
				final StorageEntityMarkMonitor.Creator      entityMarkMonitorCreator     ,
				final long                                  rootTypeId
			)
			{
				// (14.07.2016 TM)TODO: make configuration dynamic
				final int  markBufferLength         = 10000;
				final long markingWaitTimeMs        =    10;
				final int  loadingBufferSize        =  Memory.defaultBufferSize();
				final int  readingDefaultBufferSize =  Memory.defaultBufferSize();

				final StorageChannel.Implementation[]     channels = new StorageChannel.Implementation[channelCount];

				final StorageOidMarkQueue[]    markQueues = new StorageOidMarkQueue[channels.length];
				for(int i = 0; i < markQueues.length; i++)
				{
					markQueues[i] = oidMarkQueueCreator.createOidMarkQueue(markBufferLength);
				}
				final StorageEntityMarkMonitor markMonitor = entityMarkMonitorCreator.createEntityMarkMonitor(markQueues);
				
				final BufferSizeProvider loadingBufferSizeProvider        = new BufferSizeProvider.Simple(loadingBufferSize);
				final BufferSizeProvider readingDefaultBufferSizeProvider = new BufferSizeProvider.Simple(readingDefaultBufferSize);

				for(int i = 0; i < channels.length; i++)
				{
					// entity cache to register entities, cache entity data, perform garbage collection
					final StorageEntityCache.Implementation entityCache = new StorageEntityCache.Implementation(
						i                                                ,
						channels.length                                  ,
						entityCacheEvaluator                             ,
						typeDictionary                                   ,
						markMonitor                                      ,
						zombieOidHandler                                 ,
						rootOidSelectorProvider.provideRootOidSelector(i),
						rootTypeId                                       ,
						markQueues[i]                                    ,
						markBufferLength                                 ,
						markingWaitTimeMs
					);

					// file manager to handle "file" IO (whatever "file" might be, might be a RDBMS binary table as well)
					final StorageFileManager.Implementation fileManager = new StorageFileManager.Implementation(
						i                               ,
						initialDataFileNumberProvider   ,
						timestampProvider               ,
						storageFileProvider             ,
						fileDissolver                   ,
						entityCache                     ,
						readerProvider.provideReader(i) ,
						writerProvider.provideWriter(i) ,
						writeListener                   ,
						readingDefaultBufferSizeProvider
					);

					// required to resolve the initializer cyclic depedency
					entityCache.initializeStorageManager(fileManager);

					// everything bundled together in a "channel".
					channels[i] = new StorageChannel.Implementation(
						i                        ,
						exceptionHandler         ,
						taskBroker               ,
						channelController        ,
						housekeepingController   ,
						entityCache              ,
						loadingBufferSizeProvider,
						fileManager
					);

				}
				return channels;
			}

		}

	}


	public final class EntityCollectorByOid implements _longProcedure
	{
		// (01.06.2013)TODO: clean up / consolidate all internal implementations

		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageEntityCache.Implementation entityCache  ;
		private final ChunksBuffer                      dataCollector;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public EntityCollectorByOid(
			final StorageEntityCache.Implementation entityCache  ,
			final ChunksBuffer                      dataCollector
		)
		{
			super();
			this.entityCache   = entityCache  ;
			this.dataCollector = dataCollector;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void accept(final long oid)
		{
			final StorageEntityCacheItem<?> entry;
			if((entry = this.entityCache.getEntry(oid)) == null)
			{
				/* (14.01.2015 TM)NOTE: this actually is an error, as every oid request comes
				 * from a referencing entity from inside the same database. So if any load request lookup
				 * yields null, it is a inconcistency that has to be expressed rather sooner than later.
				 *
				 * If some kind of querying request (look if an arbitrary oid yields an entity) is needed,
				 * is has to be a dedicated kind of request, not this one.
				 * This one does recursive graph loading (consistency required), not arbitrary querying
				 * with optional results.
				 */
				// (14.01.2015 TM)EXCP: proper exception
				throw new StorageException("No entity found for objectId " + oid);
			}
			entry.copyCachedData(this.dataCollector);
		}

	}

	public final class EntityCollectorByTid implements _longProcedure
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageEntityCache.Implementation entityCache  ;
		private final ChunksBuffer                      dataCollector;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public EntityCollectorByTid(
			final StorageEntityCache.Implementation entityCache  ,
			final ChunksBuffer                      dataCollector
		)
		{
			super();
			this.entityCache   = entityCache  ;
			this.dataCollector = dataCollector;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void accept(final long tid)
		{
			final StorageEntityType.Implementation type;
			if((type = this.entityCache.getType(tid)) == null)
			{
				// it can very well be that a channel does not have a certain type at all. That is no error
				return;
			}

			// all the type's entities are iterated and their data is collected
			for(StorageEntity.Implementation entity = type.head; (entity = entity.typeNext) != null;)
			{
				entity.copyCachedData(this.dataCollector);
			}
		}

	}


	@FunctionalInterface
	public interface HousekeepingTask
	{
		/**
		 * Performs a housekeeping task with reference to a starting time of the current housekeeping cycle
		 * (typically to make a best effort attempt to not exceed a certain time budget).
		 * Returns {@literal true} if the task was completed (e.g. currently no more work to dor)
		 * or {@literal false} if the task execution had to be interrupted.
		 *
		 * @return whether the task was completed in the given time budget.
		 */
		public boolean perform();
	}

}
