package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Predicate;

import one.microstream.afs.types.AWritableFile;
import one.microstream.persistence.binary.types.Chunk;
import one.microstream.persistence.binary.types.ChunksBuffer;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.storage.types.StorageRawFileStatistics.ChannelStatistics;
import one.microstream.typing.KeyValue;

public interface StorageChannelLogging extends StorageChannel, StorageLoggingWrapper<StorageChannel>
{
	static StorageChannelLogging New(final StorageChannel wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
	static class Default
		extends StorageLoggingWrapper.Abstract<StorageChannel>
		implements StorageChannelLogging
	{
		Default(final StorageChannel wrapped)
		{
			super(wrapped);
		}
		
		@Override
		public boolean isActive()
		{
			return this.wrapped().isActive();
		}

		@Override
		public int channelIndex()
		{
			return this.wrapped().channelIndex();
		}

		@Override
		public void reset()
		{
			this.wrapped().reset();
		}

		@Override
		public StorageTypeDictionary typeDictionary()
		{
			return this.wrapped().typeDictionary();
		}

		@Override
		public ChunksBuffer collectLoadByOids(final ChunksBuffer[] channelChunks, final PersistenceIdSet loadOids)
		{
			return this.wrapped().collectLoadByOids(channelChunks, loadOids);
		}

		@Override
		public ChunksBuffer collectLoadRoots(final ChunksBuffer[] channelChunks)
		{
			return this.wrapped().collectLoadRoots(channelChunks);
		}

		@Override
		public ChunksBuffer collectLoadByTids(final ChunksBuffer[] channelChunks, final PersistenceIdSet loadTids)
		{
			return this.wrapped().collectLoadByTids(channelChunks, loadTids);
		}

		@Override
		public KeyValue<ByteBuffer[], long[]> storeEntities(final long timestamp, final Chunk chunkData)
		{
			return this.wrapped().storeEntities(timestamp, chunkData);
		}

		@Override
		public void rollbackChunkStorage()
		{
			this.wrapped().rollbackChunkStorage();
		}

		@Override
		public void commitChunkStorage()
		{
			this.wrapped().commitChunkStorage();
		}

		@Override
		public void postStoreUpdateEntityCache(final ByteBuffer[] chunks, final long[] chunksStoragePositions)
			throws InterruptedException
		{
			this.wrapped().postStoreUpdateEntityCache(chunks, chunksStoragePositions);
		}

		@Override
		public void run()
		{
			this.logger().storageChannel_beforeRun(this.wrapped());
			
			this.wrapped().run();
			
			this.logger().storageChannel_afterRun(this.wrapped());
		}

		@Override
		public StorageInventory readStorage()
		{
			return this.wrapped().readStorage();
		}

		@Override
		public boolean issuedGarbageCollection(final long nanoTimeBudget)
		{
			return this.wrapped().issuedGarbageCollection(nanoTimeBudget);
		}

		@Override
		public void exportData(final StorageLiveFileProvider fileProvider)
		{
			this.wrapped().exportData(fileProvider);
		}

		@Override
		public one.microstream.storage.types.StorageEntityCache.Default prepareImportData()
		{
			return this.wrapped().prepareImportData();
		}

		@Override
		public void importData(final StorageImportSourceFile importFile)
		{
			this.wrapped().importData(importFile);
		}

		@Override
		public void rollbackImportData(final Throwable cause)
		{
			this.wrapped().rollbackImportData(cause);
		}

		@Override
		public void commitImportData(final long taskTimestamp)
		{
			this.wrapped().commitImportData(taskTimestamp);
		}

		@Override
		public KeyValue<Long, Long> exportTypeEntities(final StorageEntityTypeHandler type, final AWritableFile file)
			throws IOException
		{
			return this.wrapped().exportTypeEntities(type, file);
		}

		@Override
		public KeyValue<Long, Long> exportTypeEntities(final StorageEntityTypeHandler type, final AWritableFile file,
			final Predicate<? super StorageEntity> predicateEntity) throws IOException
		{
			return this.wrapped().exportTypeEntities(type, file, predicateEntity);
		}

		@Override
		public ChannelStatistics createRawFileStatistics()
		{
			return this.wrapped().createRawFileStatistics();
		}

		@Override
		public StorageIdAnalysis initializeStorage(final long taskTimestamp, final long consistentStoreTimestamp,
			final StorageInventory storageInventory)
		{
			return this.wrapped().initializeStorage(taskTimestamp, consistentStoreTimestamp, storageInventory);
		}

		@Override
		public void signalGarbageCollectionSweepCompleted()
		{
			this.wrapped().signalGarbageCollectionSweepCompleted();
		}

		@Override
		public void cleanupStore()
		{
			this.wrapped().cleanupStore();
		}

		@Override
		public boolean issuedFileCleanupCheck(final long nanoTimeBudget)
		{
			return this.wrapped().issuedFileCleanupCheck(nanoTimeBudget);
		}

		@Override
		public boolean issuedEntityCacheCheck(final long nanoTimeBudget, final StorageEntityCacheEvaluator entityEvaluator)
		{
			return this.wrapped().issuedEntityCacheCheck(nanoTimeBudget, entityEvaluator);
		}
	}

}
