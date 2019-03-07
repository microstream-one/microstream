package net.jadoth.storage.types;

import net.jadoth.memory.XMemory;
import net.jadoth.util.BufferSizeProvider;
import net.jadoth.util.BufferSizeProviderIncremental;

public interface StorageChannelsCreator
{
	public StorageChannel[] createChannels(
		int                                  channelCount                 ,
		StorageInitialDataFileNumberProvider initialDataFileNumberProvider,
		StorageExceptionHandler              exceptionHandler             ,
		StorageDataFileEvaluator             fileDissolver                ,
		StorageFileProvider                  storageFileProvider          ,
		StorageEntityCacheEvaluator          entityCacheEvaluator         ,
		StorageTypeDictionary                typeDictionary               , // the connection to the exclusive storage (file or whatever)
		StorageTaskBroker                    taskBroker                   , // the source for new tasks
		StorageChannelController             channelController            , // simple hook to check if processing is still enabled
		StorageHousekeepingController        housekeepingController       ,
		StorageTimestampProvider             timestampProvider            ,
		StorageFileReader.Provider           readerProvider               ,
		StorageFileWriter.Provider           writerProvider               ,
		StorageGCZombieOidHandler            zombieOidHandler             ,
		StorageRootOidSelector.Provider      rootOidSelectorProvider      ,
		StorageOidMarkQueue.Creator          oidMarkQueueCreator          ,
		StorageEntityMarkMonitor.Creator     entityMarkMonitorCreator     ,
		StorageBackupHandler                 backupHandler                ,
		boolean                              switchByteOrder              ,
		long                                 rootTypeId
	);



	public static final class Implementation implements StorageChannelsCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final StorageChannel.Implementation[] createChannels(
			final int                                  channelCount                 ,
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider,
			final StorageExceptionHandler              exceptionHandler             ,
			final StorageDataFileEvaluator             dataFileEvaluator            ,
			final StorageFileProvider                  storageFileProvider          ,
			final StorageEntityCacheEvaluator          entityCacheEvaluator         ,
			final StorageTypeDictionary                typeDictionary               ,
			final StorageTaskBroker                    taskBroker                   ,
			final StorageChannelController             channelController            ,
			final StorageHousekeepingController        housekeepingController       ,
			final StorageTimestampProvider             timestampProvider            ,
			final StorageFileReader.Provider           readerProvider               ,
			final StorageFileWriter.Provider           writerProvider               ,
			final StorageGCZombieOidHandler            zombieOidHandler             ,
			final StorageRootOidSelector.Provider      rootOidSelectorProvider      ,
			final StorageOidMarkQueue.Creator          oidMarkQueueCreator          ,
			final StorageEntityMarkMonitor.Creator     entityMarkMonitorCreator     ,
			final StorageBackupHandler                 backupHandler                ,
			final boolean                              switchByteOrder              ,
			final long                                 rootTypeId
		)
		{
			// (14.07.2016 TM)TODO: make configuration dynamic
			final int  markBufferLength         = 10000;
			final long markingWaitTimeMs        =    10;
			final int  loadingBufferSize        =  XMemory.defaultBufferSize();
			final int  readingDefaultBufferSize =  XMemory.defaultBufferSize();

			final StorageChannel.Implementation[]     channels = new StorageChannel.Implementation[channelCount];

			final StorageOidMarkQueue[]    markQueues = new StorageOidMarkQueue[channels.length];
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
				channels[i] = new StorageChannel.Implementation(
					i                        ,
					exceptionHandler         ,
					taskBroker               ,
					channelController        ,
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