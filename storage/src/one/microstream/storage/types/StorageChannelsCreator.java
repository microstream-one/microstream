package one.microstream.storage.types;

import one.microstream.memory.XMemory;
import one.microstream.util.BufferSizeProvider;
import one.microstream.util.BufferSizeProviderIncremental;

public interface StorageChannelsCreator
{
	public StorageChannel[] createChannels(
		int                                  channelCount                 ,
		StorageInitialDataFileNumberProvider initialDataFileNumberProvider,
		StorageExceptionHandler              exceptionHandler             ,
		StorageDataFileEvaluator             fileDissolver                ,
		StorageFileProvider                  storageFileProvider          ,
		StorageEntityCacheEvaluator          entityCacheEvaluator         ,
		StorageTypeDictionary                typeDictionary               ,
		StorageTaskBroker                    taskBroker                   ,
		StorageOperationController           operationController          ,
		StorageHousekeepingController        housekeepingController       ,
		StorageTimestampProvider             timestampProvider            ,
		StorageFileReader.Provider           readerProvider               ,
		StorageFileWriter.Provider           writerProvider               ,
		StorageGCZombieOidHandler            zombieOidHandler             ,
		StorageRootOidSelector.Provider      rootOidSelectorProvider      ,
		StorageobjectIdMarkQueue.Creator          oidMarkQueueCreator          ,
		StorageEntityMarkMonitor.Creator     entityMarkMonitorCreator     ,
		StorageBackupHandler                 backupHandler                ,
		boolean                              switchByteOrder              ,
		long                                 rootTypeId
	);



	public static final class Default implements StorageChannelsCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final StorageChannel.Default[] createChannels(
			final int                                  channelCount                 ,
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider,
			final StorageExceptionHandler              exceptionHandler             ,
			final StorageDataFileEvaluator             dataFileEvaluator            ,
			final StorageFileProvider                  storageFileProvider          ,
			final StorageEntityCacheEvaluator          entityCacheEvaluator         ,
			final StorageTypeDictionary                typeDictionary               ,
			final StorageTaskBroker                    taskBroker                   ,
			final StorageOperationController           operationController          ,
			final StorageHousekeepingController        housekeepingController       ,
			final StorageTimestampProvider             timestampProvider            ,
			final StorageFileReader.Provider           readerProvider               ,
			final StorageFileWriter.Provider           writerProvider               ,
			final StorageGCZombieOidHandler            zombieOidHandler             ,
			final StorageRootOidSelector.Provider      rootOidSelectorProvider      ,
			final StorageobjectIdMarkQueue.Creator          oidMarkQueueCreator          ,
			final StorageEntityMarkMonitor.Creator     entityMarkMonitorCreator     ,
			final StorageBackupHandler                 backupHandler                ,
			final boolean                              switchByteOrder              ,
			final long                                 rootTypeId
		)
		{
			// (14.07.2016 TM)TODO: make configuration dynamic
			final int  markBufferLength         = 10000; // see comment in StorageEntityCache. Must be big!
			final long markingWaitTimeMs        =    10;
			final int  loadingBufferSize        =  XMemory.defaultBufferSize();
			final int  readingDefaultBufferSize =  XMemory.defaultBufferSize();

			final StorageChannel.Default[]     channels = new StorageChannel.Default[channelCount];

			final StorageobjectIdMarkQueue[]    markQueues = new StorageobjectIdMarkQueue[channels.length];
			for(int i = 0; i < markQueues.length; i++)
			{
				markQueues[i] = oidMarkQueueCreator.createOidMarkQueue(markBufferLength);
			}
			final StorageEntityMarkMonitor markMonitor = entityMarkMonitorCreator.createEntityMarkMonitor(markQueues);
			
			final BufferSizeProviderIncremental loadingBufferSizeProvider = BufferSizeProviderIncremental.New(loadingBufferSize);
			final BufferSizeProvider readingDefaultBufferSizeProvider     = BufferSizeProvider.New(readingDefaultBufferSize);

			for(int i = 0; i < channels.length; i++)
			{
				// entity cache to register entities, cache entity data, perform garbage collection
				final StorageEntityCache.Default entityCache = new StorageEntityCache.Default(
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
				final StorageFileManager.Default fileManager = new StorageFileManager.Default(
					i                               ,
					initialDataFileNumberProvider   ,
					timestampProvider               ,
					storageFileProvider             ,
					dataFileEvaluator               ,
					entityCache                     ,
					readerProvider.provideReader(i) ,
					writerProvider.provideWriter(i) ,
					readingDefaultBufferSizeProvider,
					backupHandler
				);

				// required to resolve the initializer cyclic depedency
				entityCache.initializeStorageManager(fileManager);

				// everything bundled together in a "channel".
				channels[i] = new StorageChannel.Default(
					i                        ,
					exceptionHandler         ,
					taskBroker               ,
					operationController      ,
					housekeepingController   ,
					entityCache              ,
					switchByteOrder             ,
					loadingBufferSizeProvider,
					fileManager
				);

			}
			return channels;
		}

	}

}
