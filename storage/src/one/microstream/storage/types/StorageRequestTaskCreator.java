package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.file.Path;
import java.util.function.Predicate;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceIdSet;

public interface StorageRequestTaskCreator
{
	public StorageChannelTaskInitialize createInitializationTask(
		int                        channelCount     ,
		StorageOperationController operationController
	);

	public StorageRequestTaskStoreEntities createSaveTask(Binary medium);

	public StorageRequestTaskLoadByOids createLoadTaskByOids(PersistenceIdSet[] loadOids);

	public StorageRequestTaskLoadRoots createRootsLoadTask(int channelCount);
	
	public StorageRequestTaskLoadByTids createLoadTaskByTids(PersistenceIdSet loadTids, int channelCount);

	public default StorageRequestTaskExportEntitiesByType createExportTypesTask(
		final int                                 channelCount      ,
		final StorageEntityTypeExportFileProvider exportFileProvider
	)
	{
		return this.createExportTypesTask(channelCount, exportFileProvider, null);
	}
	
	public StorageRequestTaskExportEntitiesByType createExportTypesTask(
		int                                         channelCount      ,
		StorageEntityTypeExportFileProvider         exportFileProvider,
		Predicate<? super StorageEntityTypeHandler> isExportType
	);

	public StorageRequestTaskExportChannels createTaskExportChannels(
		int                channelCount,
		StorageIoHandler fileHandler
	);

	public StorageRequestTaskCreateStatistics createCreateRawFileStatisticsTask(int channelCount);

	public StorageRequestTaskFileCheck createFullFileCheckTask(
		int                                channelCount  ,
		long                               nanoTimeBudget,
		StorageDataFileDissolvingEvaluator fileDissolver
	);

	public StorageRequestTaskCacheCheck createFullCacheCheckTask(
		int                         channelCount   ,
		long                        nanoTimeBudget ,
		StorageEntityCacheEvaluator entityEvaluator
	);

	public StorageRequestTaskImportData createImportFromFilesTask(
		int                           channelCount          ,
		StorageDataFileEvaluator      fileEvaluator         ,
		StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
		XGettingEnum<Path>            importFiles
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
		public StorageRequestTaskStoreEntities createSaveTask(final Binary medium)
		{
			return new StorageRequestTaskStoreEntities.Default(
				this.timestampProvider.currentNanoTimestamp(),
				medium
			);
		}

		@Override
		public StorageRequestTaskLoadByOids createLoadTaskByOids(final PersistenceIdSet[] loadOids)
		{
			return new StorageRequestTaskLoadByOids.Default(
				this.timestampProvider.currentNanoTimestamp(),
				loadOids
			);
		}

		@Override
		public StorageRequestTaskLoadRoots createRootsLoadTask(final int channelCount)
		{
			return new StorageRequestTaskLoadRoots.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount
			);
		}
		
		@Override
		public StorageRequestTaskLoadByTids createLoadTaskByTids(final PersistenceIdSet loadTids, final int channelCount)
		{
			return new StorageRequestTaskLoadByTids.Default(
				this.timestampProvider.currentNanoTimestamp(),
				loadTids,
				channelCount
			);
		}

		@Override
		public StorageRequestTaskExportEntitiesByType createExportTypesTask(
			final int                                         channelCount      ,
			final StorageEntityTypeExportFileProvider         exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler> isExportType
		)
		{
			return new StorageRequestTaskExportEntitiesByType.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount                                 ,
				exportFileProvider                           ,
				isExportType
			);
		}

		@Override
		public StorageRequestTaskExportChannels createTaskExportChannels(
			final int              channelCount,
			final StorageIoHandler fileHandler
		)
		{
			return new StorageRequestTaskExportChannels.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				fileHandler
			);
		}

		@Override
		public StorageRequestTaskCreateStatistics createCreateRawFileStatisticsTask(final int channelCount)
		{
			return new StorageRequestTaskCreateStatistics.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount
			);
		}

		@Override
		public StorageRequestTaskFileCheck createFullFileCheckTask(
			final int                                channelCount       ,
			final long                               nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolver
		)
		{
			return new StorageRequestTaskFileCheck.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				nanoTimeBudgetBound,
				fileDissolver
			);
		}

		@Override
		public StorageRequestTaskCacheCheck createFullCacheCheckTask(
			final int                         channelCount       ,
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			return new StorageRequestTaskCacheCheck.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				nanoTimeBudgetBound,
				entityEvaluator
			);
		}

		@Override
		public StorageRequestTaskImportData createImportFromFilesTask(
			final int                           channelCount          ,
			final StorageDataFileEvaluator      fileEvaluator         ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final XGettingEnum<Path>            importFiles
		)
		{
			return new StorageRequestTaskImportData.Default(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				objectIdRangeEvaluator,
				importFiles
			);
		}

	}

}
