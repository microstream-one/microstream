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

import java.nio.ByteBuffer;
import java.util.function.Predicate;

import one.microstream.afs.types.AFile;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.storage.exceptions.StorageExceptionRequest;


public interface StorageRequestAcceptor
{
	// storing //

	public void storeData(Binary data) throws StorageExceptionRequest, InterruptedException;

	// querying //

	public Binary queryByObjectIds(PersistenceIdSet[] loadOids) throws StorageExceptionRequest, InterruptedException;

	/*
	 * required for refactoring: all instances of a certain type have to be queried, modified and stored again.
	 * (22.06.2015 TM)NOTE: Also required to initialize a substituter cache.
	 * E.g. load ALL string instance at system start, reuse existing fitting strings in business logic instead of
	 * creating redundant new ones. Of course, this method will have to be used carefully.
	 * Loading all instances of an entity type can quickly result in the whole database being loaded.
	 * That responsibility must reside with the user.
	 */
	public Binary queryByTypeIds(PersistenceIdSet loadTids) throws StorageExceptionRequest, InterruptedException;

	/* (23.06.2015 TM)TODO: queryTypeStatistics
	 * how many entities per length in each type.
	 * useful/important for:
	 * - deciding on export or such
	 * - deciding on querying all entities per type
	 * - creating statistics on large value types (e.g. images) without having to actually load all of them.
	 * Fixed length types are of course trivial (one entry with total entity count)
	 */

	/* (06.07.2015 TM)TODO: Entity content refactor function
	 * Function that gets passed an entity and returns null (no change) or a bytebuffer with the new content
	 * to be stored.
	 * Required/useful for:
	 * - internal automatic refactoring functionality
	 * - manual truncation of large data for e.g. clear a test database of large binaries / prim arrays / strings
	 */

	public Binary recallRoots() throws StorageExceptionRequest, InterruptedException;

	public boolean issueGarbageCollection(long nanoTimeBudget) throws InterruptedException;

	public boolean issueFileCheck(long nanoTimeBudget) throws InterruptedException;

	public boolean issueCacheCheck(long nanoTimeBudget, StorageEntityCacheEvaluator entityEvaluator)
		throws InterruptedException;

	// exporting //

	public default StorageEntityTypeExportStatistics exportTypes(final StorageEntityTypeExportFileProvider exportFileProvider)
		throws InterruptedException
	{
		return this.exportTypes(exportFileProvider, null);
	}
	
	public StorageEntityTypeExportStatistics exportTypes(
		StorageEntityTypeExportFileProvider         exportFileProvider,
		Predicate<? super StorageEntityTypeHandler> isExportType
		
	)
		throws InterruptedException
	;
	

	public void exportChannels(StorageLiveFileProvider fileProvider, boolean performGarbageCollection)
		throws InterruptedException;

	public void importFiles(XGettingEnum<AFile> importFiles) throws InterruptedException;

	public void importData(XGettingEnum<ByteBuffer> importFiles) throws InterruptedException;

	public StorageRawFileStatistics createStatistics() throws InterruptedException;



	public interface Creator
	{
		public StorageRequestAcceptor createRequestAcceptor(
			StorageDataChunkValidator dataChunkValidator,
			StorageTaskBroker         taskBroker
		);


		public final class Default implements Creator
		{
			@Override
			public StorageRequestAcceptor createRequestAcceptor(
				final StorageDataChunkValidator dataChunkValidator,
				final StorageTaskBroker         taskBroker
			)
			{
				return new StorageRequestAcceptor.Default(dataChunkValidator, taskBroker);
			}

		}

	}



	public final class Default implements StorageRequestAcceptor
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageTaskBroker         taskBroker           ;
		private final StorageDataChunkValidator prevalidatorDataChunk;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final StorageDataChunkValidator dataChunkValidator,
			final StorageTaskBroker         taskBroker
		)
		{
			super();
			this.prevalidatorDataChunk = notNull(dataChunkValidator);
			this.taskBroker            = notNull(taskBroker)        ;
		}


		private static <T extends StorageRequestTask> T waitOnTask(final T task) throws InterruptedException
		{
			task.waitOnCompletion();
			return task;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void storeData(final Binary data) throws InterruptedException
		{
			// note: enabled accepting tasks has to be checked prior to calling this method (external concern)

			// prevalidate on the caller site before creating and enqueing a task (may be no-op)
			this.prevalidatorDataChunk.validateDataChunk(data);

			waitOnTask(this.taskBroker.enqueueStoreTask(data));
		}

		@Override
		public final Binary queryByObjectIds(final PersistenceIdSet[] loadOids) throws InterruptedException
		{
			// note: enabled accepting tasks has to be checked prior to calling this method (external concern)

			// no need for a plain-oid prevalidator at this point (i.e. invalid OIDs are no error, just yield nothing)

			// create and enqueue new chunk entry for task broker
//			return this.enqueueAndWaitOnTask(this.taskCreator.createLoadTask(loadOids)).result();
			return waitOnTask(this.taskBroker.enqueueLoadTaskByOids(loadOids)).result();
		}

		@Override
		public Binary queryByTypeIds(final PersistenceIdSet loadTids) throws StorageExceptionRequest, InterruptedException
		{
			return waitOnTask(this.taskBroker.enqueueLoadTaskByTids(loadTids)).result();
		}

		@Override
		public Binary recallRoots() throws StorageExceptionRequest, InterruptedException
		{
			return waitOnTask(this.taskBroker.enqueueRootsLoadTask()).result();
		}

		@Override
		public boolean issueGarbageCollection(final long nanoTimeBudget) throws InterruptedException
		{
			return waitOnTask(this.taskBroker.issueGarbageCollection(nanoTimeBudget)).result();
		}

		@Override
		public boolean issueCacheCheck(
			final long                        nanoTimeBudget,
			final StorageEntityCacheEvaluator entityEvaluator
		)
			throws InterruptedException
		{
			return waitOnTask(this.taskBroker.issueCacheCheck(nanoTimeBudget, entityEvaluator)).result();
		}

		@Override
		public boolean issueFileCheck(final long nanoTimeBudget)
			throws InterruptedException
		{
			return waitOnTask(this.taskBroker.issueFileCheck(nanoTimeBudget)).result();
		}

		@Override
		public final StorageEntityTypeExportStatistics exportTypes(
			final StorageEntityTypeExportFileProvider         exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler> isExportType
		)
			throws InterruptedException
		{
			return waitOnTask(this.taskBroker.enqueueExportTypesTask(exportFileProvider, isExportType)).result();
		}

		@Override
		public final void exportChannels(
			final StorageLiveFileProvider fileProvider           ,
			final boolean             performGarbageCollection
		)
			throws InterruptedException
		{
			waitOnTask(this.taskBroker.enqueueExportChannelsTask(fileProvider, performGarbageCollection));
		}

		@Override
		public StorageRawFileStatistics createStatistics() throws InterruptedException
		{
			return waitOnTask(this.taskBroker.enqueueCreateRawFileStatisticsTask()).result();
		}

		@Override
		public void importFiles(final XGettingEnum<AFile> importFiles) throws InterruptedException
		{
			waitOnTask(this.taskBroker.enqueueImportFromFilesTask(importFiles));
		}
		
		@Override
		public void importData(final XGettingEnum<ByteBuffer> importData) throws InterruptedException
		{
			waitOnTask(this.taskBroker.enqueueImportFromByteBuffersTask(importData));
		}

	}

}
