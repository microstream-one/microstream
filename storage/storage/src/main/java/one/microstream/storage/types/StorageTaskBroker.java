package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.util.function.Predicate;

import one.microstream.afs.types.AFile;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionNotRunning;
import one.microstream.util.UtilStackTrace;

public interface StorageTaskBroker
{
	public StorageTask currentTask();

	public StorageRequestTaskLoadRoots enqueueRootsLoadTask()
		throws InterruptedException;

	public StorageRequestTaskLoadByTids enqueueLoadTaskByTids(PersistenceIdSet loadTids)
		throws InterruptedException;

	public StorageRequestTaskLoadByOids enqueueLoadTaskByOids(PersistenceIdSet[] loadOids)
		throws InterruptedException;
	
	public StorageRequestTaskStoreEntities enqueueStoreTask(Binary data)
		throws InterruptedException;

	public default StorageRequestTaskExportEntitiesByType enqueueExportTypesTask(
		final StorageEntityTypeExportFileProvider exportFileProvider
	)
		throws InterruptedException
	{
		return this.enqueueExportTypesTask(exportFileProvider, null);
	}
	
	public StorageRequestTaskExportEntitiesByType enqueueExportTypesTask(
		StorageEntityTypeExportFileProvider         exportFileProvider,
		Predicate<? super StorageEntityTypeHandler> isExportType
	)
		throws InterruptedException;
	
	

	public StorageRequestTask enqueueExportChannelsTask(
		StorageLiveFileProvider fileProvider             ,
		boolean             performGarbageCollection
	)
		throws InterruptedException;

	public StorageRequestTask enqueueImportFromFilesTask(XGettingEnum<AFile> importFiles)
		throws InterruptedException;

	public StorageRequestTaskCreateStatistics enqueueCreateRawFileStatisticsTask()
		throws InterruptedException;

	public StorageChannelTaskInitialize issueChannelInitialization(
		StorageOperationController operationController
	)
		throws InterruptedException;

	public StorageChannelTaskShutdown issueChannelShutdown(StorageOperationController operationController)
		throws InterruptedException;

	public StorageRequestTaskGarbageCollection issueGarbageCollection(long nanoTimeBudget)
		throws InterruptedException;

	public StorageRequestTaskFileCheck issueFileCheck(long nanoTimeBudget)
		throws InterruptedException;

	public StorageRequestTaskCacheCheck issueCacheCheck(
		long                        nanoTimeBudget ,
		StorageEntityCacheEvaluator entityEvaluator
	)
		throws InterruptedException;

	public StorageOperationController operationController();

	public final class Default implements StorageTaskBroker
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// can't have a strong reference to StorageManager since that would prevent automatic shutdown
		private final StorageOperationController    operationController   ;
		private final StorageDataFileEvaluator      fileEvaluator         ;
		private final StorageObjectIdRangeEvaluator objectIdRangeEvaluator;
		private final StorageRequestTaskCreator     taskCreator           ;
		private final int                           channelCount          ;

		private volatile StorageTask currentHead;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageRequestTaskCreator     taskCreator           ,
			final StorageOperationController    operationController   ,
			final StorageDataFileEvaluator      fileEvaluator         ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final int                           channelCount
		)
		{
			super();
			this.taskCreator            = notNull(taskCreator);
			this.operationController    = notNull(operationController);
			this.fileEvaluator          = notNull(fileEvaluator);
			this.objectIdRangeEvaluator = notNull(objectIdRangeEvaluator);
			this.channelCount           =         channelCount;
			this.currentHead            = new StorageTask.DummyTask();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		private StorageRequestTaskGarbageCollection enqueueTaskPrependingFullGc(
			final StorageTask task          ,
			final long        nanoTimeBudget
		)
			throws InterruptedException
		{
			final StorageRequestTaskGarbageCollection gcTask;
			this.enqueueTasksAndNotifyAll(
				gcTask = new StorageRequestTaskGarbageCollection.Default(
					task.timestamp() - 1,
					this.channelCount   ,
					nanoTimeBudget      ,
					task                , 
					operationController
				),
				task
			);
			return gcTask;
		}

		private synchronized void enqueueTasksAndNotifyAll(
			final StorageTask firstTask ,
			final StorageTask secondTask
		)
			throws InterruptedException
		{
			/* The first task is the next task to be processed, the second task is the new head task, i.e.
			 * the new last task that gets future tasks attached to.
			 * It is the first task's responsibility to (eventually) lead to the second task in order to
			 * close the task chain.
			 */
			final StorageTask currentHead = this.enqueueTask(firstTask, secondTask);

			// notifiy waiting threads via current head
			synchronized(currentHead)
			{
//				XDebug.debugln(Thread.currentThread() + " notifying all");
				currentHead.notifyAll();
			}
		}

		private void enqueueTaskAndNotifyAll(final StorageTask task) throws InterruptedException
		{
			final StorageTask currentHead = this.enqueueTask(task);
			synchronized(currentHead)
			{
//				XDebug.debugln(Thread.currentThread() + " notifying all");
				currentHead.notifyAll();
			}
		}

		private StorageTask enqueueTask(final StorageTask task)
		{
			return this.enqueueTask(task, task);
//			final StorageTask currentHead;
//			(currentHead = this.currentHead).setNext(task);
//			this.currentHead = task;
//			return currentHead;
		}

		private StorageTask enqueueTask(final StorageTask nextTask, final StorageTask newHeadTask)
		{
			/* (12.06.2019 TM)NOTE:
			 * prevents application threads from waiting forever for a storage
			 * that is already shutdown due to an error (e.g. IO-location not reachable).
			 */
			if(!this.operationController.checkProcessingEnabled())
			{
				throw new StorageExceptionNotRunning("Storage is shut down.");
			}
			
			return this.uncheckedEnqueueTask(nextTask, newHeadTask);
		}
		
		private StorageTask uncheckedEnqueueTask(final StorageTask nextTask, final StorageTask newHeadTask)
		{
			/* (15.02.2019 TM)FIXME: That single-head queue is dangerous. Probably the source for some hangups.
			 * Just build a proper queue with head and tail, ffs.
			 */
			final StorageTask currentHead;
			(currentHead = this.currentHead).setNext(nextTask);
			this.currentHead = newHeadTask;
			return currentHead;
		}

		@Override
		public final StorageTask currentTask()
		{
			return this.currentHead;
		}

		@Override
		public final synchronized StorageRequestTaskGarbageCollection issueGarbageCollection(
			final long nanoTimeBudget
		)
			throws InterruptedException
		{
			final StorageRequestTask dummy =
				new StorageChannelSynchronizingTask.AbstractCompletingTask.Dummy(this.channelCount, operationController)
			;
			final StorageRequestTaskGarbageCollection gcTask =
				this.enqueueTaskPrependingFullGc(dummy, nanoTimeBudget)
			;
			return gcTask;
		}

		@Override
		public final synchronized StorageRequestTaskCacheCheck issueCacheCheck(
			final long                        nanoTimeBudget ,
			final StorageEntityCacheEvaluator entityEvaluator
		)
			throws InterruptedException
		{
			final StorageRequestTaskCacheCheck task = this.taskCreator.createFullCacheCheckTask(
				this.channelCount,
				nanoTimeBudget,
				entityEvaluator, 
				operationController
			);
			this.enqueueTaskAndNotifyAll(task);
			return task;
		}

		@Override
		public final synchronized StorageRequestTaskFileCheck issueFileCheck(
			final long nanoTimeBudget
		)
			throws InterruptedException
		{
			final StorageRequestTaskFileCheck task = this.taskCreator.createFullFileCheckTask(
				this.channelCount,
				nanoTimeBudget,
				operationController
			);
			this.enqueueTaskAndNotifyAll(task);
			return task;
		}

		@Override
		public final synchronized StorageRequestTask enqueueExportChannelsTask(
			final StorageLiveFileProvider fileProvider             ,
			final boolean             performGarbageCollection
		)
			throws InterruptedException
		{
			final StorageRequestTaskExportChannels task = this.taskCreator.createTaskExportChannels(
				this.channelCount,
				fileProvider,
				operationController
			);

			/*
			 * If the data shall "just" be exported as fast as possible and potential unreachable entities
			 * are not a problem, then not performing the GC is perferable.
			 * If the exported data shall represent a definite minimum of all reachable entities and the
			 * required time for a full GC is not an issue (e.g. nightly chronjob), then performing the GC
			 * is preferable.
			 * Both cases are equally viable depending on the situation. Hence the required flag.
			 */
			if(performGarbageCollection)
			{
				// enqueue task with a prepended full GC
				this.enqueueTaskPrependingFullGc(task, Long.MAX_VALUE); // must let GC complete to get viable results
			}
			else
			{
				// enqueue task directly
				this.enqueueTaskAndNotifyAll(task);
			}

			/*
			 * in both cases, the actual task is the last to be processed, so the calling thread
			 * must always wait on the actual task.
			 */

			return task;
		}

		@Override
		public StorageRequestTask enqueueImportFromFilesTask(final XGettingEnum<AFile> importFiles)
			throws InterruptedException
		{
			// always use the internal evaluator to match live operation
			final StorageRequestTaskImportData task = this.taskCreator.createImportFromFilesTask(
				this.channelCount          ,
				this.fileEvaluator         ,
				this.objectIdRangeEvaluator,
				importFiles,
				operationController
			);
			this.enqueueTaskAndNotifyAll(task);
			return task;
		}

		@Override
		public StorageRequestTaskCreateStatistics enqueueCreateRawFileStatisticsTask() throws InterruptedException
		{
			final StorageRequestTaskCreateStatistics task = this.taskCreator.createCreateRawFileStatisticsTask(
				this.channelCount, operationController
			);
			this.enqueueTaskAndNotifyAll(task);
			return task;
		}


		@Override
		public final synchronized StorageRequestTaskExportEntitiesByType enqueueExportTypesTask(
			final StorageEntityTypeExportFileProvider         exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler> isExportType
		)
			throws InterruptedException
		{
			final StorageRequestTaskExportEntitiesByType task = this.taskCreator.createExportTypesTask(
				this.channelCount ,
				exportFileProvider,
				isExportType,
				operationController
			);

			// must let GC complete to get viable results
			this.enqueueTaskPrependingFullGc(task, Long.MAX_VALUE);
//			DEBUGStorage.println("disabled type export prepended Gc!");
//			this.enqueueTaskAndNotifyAll(task);

			// return actual task
			return task;
		}
		
		/**
		 * The task broker cannot rely on any outside logic to pass an array with valid length or validate its length.
		 * Every channel-count-depending array must be validated right before it is enqueued as a task to prevent
		 * the system from crashing.
		 */
		private void validateChannelCount(final int channelCount)
		{
			if(channelCount != this.channelCount)
			{
				throw UtilStackTrace.cutStacktraceByOne(new StorageException(
					"Invalid channel count, given: " + channelCount +
					", expected: " + this.channelCount
				));
			}
		}

		@Override
		public final synchronized StorageRequestTaskStoreEntities enqueueStoreTask(final Binary data)
			throws InterruptedException
		{
			this.validateChannelCount(data.channelCount());
			
			// task creation must be called AFTER acquiring the lock to ensure temporal consistency in the task chain
			final StorageRequestTaskStoreEntities task = this.taskCreator.createSaveTask(data, operationController);
			
//			((StorageRequestTaskSaveEntities.Default)task).DEBUG_Print(null);
			
			this.enqueueTaskAndNotifyAll(task);
			return task;
		}

		@Override
		public final synchronized StorageRequestTaskLoadByOids enqueueLoadTaskByOids(
			final PersistenceIdSet[] loadOids
		)
			throws InterruptedException
		{
			this.validateChannelCount(loadOids.length);
			
			// task creation must be called AFTER acquiring the lock to ensure temporal consistency in the task chain
			final StorageRequestTaskLoadByOids task = this.taskCreator.createLoadTaskByOids(loadOids, operationController);
			this.enqueueTaskAndNotifyAll(task);
			return task;
		}

		@Override
		public final synchronized StorageRequestTaskLoadRoots enqueueRootsLoadTask() throws InterruptedException
		{
			// task creation must be called AFTER acquiring the lock to ensure temporal consistency in the task chain
			final StorageRequestTaskLoadRoots task = this.taskCreator.createRootsLoadTask(this.channelCount, operationController);
			this.enqueueTaskAndNotifyAll(task);
			return task;
		}

		@Override
		public final synchronized StorageRequestTaskLoadByTids enqueueLoadTaskByTids(
			final PersistenceIdSet loadTids
		)
			throws InterruptedException
		{
			// task creation must be called AFTER acquiring the lock to ensure temporal consistency in the task chain
			final StorageRequestTaskLoadByTids task = this.taskCreator.createLoadTaskByTids(loadTids, this.channelCount, operationController);
			this.enqueueTaskAndNotifyAll(task);
			return task;
		}

		@Override
		public final synchronized StorageChannelTaskInitialize issueChannelInitialization(
			final StorageOperationController operationController

		)
			throws InterruptedException
		{
			final StorageChannelTaskInitialize task = this.taskCreator.createInitializationTask(
				this.channelCount  ,
				operationController
			);
			
			/* (12.06.2019 TM)NOTE:
			 * Even more special case:
			 * Cannot check for running storage in the initialization that will cause it to run.
			 * Plus the old special case:
			 * Cannot wait on the task before the channel threads are started
			 */
			final StorageTask currentHead = this.uncheckedEnqueueTask(task, task);
			synchronized(currentHead)
			{
				currentHead.notifyAll();
			}
			
			return task;
		}

		@Override
		public final synchronized StorageChannelTaskShutdown issueChannelShutdown(
			final StorageOperationController operationController
		)
			throws InterruptedException
		{
			final StorageChannelTaskShutdown task = this.taskCreator.createShutdownTask(
				this.channelCount  ,
				operationController
			);
			// special case: cannot wait on the task before the channel threads are started
			this.enqueueTaskAndNotifyAll(task);
			return task;
		}

		@Override
		public StorageOperationController operationController() {
			return this.operationController;
		}

	}

	public interface Creator
	{
		public StorageTaskBroker createTaskBroker(
			StorageSystem             storageSystem,
			StorageRequestTaskCreator taskCreator
		);



		public final class Default implements Creator
		{
			public Default()
			{
				super();
			}
			
			@Override
			public StorageTaskBroker createTaskBroker(
				final StorageSystem             storageSystem,
				final StorageRequestTaskCreator taskCreator
			)
			{
				return new StorageTaskBroker.Default(
					taskCreator,
					storageSystem.operationController(),
					storageSystem.configuration().dataFileEvaluator(),
					storageSystem.objectIdRangeEvaluator(),
					storageSystem.channelCountProvider().getChannelCount()
				);
			}

		}
		
	}

}
