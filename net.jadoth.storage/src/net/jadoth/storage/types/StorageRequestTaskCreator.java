package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.File;
import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.binary.types.Chunks;
import net.jadoth.swizzling.types.SwizzleIdSet;

public interface StorageRequestTaskCreator
{
	public StorageChannelTaskInitialize createInitializationTask(
		int                      channelCount     ,
		StorageChannelController channelController
	);

	public StorageRequestTaskStoreEntities createSaveTask(Chunks[] medium);

	public StorageRequestTaskLoadByOids createLoadTaskByOids(SwizzleIdSet[] loadOids);

	public StorageRequestTaskLoadRoots createRootsLoadTask(int channelCount);
	
	public StorageRequestTaskLoadByTids createLoadTaskByTids(SwizzleIdSet loadTids, int channelCount);

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
		XGettingEnum<File>            importFiles
	);

	public StorageChannelTaskShutdown createShutdownTask(
		int                      channelCount     ,
		StorageChannelController channelController
	);

	public StorageChannelTaskTruncateData createTruncateTask(
		int                      channelCount     ,
		StorageChannelController channelController
	);



	public final class Implementation implements StorageRequestTaskCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageTimestampProvider timestampProvider;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final StorageTimestampProvider timestampProvider)
		{
			super();
			this.timestampProvider = notNull(timestampProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public StorageChannelTaskInitialize createInitializationTask(
			final int                      channelCount     ,
			final StorageChannelController channelController
		)
		{
			return new StorageChannelTaskInitialize.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount                                 ,
				channelController
			);
		}

		@Override
		public StorageChannelTaskShutdown createShutdownTask(
			final int channelCount,
			final StorageChannelController channelController
		)
		{
			return new StorageChannelTaskShutdown.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				channelController
			);
		}

		@Override
		public StorageChannelTaskTruncateData createTruncateTask(
			final int                      channelCount     ,
			final StorageChannelController channelController
		)
		{
			return new StorageChannelTaskTruncateData.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				channelController
			);
		}

		@Override
		public StorageRequestTaskStoreEntities createSaveTask(final Chunks[] medium)
		{
			return new StorageRequestTaskStoreEntities.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				medium
			);
		}

		@Override
		public StorageRequestTaskLoadByOids createLoadTaskByOids(final SwizzleIdSet[] loadOids)
		{
			return new StorageRequestTaskLoadByOids.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				loadOids
			);
		}

		@Override
		public StorageRequestTaskLoadRoots createRootsLoadTask(final int channelCount)
		{
			return new StorageRequestTaskLoadRoots.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount
			);
		}
		
		@Override
		public StorageRequestTaskLoadByTids createLoadTaskByTids(final SwizzleIdSet loadTids, final int channelCount)
		{
			return new StorageRequestTaskLoadByTids.Implementation(
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
			return new StorageRequestTaskExportEntitiesByType.Implementation(
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
			return new StorageRequestTaskExportChannels.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				fileHandler
			);
		}

		@Override
		public StorageRequestTaskCreateStatistics createCreateRawFileStatisticsTask(final int channelCount)
		{
			return new StorageRequestTaskCreateStatistics.Implementation(
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
			return new StorageRequestTaskFileCheck.Implementation(
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
			return new StorageRequestTaskCacheCheck.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				nanoTimeBudgetBound,
				entityEvaluator
			);
		}

		@Override
		public StorageRequestTaskImportData createImportFromFilesTask(
			final int                           channelCount          ,
			final StorageDataFileEvaluator          fileEvaluator         ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final XGettingEnum<File>            importFiles
		)
		{
			return new StorageRequestTaskImportData.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				objectIdRangeEvaluator,
				importFiles
			);
		}

	}

}
