package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageChannelsCreatorLogging
	extends StorageChannelsCreator, StorageLoggingWrapper<StorageChannelsCreator>
{
	static StorageChannelsCreatorLogging New(final StorageChannelsCreator wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
	public static class Default
		extends StorageLoggingWrapper.Abstract<StorageChannelsCreator>
		implements StorageChannelsCreatorLogging
	{
		protected Default(final StorageChannelsCreator wrapped)
		{
			super(wrapped);
		}
				
		
		@Override
		public StorageChannel[] createChannels(
			final int                                  channelCount                 ,
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider,
			final StorageExceptionHandler              exceptionHandler             ,
			final StorageDataFileEvaluator             fileDissolver                ,
			final StorageLiveFileProvider              liveFileProvider             ,
			final StorageEntityCacheEvaluator          entityCacheEvaluator         ,
			final StorageTypeDictionary                typeDictionary               ,
			final StorageTaskBroker                    taskBroker                   ,
			final StorageOperationController           operationController          ,
			final StorageHousekeepingBroker            housekeepingBroker           ,
			final StorageHousekeepingController        housekeepingController       ,
			final StorageTimestampProvider             timestampProvider            ,
			final StorageWriteController               writeController              ,
			final StorageFileWriter.Provider           writerProvider               ,
			final StorageGCZombieOidHandler            zombieOidHandler             ,
			final StorageRootOidSelector.Provider      rootOidSelectorProvider      ,
			final StorageObjectIdMarkQueue.Creator     oidMarkQueueCreator          ,
			final StorageEntityMarkMonitor.Creator     entityMarkMonitorCreator     ,
			final StorageBackupHandler                 backupHandler                ,
			final StorageEventLogger                   eventLogger                  ,
			final boolean                              switchByteOrder              ,
			final long                                 rootTypeId
		)
		{
			
			final StorageDataFileEvaluator    storageDataFileEvaluator    = StorageDataFileEvaluatorLogging.New(fileDissolver);
			final StorageEntityCacheEvaluator storageEntityCacheEvaluator = StorageEntityCacheEvaluatorLogging.New(entityCacheEvaluator);
			final StorageBackupHandler        storageBackupHandler        = backupHandler == null ? backupHandler : StorageBackupHandlerLogging.New(backupHandler);
					
			this.logger().storageSystem_beforeCreateChannels(channelCount);
			
			final StorageChannel[] channels = this.wrapped().createChannels(
				channelCount                 ,
				initialDataFileNumberProvider,
				exceptionHandler             ,
				storageDataFileEvaluator     ,
				liveFileProvider             ,
				storageEntityCacheEvaluator  ,
				typeDictionary               ,
				taskBroker                   ,
				operationController          ,
				housekeepingBroker           ,
				housekeepingController       ,
				timestampProvider            ,
				writeController              ,
				writerProvider               ,
				zombieOidHandler             ,
				rootOidSelectorProvider      ,
				oidMarkQueueCreator          ,
				entityMarkMonitorCreator     ,
				storageBackupHandler         ,
				eventLogger                  ,
				switchByteOrder              ,
				rootTypeId
			);
			
			final StorageChannelLogging[] loggingChannels = new StorageChannelLogging[channels.length];
			for(int i = 0; i < channels.length; i++)
			{
				loggingChannels[i] = StorageChannelLogging.New(channels[i]);
			}
			
			this.logger().storageSystem_afterCreateChannels(channelCount);
			
			return loggingChannels;
		}
		
	}
	
}
