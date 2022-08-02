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

import one.microstream.memory.XMemory;
import one.microstream.persistence.types.ObjectIdsSelector;
import one.microstream.persistence.types.PersistenceLiveStorerRegistry;
import one.microstream.reference.Referencing;
import one.microstream.util.BufferSizeProvider;
import one.microstream.util.BufferSizeProviderIncremental;

public interface StorageChannelsCreator
{
	public StorageChannel[] createChannels(
		int                                        channelCount                 ,
		StorageInitialDataFileNumberProvider       initialDataFileNumberProvider,
		StorageExceptionHandler                    exceptionHandler             ,
		StorageDataFileEvaluator                   fileDissolver                ,
		StorageLiveFileProvider                    liveFileProvider             ,
		StorageEntityCacheEvaluator                entityCacheEvaluator         ,
		StorageTypeDictionary                      typeDictionary               ,
		StorageTaskBroker                          taskBroker                   ,
		StorageOperationController                 operationController          ,
		StorageHousekeepingBroker                  housekeepingBroker           ,
		StorageHousekeepingController              housekeepingController       ,
		StorageTimestampProvider                   timestampProvider            ,
		StorageWriteController                     writeController              ,
		StorageFileWriter.Provider                 writerProvider               ,
		StorageGCZombieOidHandler                  zombieOidHandler             ,
		StorageRootOidSelector.Provider            rootOidSelectorProvider      ,
		StorageObjectIdMarkQueue.Creator           oidMarkQueueCreator          ,
		StorageEntityMarkMonitor.Creator           entityMarkMonitorCreator     ,
		StorageBackupHandler                       backupHandler                ,
		StorageEventLogger                         eventLogger                  ,
		ObjectIdsSelector                          liveObjectIdChecker          ,
		Referencing<PersistenceLiveStorerRegistry> refStorerRegistry            ,
		boolean                                    switchByteOrder              ,
		long                                       rootTypeId
	);



	public static final class Default implements StorageChannelsCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final StorageChannel.Default[] createChannels(
			final int                                        channelCount                 ,
			final StorageInitialDataFileNumberProvider       initialDataFileNumberProvider,
			final StorageExceptionHandler                    exceptionHandler             ,
			final StorageDataFileEvaluator                   dataFileEvaluator            ,
			final StorageLiveFileProvider                    liveFileProvider             ,
			final StorageEntityCacheEvaluator                entityCacheEvaluator         ,
			final StorageTypeDictionary                      typeDictionary               ,
			final StorageTaskBroker                          taskBroker                   ,
			final StorageOperationController                 operationController          ,
			final StorageHousekeepingBroker                  housekeepingBroker           ,
			final StorageHousekeepingController              housekeepingController       ,
			final StorageTimestampProvider                   timestampProvider            ,
			final StorageWriteController                     writeController              ,
			final StorageFileWriter.Provider                 writerProvider               ,
			final StorageGCZombieOidHandler                  zombieOidHandler             ,
			final StorageRootOidSelector.Provider            rootOidSelectorProvider      ,
			final StorageObjectIdMarkQueue.Creator           oidMarkQueueCreator          ,
			final StorageEntityMarkMonitor.Creator           entityMarkMonitorCreator     ,
			final StorageBackupHandler                       backupHandler                ,
			final StorageEventLogger                         eventLogger                  ,
			final ObjectIdsSelector                          liveObjectIdChecker          ,
			final Referencing<PersistenceLiveStorerRegistry> refStorerRegistry            ,
			final boolean                                    switchByteOrder              ,
			final long                                       rootTypeId
		)
		{
			// (14.07.2016 TM)TODO: make configuration dynamic
			final int  markBufferLength         = 10000; // see comment in StorageEntityCache. Must be big!
			final long markingWaitTimeMs        =    10;
			final int  loadingBufferSize        =  XMemory.defaultBufferSize();
			final int  readingDefaultBufferSize =  XMemory.defaultBufferSize();

			final StorageChannel.Default[] channels = new StorageChannel.Default[channelCount];

			final StorageObjectIdMarkQueue[] markQueues = new StorageObjectIdMarkQueue[channels.length];
			for(int i = 0; i < markQueues.length; i++)
			{
				markQueues[i] = oidMarkQueueCreator.createOidMarkQueue(markBufferLength);
			}
			final StorageEntityMarkMonitor markMonitor = entityMarkMonitorCreator.createEntityMarkMonitor(
				markQueues,
				eventLogger,
				refStorerRegistry
			);
			
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
					eventLogger                                      ,
					liveObjectIdChecker                              ,
					markingWaitTimeMs                                ,
					markBufferLength
				);

				// file manager to handle "file" IO (whatever "file" might be, might be a RDBMS binary table as well)
				final StorageFileManager.Default fileManager = new StorageFileManager.Default(
					i                               ,
					initialDataFileNumberProvider   ,
					timestampProvider               ,
					liveFileProvider                ,
					dataFileEvaluator               ,
					entityCache                     ,
					writeController                 ,
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
					housekeepingBroker       ,
					housekeepingController   ,
					entityCache              ,
					switchByteOrder          ,
					loadingBufferSizeProvider,
					fileManager              ,
					eventLogger
				);

			}
			return channels;
		}

	}

}
