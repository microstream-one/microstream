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

public interface StorageRequestTaskCreator
{
	public StorageChannelTaskInitialize createInitializationTask(
		int                        channelCount       ,
		StorageOperationController operationController
	);

	public StorageRequestTaskStoreEntities createSaveTask(
		Binary                     data      ,
		StorageOperationController controller
	);

	public StorageRequestTaskLoadByOids createLoadTaskByOids(
		PersistenceIdSet[]         loadOids  ,
		StorageOperationController controller
	);

	public StorageRequestTaskLoadRoots createRootsLoadTask(
		int                        channelCount,
		StorageOperationController controller
	);
	
	public StorageRequestTaskLoadByTids createLoadTaskByTids(
		PersistenceIdSet           loadTids    , 
		int                        channelCount, 
		StorageOperationController controller
	);

	public default StorageRequestTaskExportEntitiesByType createExportTypesTask(
		final int                                 channelCount      ,
		final StorageEntityTypeExportFileProvider exportFileProvider,
		StorageOperationController                controller
	)
	{
		return this.createExportTypesTask(channelCount, exportFileProvider, controller);
	}
	
	public StorageRequestTaskExportEntitiesByType createExportTypesTask(
		int                                         channelCount      ,
		StorageEntityTypeExportFileProvider         exportFileProvider,
		Predicate<? super StorageEntityTypeHandler> isExportType      ,
		StorageOperationController                  controller
	);

	public StorageRequestTaskExportChannels createTaskExportChannels(
		int                        channelCount,
		StorageLiveFileProvider    fileProvider,
		StorageOperationController controller
	);

	public StorageRequestTaskCreateStatistics createCreateRawFileStatisticsTask(
		int                        channelCount,
		StorageOperationController controller
	);

	public StorageRequestTaskFileCheck createFullFileCheckTask(
		int                        channelCount  ,
		long                       nanoTimeBudget,
		StorageOperationController controller
	);

	public StorageRequestTaskCacheCheck createFullCacheCheckTask(
		int                         channelCount       ,
		long                        nanoTimeBudget     ,
		StorageEntityCacheEvaluator entityEvaluator    ,
		StorageOperationController  operationController
	);

	public StorageRequestTaskImportData createImportFromFilesTask(
		int                           channelCount          ,
		StorageDataFileEvaluator      fileEvaluator         ,
		StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
		XGettingEnum<AFile>           importFiles,
		StorageOperationController    controller
	);

	public StorageChannelTaskShutdown createShutdownTask(
		int                        channelCount       ,
		StorageOperationController operationController
	);



	public final class Default implements StorageRequestTaskCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageTimestampProvider timestampProvider;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final StorageTimestampProvider timestampProvider)
		{
			super();
			this.timestampProvider = notNull(timestampProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public StorageChannelTaskInitialize createInitializationTask(
			final int                        channelCount       ,
			final StorageOperationController operationController
		)
		{
			return new StorageChannelTaskInitialize.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount                                 ,
				operationController
			);
		}

		@Override
		public StorageChannelTaskShutdown createShutdownTask(
			final int                        channelCount       ,
			final StorageOperationController operationController
		)
		{
			return new StorageChannelTaskShutdown.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				operationController
			);
		}

		@Override
		public StorageRequestTaskStoreEntities createSaveTask(
			final Binary                     data      ,
			final StorageOperationController controller
		)
		{
			return new StorageRequestTaskStoreEntities.Default(
				this.timestampProvider.currentNanoTimestamp(),
				data,
				controller
			);
		}

		@Override
		public StorageRequestTaskLoadByOids createLoadTaskByOids(
			final PersistenceIdSet[]   loadOids  ,
			final StorageOperationController controller
		)
		{
			return new StorageRequestTaskLoadByOids.Default(
				this.timestampProvider.currentNanoTimestamp(),
				loadOids,
				controller
			);
		}

		@Override
		public StorageRequestTaskLoadRoots createRootsLoadTask(
			final int                        channelCount,
			final StorageOperationController controller
		)
		{
			return new StorageRequestTaskLoadRoots.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				controller
			);
		}
		
		@Override
		public StorageRequestTaskLoadByTids createLoadTaskByTids(
			final PersistenceIdSet           loadTids, 
			final int                        channelCount, 
			final StorageOperationController controller
		)
		{
			return new StorageRequestTaskLoadByTids.Default(
				this.timestampProvider.currentNanoTimestamp(),
				loadTids,
				channelCount,
				controller
			);
		}

		@Override
		public StorageRequestTaskExportEntitiesByType createExportTypesTask(
			final int                                         channelCount      ,
			final StorageEntityTypeExportFileProvider         exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler> isExportType      ,
			final StorageOperationController                  controller
		)
		{
			return new StorageRequestTaskExportEntitiesByType.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount                                 ,
				exportFileProvider                           ,
				isExportType,
				controller
			);
		}

		@Override
		public StorageRequestTaskExportChannels createTaskExportChannels(
			final int                        channelCount,
			final StorageLiveFileProvider    fileProvider,
			final StorageOperationController controller
		)
		{
			return new StorageRequestTaskExportChannels.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				fileProvider,
				controller
			);
		}

		@Override
		public StorageRequestTaskCreateStatistics createCreateRawFileStatisticsTask(
			final int                        channelCount,
			final StorageOperationController controller
		)
		{
			return new StorageRequestTaskCreateStatistics.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				controller
			);
		}

		@Override
		public StorageRequestTaskFileCheck createFullFileCheckTask(
			final int                        channelCount  ,
			final long                       nanoTimeBudget,
			final StorageOperationController controller
		)
		{
			return new StorageRequestTaskFileCheck.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				nanoTimeBudget,
				controller
			);
		}

		@Override
		public StorageRequestTaskCacheCheck createFullCacheCheckTask(
			final int                         channelCount   ,
			final long                        nanoTimeBudget ,
			final StorageEntityCacheEvaluator entityEvaluator,
			final StorageOperationController  controller
		)
		{
			return new StorageRequestTaskCacheCheck.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				nanoTimeBudget,
				entityEvaluator,
				controller
			);
		}

		@Override
		public StorageRequestTaskImportData createImportFromFilesTask(
			final int                           channelCount          ,
			final StorageDataFileEvaluator      fileEvaluator         ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final XGettingEnum<AFile>           importFiles           ,
			final StorageOperationController    controller
		)
		{
			return new StorageRequestTaskImportData.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				objectIdRangeEvaluator,
				importFiles,
				controller
			);
		}

	}

}
