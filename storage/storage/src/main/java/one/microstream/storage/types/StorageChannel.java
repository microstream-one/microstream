package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Predicate;

import org.slf4j.Logger;

import one.microstream.X;
import one.microstream.afs.types.AWritableFile;
import one.microstream.collections.BulkList;
import one.microstream.functional.ThrowingProcedure;
import one.microstream.functional._longProcedure;
import one.microstream.persistence.binary.types.Chunk;
import one.microstream.persistence.binary.types.ChunksBuffer;
import one.microstream.persistence.binary.types.ChunksBufferByteReversing;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.storage.exceptions.StorageExceptionConsistency;
import one.microstream.time.XTime;
import one.microstream.typing.Disposable;
import one.microstream.typing.KeyValue;
import one.microstream.util.BufferSizeProviderIncremental;
import one.microstream.util.logging.Logging;


public interface StorageChannel extends Runnable, StorageChannelResetablePart, StorageActivePart, Disposable
{
	public StorageTypeDictionary typeDictionary();

	public ChunksBuffer collectLoadByOids(ChunksBuffer[] channelChunks, PersistenceIdSet loadOids);

	public ChunksBuffer collectLoadRoots(ChunksBuffer[] channelChunks);

	public ChunksBuffer collectLoadByTids(ChunksBuffer[] channelChunks, PersistenceIdSet loadTids);

	public KeyValue<ByteBuffer[], long[]> storeEntities(long timestamp, Chunk chunkData);

	public void rollbackChunkStorage();

	public void commitChunkStorage();

	public void postStoreUpdateEntityCache(ByteBuffer[] chunks, long[] chunksStoragePositions)
		throws InterruptedException;

	public StorageInventory readStorage();

	public boolean issuedGarbageCollection(long nanoTimeBudget);

	public boolean issuedFileCleanupCheck(long nanoTimeBudget);

	public boolean issuedEntityCacheCheck(long nanoTimeBudget, StorageEntityCacheEvaluator entityEvaluator);

	public void exportData(StorageLiveFileProvider fileProvider);

	// (19.07.2014 TM)TODO: refactor storage typing to avoid classes in public API
	public StorageEntityCache.Default prepareImportData();

	public void importData(StorageImportSource importSource);

	public void rollbackImportData(Throwable cause);

	public void commitImportData(long taskTimestamp);

	public KeyValue<Long, Long> exportTypeEntities(StorageEntityTypeHandler type, AWritableFile file)
		throws IOException;

	public KeyValue<Long, Long> exportTypeEntities(
		StorageEntityTypeHandler         type           ,
		AWritableFile                    file           ,
		Predicate<? super StorageEntity> predicateEntity
	) throws IOException;

	public StorageRawFileStatistics.ChannelStatistics createRawFileStatistics();

	public StorageIdAnalysis initializeStorage(
		long             taskTimestamp           ,
		long             consistentStoreTimestamp,
		StorageInventory storageInventory
	);

	public void signalGarbageCollectionSweepCompleted();

//	public void truncateData();

	public void cleanupStore();


	

	public final class Default implements StorageChannel, Unpersistable, StorageHousekeepingExecutor
	{
		private final static Logger logger = Logging.getLogger(Default.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final int                           channelIndex             ;
		private final StorageExceptionHandler       exceptionHandler         ;
		private final StorageTaskBroker             taskBroker               ;
		private final StorageOperationController    operationController      ;
		private final StorageHousekeepingController housekeepingController   ;
		private final StorageHousekeepingBroker     housekeepingBroker       ;
		private final StorageFileManager.Default    fileManager              ;
		private final StorageEntityCache.Default    entityCache              ;
		private final boolean                       switchByteOrder          ;
		private final BufferSizeProviderIncremental loadingBufferSizeProvider;
		private final StorageEventLogger            eventLogger              ;

		private final HousekeepingTask[] housekeepingTasks;
		
		private int nextHouseKeepingIndex;

		/**
		 * A nanosecond timestamp marking the calculated end of the current housekeeping interval.
		 * @see {@link StorageHousekeepingController#housekeepingIntervalMs()}
		 */
		private long housekeepingIntervalBoundTimeNs;

		/**
		 * The remaining housekeeping budget in nanoseconds for the current interval.
		 * @see StorageHousekeepingController#housekeepingTimeBudgetNs()
		 */
		private long housekeepingIntervalBudgetNs;
		
		private boolean active;
		


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final int                           hashIndex                ,
			final StorageExceptionHandler       exceptionHandler         ,
			final StorageTaskBroker             taskBroker               ,
			final StorageOperationController    operationController      ,
			final StorageHousekeepingBroker     housekeepingBroker       ,
			final StorageHousekeepingController housekeepingController   ,
			final StorageEntityCache.Default    entityCache              ,
			final boolean                       switchByteOrder          ,
			final BufferSizeProviderIncremental loadingBufferSizeProvider,
			final StorageFileManager.Default    fileManager              ,
			final StorageEventLogger            eventLogger
		)
		{
			super();
			this.channelIndex              = notNegative(hashIndex)                ;
			this.exceptionHandler          =     notNull(exceptionHandler)         ;
			this.taskBroker                =     notNull(taskBroker)               ;
			this.operationController       =     notNull(operationController)      ;
			this.housekeepingBroker        =     notNull(housekeepingBroker)       ;
			this.fileManager               =     notNull(fileManager)              ;
			this.entityCache               =     notNull(entityCache)              ;
			this.housekeepingController    =     notNull(housekeepingController)   ;
			this.loadingBufferSizeProvider =     notNull(loadingBufferSizeProvider);
			this.eventLogger               =     notNull(eventLogger)              ;
			this.switchByteOrder           =             switchByteOrder           ;
			
			// depends on this.fileManager!
			this.housekeepingTasks = this.defineHouseKeepingTasks();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////
		
		private HousekeepingTask[] defineHouseKeepingTasks()
		{
			final BulkList<HousekeepingTask> tasks = BulkList.New();
			tasks.add(this::houseKeepingCheckFileCleanup);
			tasks.add(this::houseKeepingGarbageCollection);
			tasks.add(this::houseKeepingEntityCacheCheck);
			// (16.06.2020 TM)TODO: priv#49: housekeeping task that closes data files after a timeout.

			return tasks.toArray(HousekeepingTask.class);
		}

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
					+ Storage.millisecondsToNanoseconds(this.housekeepingController.housekeepingIntervalMs())
				;
				this.housekeepingIntervalBudgetNs = this.housekeepingController.housekeepingTimeBudgetNs();
			}
			else if(this.housekeepingIntervalBudgetNs <= 0)
			{
				return;
			}
			else if(this.housekeepingIntervalBoundTimeNs - currentNanotime < this.housekeepingIntervalBudgetNs)
			{
				// cap remaining housekeeping budget at the current interval's housekeeping time bound
				this.housekeepingIntervalBudgetNs = this.housekeepingIntervalBoundTimeNs - currentNanotime;
			}

			final long budgetOffset = currentNanotime + this.housekeepingIntervalBudgetNs;


			// execute every task once at most per cycle (therefore the counter, but NOT for selecting the task)
			for(int c = 0; c < this.housekeepingTasks.length; c++)
			{
				// call the next task (next from last cycle or just another one if there is still time)
				this.housekeepingTasks[this.getCurrentHouseKeepingIndexAndAdvance()].perform();

				// intentionally checked AFTER the first housekeeping task to guarantee at least one task to be executed
				if((this.housekeepingIntervalBudgetNs = budgetOffset - System.nanoTime()) <= 0)
				{
					break;
				}
			}

		}
		
		@Override
		public boolean performIssuedGarbageCollection(final long nanoTimeBudget)
		{
			logger.trace("StorageChannel#{} performing issued garbage collection", this.channelIndex);
			
			// turn budget into the budget bounding value for easier and faster checking
			final long nanoTimeBudgetBound = XTime.calculateNanoTimeBudgetBound(nanoTimeBudget);

			return this.entityCache.issuedGarbageCollection(nanoTimeBudgetBound, this);
		}
		
		@Override
		public boolean performIssuedFileCleanupCheck(final long nanoTimeBudget)
		{
			if(!this.fileManager.isFileCleanupEnabled())
			{
				return true;
			}
			
			logger.trace("StorageChannel#{} performing issued file cleanup check", this.channelIndex);
			
			// turn budget into the budget bounding value for easier and faster checking
			final long nanoTimeBudgetBound = XTime.calculateNanoTimeBudgetBound(nanoTimeBudget);
			
			return this.fileManager.issuedFileCleanupCheck(nanoTimeBudgetBound);
		}
		
		@Override
		public boolean performIssuedEntityCacheCheck(
			final long                        nanoTimeBudget,
			final StorageEntityCacheEvaluator evaluator
		)
		{
			logger.trace("StorageChannel#{} performing issued entity cache check", this.channelIndex);
			
			// turn budget into the budget bounding value for easier and faster checking
			final long nanoTimeBudgetBound = XTime.calculateNanoTimeBudgetBound(nanoTimeBudget);

			return this.entityCache.issuedEntityCacheCheck(nanoTimeBudgetBound, evaluator);
		}

		@Override
		public final boolean performFileCleanupCheck(final long nanoTimeBudget)
		{
			if(!this.fileManager.isFileCleanupEnabled())
			{
				return true;
			}
			
			logger.trace("StorageChannel#{} performing incremental file cleanup check", this.channelIndex);
			
			// turn budget into the budget bounding value for easier and faster checking
			final long nanoTimeBudgetBound = XTime.calculateNanoTimeBudgetBound(nanoTimeBudget);
			
			return this.fileManager.incrementalFileCleanupCheck(nanoTimeBudgetBound);
		}
		
		@Override
		public boolean performGarbageCollection(final long nanoTimeBudget)
		{
			logger.trace("StorageChannel#{} performing incremental garbage collection", this.channelIndex);
			
			// turn budget into the budget bounding value for easier and faster checking
			final long nanoTimeBudgetBound = XTime.calculateNanoTimeBudgetBound(nanoTimeBudget);
			
			return this.entityCache.incrementalGarbageCollection(nanoTimeBudgetBound, this);
		}
		
		@Override
		public boolean performEntityCacheCheck(
			final long nanoTimeBudget
		)
		{
			logger.trace("StorageChannel#{} performing incremental entity cache check", this.channelIndex);
			
			// turn budget into the budget bounding value for easier and faster checking
			final long nanoTimeBudgetBound = XTime.calculateNanoTimeBudgetBound(nanoTimeBudget);
			
			return this.entityCache.incrementalEntityCacheCheck(nanoTimeBudgetBound);
		}
		
		@Override
		public final boolean issuedGarbageCollection(final long nanoTimeBudget)
		{
			return this.housekeepingBroker.performIssuedGarbageCollection(this, nanoTimeBudget);
		}

		@Override
		public boolean issuedFileCleanupCheck(final long nanoTimeBudget)
		{
			return this.housekeepingBroker.performIssuedFileCleanupCheck(this, nanoTimeBudget);
		}

		@Override
		public boolean issuedEntityCacheCheck(
			final long                        nanoTimeBudget,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			return this.housekeepingBroker.performIssuedEntityCacheCheck(this, nanoTimeBudget, entityEvaluator);
		}
		
		private long calculateSpecificHousekeepingTimeBudget(final long nanoTimeBudget)
		{
			return Math.min(nanoTimeBudget, this.housekeepingIntervalBudgetNs);
		}

		final boolean houseKeepingCheckFileCleanup()
		{
			if(!this.fileManager.isFileCleanupEnabled())
			{
				return true;
			}
			
			final long nanoTimeBudget = this.calculateSpecificHousekeepingTimeBudget(
				this.housekeepingController.fileCheckTimeBudgetNs()
			);
			
			return this.housekeepingBroker.performFileCleanupCheck(this, nanoTimeBudget);
		}

		final boolean houseKeepingGarbageCollection()
		{
			final long nanoTimeBudget = this.calculateSpecificHousekeepingTimeBudget(
				this.housekeepingController.garbageCollectionTimeBudgetNs()
			);
			
			return this.housekeepingBroker.performGarbageCollection(this, nanoTimeBudget);
		}

		final boolean houseKeepingEntityCacheCheck()
		{
			final long nanoTimeBudget = this.calculateSpecificHousekeepingTimeBudget(
				this.housekeepingController.liveCheckTimeBudgetNs()
			);
			
			return this.housekeepingBroker.performEntityCacheCheck(this, nanoTimeBudget);
		}

		private void work() throws InterruptedException
		{
			logger.debug("StorageChannel#{} started", this.channelIndex);
			
			final StorageOperationController    operationController    = this.operationController   ;
			final StorageHousekeepingController housekeepingController = this.housekeepingController;

			StorageTask processedTask = new StorageTask.DummyTask();
			StorageTask currentTask   = notNull(this.taskBroker.currentTask());

			while(true)
			{
				// ensure to process every task only once in case no new task came in in time (see below).
				if(currentTask != processedTask)
				{
					currentTask.processBy(this);
					processedTask = currentTask;
				}

				/*
				 * Must check immediately after task processing to abort BEFORE houseKeeping is called in case
				 * of shutdown (otherwise NPE on headFile etc.). So do-while not possible.
				 * Also, may NOT check before task processing as the first task is initializing which in turn
				 * enables channel processing on success. So no simple while condition possible.
				 */
				if(!operationController.checkProcessingEnabled())
				{
					logger.debug("StorageChannel#{} processing disabled", this.channelIndex);
					this.eventLogger.logChannelProcessingDisabled(this);
					break;
				}

				// do a little house keeping, either after a new task or use time if no new task came in.
				/* (29.07.2020 TM)FIXME: priv#361: An exception during housekeeping is fatal
				 * it kills the channel thread and leaves the application thread forever waiting to be
				 * notified.
				 * This has to be covered by a similar mechanism as tasks are.
				 * Or maybe some consolidation of that mechanism has to be done to cover house keeping as well.
				 */
				try
				{
					this.houseKeeping();
				}
				catch(final Throwable t)
				{
					logger.error("StorageChannel#{} encountered disrupting exception", this.channelIndex, t);
					this.eventLogger.logDisruption(this, t);
					this.operationController.setChannelProcessingEnabled(false);
					logger.debug("StorageChannel#{} processing disabled", this.channelIndex);
					this.operationController.registerDisruption(t);
					this.eventLogger.logChannelProcessingDisabled(this);
					break;
				}
				

				// check and wait for the next task to come in
				if((currentTask = processedTask.awaitNext(housekeepingController.housekeepingIntervalMs())) == null)
				{
					// revert to processed task to wait on it again for the next task
					currentTask = processedTask;
				}
			}
			
			logger.debug("StorageChannel#{} stopped", this.channelIndex);
			this.eventLogger.logChannelStoppedWorking(this);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public synchronized boolean isActive()
		{
			return this.active;
		}
		
		private synchronized void activate()
		{
			this.active = true;
		}
		
		private synchronized void deactivate()
		{
			this.active = false;
		}

		@Override
		public final void run()
		{
			// first thing to do
			this.activate();
			
			Throwable workingDisruption = null;
			try
			{
				this.work();
			}
			catch(final Throwable t)
			{
				/*
				 * Note that `t` could be an error or it could even be a checked exception thrown via
				 * Proxy reflective tinkering or Unsafe mechanisms.
				 * However, Throwable cannot be rethrown in Runnable#run() without cheating exception checking again.
				 * Luckily, in this special case, reporting the cause and then dying "silently" is sufficient.
				 *
				 * Note: applies to interruption as well, because on privately managed threads,
				 * interrupting ultimately means just stop running in an ordered fashion
				 */
				workingDisruption = t;
				logger.error("StorageChannel#{} encountered disrupting exception", this.channelIndex, t);
				this.eventLogger.logDisruption(this, t);
				this.exceptionHandler.handleException(t, this);
			}
			finally
			{
				try
				{
					this.dispose();
				}
				catch(final Throwable t1)
				{
					if(workingDisruption != null)
					{
						t1.addSuppressed(workingDisruption);
					}
				}
				finally
				{
					// finally finally: guaranteed last thing to do ever in any case. Ever.
					this.deactivate();
				}
			}
		}

		@Override
		public void commitChunkStorage()
		{
			this.fileManager.commitWrite();
		}

		@Override
		public KeyValue<ByteBuffer[], long[]> storeEntities(final long timestamp, final Chunk chunkData)
		{
			// reset even if there is no new data to account for (potential) new data in other channel
			this.entityCache.registerPendingStoreUpdate();

			final ByteBuffer[] buffers = chunkData.buffers();
			
			// (11.03.2019 TM)FIXME: priv#74: Pre-Write EntityValidator
			
			// set new data flag, even if chunk has no data to account for (potential) data in other channels
			return X.KeyValue(buffers, this.fileManager.storeChunks(timestamp, buffers));
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
		
		private ChunksBuffer createLoadingChunksBuffer(final ChunksBuffer[] channelChunks)
		{
			return this.switchByteOrder
				? ChunksBufferByteReversing.New(channelChunks, this.loadingBufferSizeProvider)
				: ChunksBuffer.New(channelChunks, this.loadingBufferSizeProvider)
			;
		}

		@Override
		public final ChunksBuffer collectLoadByOids(final ChunksBuffer[] resultArray, final PersistenceIdSet loadOids)
		{
			logger.debug("StorageChannel#{} loading {} references", this.channelIndex, loadOids.size());

			/* it is probably best to start (any maybe continue) with lots of small, memory-agile
			 * byte buffers than to estimate one sufficiently huge bulky byte buffer.
			 */
			final ChunksBuffer chunks = this.createLoadingChunksBuffer(resultArray);
			if(!loadOids.isEmpty())
			{
				// progress must have been incremented accordingly at task creation time
				loadOids.iterate(new EntityCollectorByOid(this.entityCache, chunks));
			}
			
			return chunks.complete();
		}

		@Override
		public final ChunksBuffer collectLoadRoots(final ChunksBuffer[] resultArray)
		{
			// pretty straight forward: cram all root instances the entity cache knows of into the buffer
			final ChunksBuffer chunks = this.createLoadingChunksBuffer(resultArray);
			this.entityCache.copyRoots(chunks);
			return chunks.complete();
		}

		@Override
		public final ChunksBuffer collectLoadByTids(final ChunksBuffer[] resultArray, final PersistenceIdSet loadTids)
		{
			final ChunksBuffer chunks = this.createLoadingChunksBuffer(resultArray);
			if(!loadTids.isEmpty())
			{
				// progress must have been incremented accordingly at task creation time
				loadTids.iterate(new EntityCollectorByTid(this.entityCache, chunks));
			}
			return chunks.complete();
		}

		@Override
		public final void exportData(final StorageLiveFileProvider fileProvider)
		{
			this.fileManager.exportData(fileProvider);
		}

		@Override
		public StorageEntityCache.Default prepareImportData()
		{
			this.fileManager.prepareImport();
			return this.entityCache;
		}

		@Override
		public void importData(final StorageImportSource importSource)
		{
			this.fileManager.copyData(importSource);
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
			final StorageEntityTypeHandler         type           ,
			final AWritableFile                    file           ,
			final Predicate<? super StorageEntity> predicateEntity
		)
			throws IOException
		{
			final StorageEntityType.Default entities = this.entityCache.getType(type.typeId());
			if(entities == null || entities.entityCount() == 0)
			{
				return X.KeyValue(0L, 0L);
			}

			final long byteCount = entities.iterateEntities(
				new ThrowingProcedure<StorageEntity.Default, IOException>()
				{
					long byteCount;

					@Override
					public void accept(final StorageEntity.Default e) throws IOException
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
			final StorageEntityTypeHandler type,
			final AWritableFile            file
		)
			throws IOException
		{
			final StorageEntityType.Default entities = this.entityCache.getType(type.typeId());
			if(entities == null || entities.entityCount() == 0)
			{
				return X.KeyValue(0L, 0L);
			}

			final long byteCount = entities.iterateEntities(
				new ThrowingProcedure<StorageEntity.Default, IOException>()
				{
					long byteCount;

					@Override
					public void accept(final StorageEntity.Default e) throws IOException
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
		public final StorageIdAnalysis initializeStorage(
			final long             taskTimestamp           ,
			final long             consistentStoreTimestamp,
			final StorageInventory storageInventory
		)
		{
			return this.fileManager.initializeStorage(
				taskTimestamp           ,
				consistentStoreTimestamp,
				storageInventory        ,
				this
			);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void reset()
		{
			this.entityCache.reset();
			this.fileManager.reset();
		}

		@Override
		public final void signalGarbageCollectionSweepCompleted()
		{
			this.fileManager.restartFileCleanupCursor();
		}

		@Override
		public void cleanupStore()
		{
			this.entityCache.clearPendingStoreUpdate();
		}

		@Override
		public final void dispose()
		{
			this.entityCache.reset();
			this.fileManager.dispose();
		}
	}



	public final class EntityCollectorByOid implements _longProcedure
	{
		// (01.06.2013 TM)TODO: clean up / consolidate all internal implementations

		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageEntityCache.Default entityCache  ;
		private final ChunksBuffer                      dataCollector;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public EntityCollectorByOid(
			final StorageEntityCache.Default entityCache  ,
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
		public final void accept(final long objectId)
		{
			final StorageEntity.Default entry;
			if((entry = this.entityCache.getEntry(objectId)) == null)
			{
				/* (14.01.2015 TM)NOTE: this actually is an error, as every oid request comes
				 * from a referencing entity from inside the same database. So if any load request lookup
				 * yields null, it is an inconsistency that has to be expressed rather sooner than later.
				 *
				 * If some kind of querying request (look if an arbitrary oid yields an entity) is needed,
				 * is has to be a dedicated kind of request, not this one.
				 * This one does recursive graph loading (consistency required), not arbitrary querying
				 * with optional results.
				 */
				throw new StorageExceptionConsistency("No entity found for objectId " + objectId);
			}
			entry.copyCachedData(this.dataCollector);
			this.entityCache.checkForCacheClear(entry, System.currentTimeMillis());
		}

	}

	public final class EntityCollectorByTid implements _longProcedure
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageEntityCache.Default entityCache  ;
		private final ChunksBuffer                      dataCollector;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public EntityCollectorByTid(
			final StorageEntityCache.Default entityCache  ,
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
			final StorageEntityType.Default type;
			if((type = this.entityCache.getType(tid)) == null)
			{
				// it can very well be that a channel does not have a certain type at all. That is no error
				return;
			}

			// all the type's entities are iterated and their data is collected
			for(StorageEntity.Default entity = type.head; (entity = entity.typeNext) != null;)
			{
				entity.copyCachedData(this.dataCollector);
				this.entityCache.checkForCacheClear(entity, System.currentTimeMillis());
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
