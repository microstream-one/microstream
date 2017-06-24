package net.jadoth.storage.types;


import static net.jadoth.Jadoth.checkArrayRange;
import static net.jadoth.Jadoth.closeSilent;
import static net.jadoth.Jadoth.coalesce;
import static net.jadoth.Jadoth.notNull;
import static net.jadoth.Jadoth.to_int;
import static net.jadoth.math.JadothMath.notNegative;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.function.Consumer;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.storage.exceptions.StorageException;
import net.jadoth.storage.exceptions.StorageExceptionIoReading;
import net.jadoth.storage.exceptions.StorageExceptionIoWritingChunk;
import net.jadoth.storage.types.StorageRawFileStatistics.FileStatistics;
import net.jadoth.storage.types.StorageTransactionsFileAnalysis.EntryAggregator;


// note that the name channel refers to the entity hash channel, not an nio channel
public interface StorageFileManager
{
	/* (17.09.2014 TM)TODO: Much more loose coupling
	 * Make all storage stuff much more loosely coupled with more interface methods and
	 * self-passing between components.
	 * For example:
	 * - Initializer, maybe even sub components like initializationValidator, etc.
	 * - Exporter
	 * - Importer
	 * - Garbage Collector (if possible to decouple efficiently)
	 *
	 * Everything that is not performance-critical (i.e. does not get called repeatedly in "hot" code)
	 * should be as flexible as possible to allow finer customization, logging (=aspect) wrapping, etc.
	 * All hot code should be contained in one very small component to be easily exchangeable and/or
	 * specifically loggable.
	 */

	public int channelIndex();

	public long[] storeChunks(long timestamp, ByteBuffer[] dataBuffers, long entityCount)
		throws StorageExceptionIoWritingChunk;

	public void rollbackWrite();

	public void commitWrite();

	public StorageInventory readStorage();

	public StorageIdRangeAnalysis initializeStorage(
		long                        taskTimestamp                   ,
		long                        consistentStoreTimestamp        ,
		StorageInventory            storageInventory                ,
		StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
		StorageTypeDictionary       oldTypes
	);

	public StorageDataFile<?> currentStorageFile();

	public void iterateStorageFiles(Consumer<? super StorageDataFile<?>> procedure);

	public boolean incrementalFileCleanupCheck(long nanoTimeBudgetBound);

	public boolean issuedFileCleanupCheck(long nanoTimeBudgetBound, StorageDataFileDissolvingEvaluator fileDissolver);

	public void exportData(StorageIoHandler fileHandler);

	public StorageRawFileStatistics.ChannelStatistics createRawFileStatistics();

	public void resetFileCleanupCursor();

	public void flush();







	public final class Implementation implements StorageFileManager, StorageReaderCallback
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		// the only reason for this limit is to have an int instead of a long for the item's file position.
		static final int MAX_FILE_LENGTH = Integer.MAX_VALUE;

		// (22.05.2015 TM)TODO: Debug Flag to disable file cleanup for testing
		private static final boolean DEBUG_ENABLE_FILE_CLEANUP = true;



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		private static long chunksTotalLength(final ByteBuffer[] chunks)
		{
			long totalLength = 0;
			for(int i = 0; i < chunks.length; i++)
			{
				totalLength += chunks[i].limit();
			}
			return totalLength;
		}

		private static long[] allChunksStoragePositions(final ByteBuffer[] chunks, final long basePosition)
		{
			final long[] storagePositions = new long[chunks.length];
			long position = basePosition;
			for(int i = 0; i < chunks.length; i++)
			{
				storagePositions[i] = position;
				position += chunks[i].limit();
			}
			return storagePositions;
		}

		static final FileLock openFileChannel(final File file)
		{
//			DEBUGStorage.println("Thread " + Thread.currentThread().getName() + " opening channel for " + file);
			FileChannel channel = null;
			try
			{
				final FileLock fileLock = StorageLockedFile.openFileChannel(file);
				channel = fileLock.channel();
				channel.position(channel.size());
				return fileLock;
			}
			catch(final IOException e)
			{
				closeSilent(channel);
				throw new RuntimeException(e); // (04.05.2013)EXCP: proper exception
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final int                                  channelIndex                 ;
		private final StorageInitialDataFileNumberProvider initialDataFileNumberProvider;
		private final StorageTimestampProvider             timestampProvider            ;
		private final StorageFileProvider                  storageFileProvider          ;
		private final StorageDataFileEvaluator             fileEvaluator                ;
		private final StorageEntityCache.Implementation    entityCache                  ;
		private final StorageFileReader                    reader                       ;
		private final StorageFileWriter                    writer                       ;
		private final StorageWriteListener                 writeListener                ;

		private final ByteBuffer
			entryBufferFileCreation   = ByteBuffer.allocateDirect(StorageTransactionsFileAnalysis.Logic.entryLengthFileCreation())  ,
			entryBufferStore          = ByteBuffer.allocateDirect(StorageTransactionsFileAnalysis.Logic.entryLengthStore())         ,
			entryBufferTransfer       = ByteBuffer.allocateDirect(StorageTransactionsFileAnalysis.Logic.entryLengthTransfer())      ,
			entryBufferFileDeletion   = ByteBuffer.allocateDirect(StorageTransactionsFileAnalysis.Logic.entryLengthFileCreation())  ,
			entryBufferFileTruncation = ByteBuffer.allocateDirect(StorageTransactionsFileAnalysis.Logic.entryLengthFileTruncation())
		;

		private final long
			entryBufferFileCreationAddress   = Memory.directByteBufferAddress(this.entryBufferFileCreation)  ,
			entryBufferStoreAddress          = Memory.directByteBufferAddress(this.entryBufferStore)         ,
			entryBufferTransferAddress       = Memory.directByteBufferAddress(this.entryBufferTransfer)      ,
			entryBufferFileDeletionAddress   = Memory.directByteBufferAddress(this.entryBufferFileDeletion)  ,
			entryBufferFileTruncationAddress = Memory.directByteBufferAddress(this.entryBufferFileTruncation)
		;

		{
			StorageTransactionsFileAnalysis.Logic.initializeEntryFileCreation  (this.entryBufferFileCreationAddress  );
			StorageTransactionsFileAnalysis.Logic.initializeEntryStore         (this.entryBufferStoreAddress         );
			StorageTransactionsFileAnalysis.Logic.initializeEntryTransfer      (this.entryBufferTransferAddress      );
			StorageTransactionsFileAnalysis.Logic.initializeEntryFileDeletion  (this.entryBufferFileDeletionAddress  );
			StorageTransactionsFileAnalysis.Logic.initializeEntryFileTruncation(this.entryBufferFileTruncationAddress);
		}

		private StorageLockedChannelFile fileTransactions;

		// sufficient to load any non-huge (large length array) entity with high efficiency
		// (14.06.2014)TODO: make standard buffer size configurable nevertheless.
		private final ByteBuffer standardByteBuffer = ByteBuffer.allocateDirect(Memory.pageSize());

		private StorageDataFile.Implementation fileCleanupCursor, headFile;

		private long uncommittedDataLength;

		private int  pendingFileDeletes;

		private transient StorageEntityCacheEvaluator entityInitializingCacheEvaluator;

//		private transient boolean hasUnflushedWrites = false;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final int                                  channelIndex                 ,
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider,
			final StorageTimestampProvider             timestampProvider            ,
			final StorageFileProvider                  storageFileProvider          ,
			final StorageDataFileEvaluator             fileDissolver                ,
			final StorageEntityCache.Implementation    entityCache                  ,
			final StorageFileReader                    reader                       ,
			final StorageFileWriter                    writer                       ,
			final StorageWriteListener                 writeListener
		)
		{
			super();
			this.channelIndex                  = notNegative(channelIndex)                 ;
			this.initialDataFileNumberProvider =     notNull(initialDataFileNumberProvider);
			this.timestampProvider             =     notNull(timestampProvider)            ;
			this.fileEvaluator                 =     notNull(fileDissolver)                ;
			this.storageFileProvider           =     notNull(storageFileProvider)          ;
			this.entityCache                   =     notNull(entityCache)                  ;
			this.reader                        =     notNull(reader)                       ;
			this.writer                        =     notNull(writer)                       ;
			this.writeListener                 =     notNull(writeListener)                ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final <L extends Consumer<StorageEntity.Implementation>> L iterateEntities(final L logic)
		{
			// (01.04.2016)TODO: /!\ not tested yet

			final StorageDataFile.Implementation head = this.headFile;
			StorageDataFile.Implementation file = head; // initial reference, but gets handled at the end
			do
			{
				file = file.next;
				final StorageEntity.Implementation tail = file.tail;
				for(StorageEntity.Implementation entity = file.head; (entity = entity.fileNext) != tail;)
				{
					logic.accept(entity);
				}
			}
			while(file != head);

			return logic;
		}

		final boolean isHeadFile(final StorageDataFile.Implementation dataFile)
		{
			return this.headFile == dataFile;
		}

		final void truncateFiles()
		{
			try
			{
				if(this.headFile != null)
				{
					this.writer.flush(this.headFile);
					final StorageDataFile.Implementation currentFile = this.headFile;
					for(StorageDataFile.Implementation file = currentFile.next; file != currentFile; file = file.next)
					{
						file.terminate(this.writer);
					}
					currentFile.terminate(this.writer);
					this.headFile = null;
				}

				if(this.fileTransactions != null)
				{
					this.writer.flush(this.fileTransactions);
				}
				else
				{
					this.setTransactionsFile(this.createTransactionsFile());
				}

				this.writer.truncate(this.fileTransactions, 0);

				// note: flush is done above on a per-case basis
				this.writeListener.registerTruncate(this.channelIndex());
				this.addFirstFile();
				this.resetFileCleanupCursor();
			}
			catch(final IOException e)
			{
				throw new RuntimeException(e); // (26.11.2014 TM)EXCP: proper exception
			}
		}

		private void addFirstFile()
		{
			try
			{
				this.createNewStorageFile(
					this.initialDataFileNumberProvider.provideInitialDataFileNumber(this.channelIndex())
				);
			}
			catch(final Exception e)
			{
				this.clearRegisteredFiles();
				throw e;
			}
		}

		final void clearRegisteredFiles()
		{
			// (07.07.2016 TM)FIXME: why close silent? What about OS/IO/network problems?
			/* (07.07.2016 TM)TODO: StorageFileCloser
			 * to abstract the delicate task of closing files.
			 * Or better enhance StorageFileProvider to a StorageFileHandler
			 * that handles both creation and closing.
			 */
			StorageFile.closeSilent(this.fileTransactions);

			if(this.headFile == null)
			{
				return; // already cleared or no files in the first place
			}

			final StorageDataFile.Implementation headFile = this.headFile;

			StorageDataFile.Implementation file = headFile;
			do
			{
				StorageFile.closeSilent(file);
			}
			while((file = file.next) != headFile);

			this.fileCleanupCursor = this.headFile = null;
		}

		private ByteBuffer buffer(final int length)
		{
			if(length > this.standardByteBuffer.capacity())
			{
				return ByteBuffer.allocateDirect(length);
			}
			this.standardByteBuffer.clear().limit(length);

			return this.standardByteBuffer;
		}

		private void clearBuffer(final ByteBuffer buffer)
		{
			buffer.clear();
			if(buffer != this.standardByteBuffer)
			{
				Memory.deallocateDirectByteBuffer(buffer); // hope this works, not tested yet
			}
		}

		private void writeChunks(final long timestamp, final ByteBuffer[] dataBuffers, final long entityCount)
		{
			this.uncommittedDataLength = chunksTotalLength(dataBuffers);
//			DEBUGStorage.println(this.channelIndex + " writing " + entityCount + " entities (" + this.uncommittedDataLength + " bytes) to " + this.headFile.number());
			if(dataBuffers.length == 0)
			{
				return; // nothing to write (empty chunk, only header for consistency)
			}

			final long currentTotalLength = this.headFile.totalLength();

			this.writer.write(this.headFile, dataBuffers);
			this.writeTransactionsEntryStore(currentTotalLength + this.uncommittedDataLength, timestamp);
			this.flush();
//			DEBUGStorage.println(this.channelIndex + " wrote " + this.uncommittedDataLength + " bytes");

			this.resetFileCleanupCursor();
//			DEBUGStorage.println("Channel " + this.channelIndex + " wrote data for " + timestamp);
		}

		final void transferOneChainToHeadFile(final StorageDataFile.Implementation sourceFile)
		{
			final StorageDataFile.Implementation headFile = this.headFile           ;
			final StorageEntity.Implementation   first    = sourceFile.head.fileNext;
			      StorageEntity.Implementation   last     = null                    ;
			      StorageEntity.Implementation   current  = first                   ;

			final int  copyStart                = first.storagePosition                     ;
			final int  targetFileOldTotalLength = to_int(headFile.totalLength())          ;
			final int  maximumFileSize          = this.fileEvaluator.maximumFileSize()      ;
			final int  freeSpace                = maximumFileSize - targetFileOldTotalLength;
			      int  copyLength               = 0                                         ;

			/*
			 * Collecting the transfer chain has 2 abort conditions:
			 * 1.) a gap between entities is detected (next entity's position is not the expected position)
			 * 2.) the current entity's length would not fit in the target file's remaining space
			 * Note:
			 * As the method is guaranteed to be called on a non-empty file,
			 * point 1) automatically recognizes the tail entry or file end.
			 */
			do
			{
				// check for enough free space
				if(copyLength + current.length > freeSpace)
				{
					// if there is already something to transfer, break and copy it
					if(copyLength != 0)
					{
						break;
					}

					// nothing to transfer yet, so create next storage file and try again in next round.
					if(targetFileOldTotalLength != 0)
					{
						this.createNextStorageFile();
						return;
					}

					// nothing to transfer yet and empty target file, transfer singleton oversized entity anyway
				}

	//			System.out.println(last.storagePosition + "\t" + (last.storagePosition - transferPositionOffset) + "\t" + last.length);
	//			DEBUGStorage.println("transfer assigning\t" + current.objectId + "\t" + fileNewTotalLength + "\t" + current.length + "\t" + targetFile.number());
				// set new file. Enqueing in the file's item chain is done for the whole sub chain
				current.typeInFile            = headFile.typeInFile(current.typeInFile.type);
				// update position to the one in the target file (old length plus current copy length)
				current.storagePosition = targetFileOldTotalLength + copyLength;

				// advance to next entity and add current entity's length to the total copy length
				copyLength += current.length;
				current = (last = current).fileNext;
			}
			while(current.storagePosition == copyStart + copyLength);

	//		DEBUGStorage.println("total transfer length = " + copyLength);

			// can only reach here if there is at least one entity to transfer

			// udpate source file to keep consistency as it might not be cleared completely
	//		DEBUGStorage.println("Updating source file " + sourceFile + " for content length " + transferLength);
			sourceFile.removeHeadBoundChain(current, copyLength);
	//		DEBUGStorage.println("Updated source file: " + sourceFile);

			// update target files's content length. Must be done here as next transfer depends on updated length
	//		DEBUGStorage.println("Updating target file " + targetFile + " for content length " + transferLength);
			headFile.addChainToTail(first, last);
	//		DEBUGStorage.println("Updated target file: " + targetFile);

	//		DEBUGStorage.println(this.channelIndex + " transfering bytes, new length " + headFile.totalLength());

			this.appendBytesToHeadFile(sourceFile, copyStart, copyLength);

			// derive fullness state of target file. Can happen on exact fit or oversized single entity.
			if(copyLength >= freeSpace)
			{
				this.createNextStorageFile();
			}
		}

		private void appendBytesToHeadFile(
			final StorageDataFile.Implementation sourceFile,
			final int                            copyStart ,
			final int                            copyLength
		)
		{
//			DEBUGStorage.println(
//				this.channelIndex + " transerring " + copyLength + " bytes from position " + copyStart
//				+ " from " + sourceFile + " to " + targetFile
//			);

			// do the actual file-level copying in one go at the end and validate the byte count to be sure //
			final long writeCount = this.writer.copy(sourceFile, this.headFile, copyStart, copyLength);

			// (12.02.2015 TM)TODO: what about flushing on length validation (or any other) exception?
			this.validateTransferLength(copyLength, writeCount);

			/*
			 * Note:
			 * It can happen that a transfer is written completely but the process terminates right before the
			 * transactions entry for it is written.
			 * This causes the next initialization to truncate a perfectly fine and complete transfer chunk
			 * because it cannot find the transactions entry validating that chunk.
			 * However this is not a problem but happens by design. Data can never be lost by this behavior:
			 * If the process terminates before the entry write can be executed, there can also be no subsequent
			 * file cleanup that deletes the old data file. Hence the transferred data still exists within it
			 * and gets registered as live data on the next initialization (and probably gets transferred then
			 * once again).
			 */

			this.writeTransactionsEntryTransfer(
				this.headFile.totalLength(),
				this.timestampProvider.currentNanoTimestamp(),
				sourceFile.number(),
				copyStart
			);

			/* No flushing is required here for the following reason:
			 * Only transfers from non-head-files (to the head-file) can happen here.
			 * non-head-files (retired files) are guaranteed to having been flushed, completely written
			 * and to never change again.
			 * The head-file, on the other hand, is not retired yet and can therefore never be read from.
			 * Any other action cannot be reported to the listener until the transferring is completed
			 * and at the end of the transfer (timeout or file migration complete), a flush() is performed.
			 */

			// report write to the listener (e.g. for updating the backup accordingly later on)
			this.writeListener.registerTransfer(sourceFile, copyStart, copyLength);
		}

		final void validateTransferLength(final long desiredLength, final long actualLength)
		{
			if(desiredLength == actualLength)
			{
				return; // validation successful
			}

			// (28.06.2013 TM)EXCP: proper exception
			throw new RuntimeException(
				this + " Incomplete file channel transfer: " + actualLength + " != " + desiredLength
			);
		}

		private void createNewStorageFile(final long fileNumber)
		{
//			DEBUGStorage.println(this.channelIndex + " creating new head file " + fileNumber);

			final StorageInventoryFile file = this.storageFileProvider.provideStorageFile(this.channelIndex(), fileNumber);

			/* File#length is incredibly slow compared to FileChannel#size (although irrelevant here),
			 * but still the file length has to be checked before the channel is created, etc.
			 */
			if(file.length() != 0)
			{
				// (29.05.2014)EXCP: proper exception
				throw new RuntimeException("New storage file is not empty: " + file);
			}

			// create and register StorageFile instance with an attached channel
			this.registerHeadFile(file);
			this.writeTransactionsEntryFileCreation(0, this.timestampProvider.currentNanoTimestamp(), fileNumber);
			this.writer.flush(this.fileTransactions); // flush changes before reporting to the listener
			this.writeListener.registerCreate(this.headFile);
		}

		private void registerHeadFile(final StorageInventoryFile file)
		{
			this.registerStorageHeadFile(StorageDataFile.Implementation.New(this, file));
		}

		private void registerStorageHeadFile(final StorageDataFile.Implementation storageFile)
		{
			if(this.headFile == null)
			{
				// initialization special case
				storageFile.next = storageFile.prev = storageFile;
			}
			else
			{
				// join in chain
				storageFile.next = this.headFile.next;
				storageFile.prev = this.headFile;
				this.headFile.next.prev = storageFile;
				this.headFile.next = storageFile;
			}

			// in the end the file is set as current head in any case
			this.headFile = storageFile;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final void flush()
		{
			this.writer.flush(this.headFile);
			this.writer.flush(this.fileTransactions);
		}

		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}

		@Override
		public final StorageDataFile.Implementation currentStorageFile()
		{
			return this.headFile;
		}

		@Override
		public void iterateStorageFiles(final Consumer<? super StorageDataFile<?>> procedure)
		{
			// keep current als end marker, but start with first file, use current als last and then quit the loop
			final StorageDataFile.Implementation current = this.headFile;
			StorageDataFile.Implementation file = current;
			do
			{
				procedure.accept(file = file.next);
			}
			while(file != current);
		}

		private void checkForNewFile()
		{
			if(this.headFile.needsRetirement(this.fileEvaluator))
			{
				this.createNextStorageFile();
			}
		}

		final void createNextStorageFile()
		{
			this.flush(); // final flush to current head file before replacing it with a new one
			this.createNewStorageFile(this.headFile.number() + 1);
		}

		@Override
		public final long[] storeChunks(final long timestamp, final ByteBuffer[] dataBuffers, final long entityCount)
			throws StorageExceptionIoWritingChunk
		{
			this.checkForNewFile();
			final long[] storagePositions = allChunksStoragePositions(
				dataBuffers,
				this.headFile.totalLength()
			);
			this.writeChunks(timestamp, dataBuffers, entityCount);
			return storagePositions;
		}

		@Override
		public final void rollbackWrite()
		{
			this.writer.truncate(this.headFile, this.headFile.totalLength());
		}

		@Override
		public final void commitWrite()
		{
			// commit data length
			this.headFile.increaseContentLength(this.uncommittedDataLength);

			// note: flush already happened before in the actual writeChunks() method

			// register after updating the content length but before resetting the length change
			this.writeListener.registerStore(
				this.headFile,
				this.headFile.totalLength() - this.uncommittedDataLength,
				this.uncommittedDataLength
			);

			// reset the length change helper field
			this.uncommittedDataLength = 0;
		}

		final void loadData(
			final StorageDataFile.Implementation dataFile   ,
			final StorageEntity.Implementation   entity     ,
			final long                           length     ,
			final long                           cacheChange
		)
		{
//			DEBUGStorage.println(this.channelIndex + " loading entity " + entity);
			final ByteBuffer dataBuffer = this.buffer(checkArrayRange(length));
			try
			{
				this.reader.readStorage(dataFile, entity.storagePosition, dataBuffer, this);
				this.putLiveEntityData(entity, Memory.directByteBufferAddress(dataBuffer), length, cacheChange);
			}
			catch(final Exception e)
			{
				// (10.12.2014 TM)EXCP: report relevant values
				throw new StorageExceptionIoReading(e);
			}
			finally
			{
				this.clearBuffer(dataBuffer);
			}
		}

		private void putLiveEntityData(
			final StorageEntity.Implementation entity     ,
			final long                         address    ,
			final long                         length     ,
			final long                         cacheChange
		)
		{
			entity.putCacheData(address, length);
			this.entityCache.modifyUsedCacheSize(cacheChange);
		}

		@Override
		public long incrementalRead(
			final StorageLockedFile fileChannel  ,
			final long              filePosition ,
			final ByteBuffer        buffer       ,
			final long              lastReadCount
		)
			throws IOException
		{
			if(lastReadCount < 0)
			{
				// (30.06.2013)EXCP: proper exception
				throw new RuntimeException(this.channelIndex() + " failed to read data at " + filePosition);
			}
			throw new net.jadoth.meta.NotImplementedYetError(
				"filePosition = " + filePosition + ", lastReadCount = " + lastReadCount
				+ ", buffer = " + buffer.limit() + " / " + buffer.position() + " / " + buffer.remaining()
			);
			/* (28.06.2013 TM)TODO: handle incomplete reads
			 * Naive while(remaining) loop won't do either as this might
			 * freeze the thread if there are no bytes for some reason
			 */
		}

		@Override
		public final StorageInventory readStorage()
		{
			if(this.headFile != null)
			{
				// (21.04.2013)EXCP: proper exception
				throw new RuntimeException(this.channelIndex() + " already initialized");
			}

			final StorageTransactionsFileAnalysis transactionsFile = this.readTransactionsFile();
			final BulkList<StorageInventoryFile>  storageFiles     = this.storageFileProvider.collectStorageFiles(
				new BulkList<>(),
				this.channelIndex()
			)
			.sort(StorageInventoryFile::orderByNumber);

			return new StorageInventory.Implementation(storageFiles, transactionsFile);
		}

		final StorageTransactionsFileAnalysis readTransactionsFile()
		{
			final StorageLockedChannelFile file = this.createTransactionsFile();

			if(!file.file().exists())
			{
				/* (11.09.2014 TM)TODO: missing transactions file handler function
				 * default implementation just returns null.
				 * Also see TO-DO for deriver function.
				 */
				return null;
			}

			FileChannel channel = null;
			try
			{
				channel = file.fileChannel();

				final EntryAggregator aggregator = StorageTransactionsFileAnalysis.Logic.processInputFile(
					channel,
					new EntryAggregator(this.channelIndex())
				);
				return aggregator.yield(file);
			}
			catch(final IOException e)
			{
				StorageFile.closeSilent(file);
				throw new RuntimeException(e); // (29.08.2014)EXCP: proper exception
			}
		}

		private void setEntityInitializingCacheEvaluator(
			final StorageEntityCacheEvaluator entityInitializingCacheEvaluator
		)
		{
			this.entityInitializingCacheEvaluator = entityInitializingCacheEvaluator != null
				? entityInitializingCacheEvaluator
				: this.entityCache.entityCacheEvaluator
			;
		}

		private void clearEntityInitializingCacheEvaluator()
		{
			this.entityInitializingCacheEvaluator = null;
		}

		private long validateStorageDataFilesLength(final StorageInventory storageInventory)
		{
			final StorageTransactionsFileAnalysis tFileAnalysis = storageInventory.transactionsFileAnalysis();
			long unregisteredEmptyLastFileNumber = -1; // -1 for "none"

			if(tFileAnalysis == null || tFileAnalysis.transactionsFileEntries().isEmpty())
			{
				// no transaction file (content) present. Abort and derive later.
				// (06.09.2014)TODO: configurable MissingTransactionsFileHandler callback
				return unregisteredEmptyLastFileNumber;
			}

			final XGettingSequence<StorageInventoryFile>    dataFiles = storageInventory.dataFiles();
			final EqHashTable<Long, StorageTransactionFile> fileEntries = EqHashTable.New(tFileAnalysis.transactionsFileEntries());
			final StorageInventoryFile                      lastFile    = dataFiles.peek();

			for(final StorageInventoryFile file : dataFiles)
			{
				final long actualFileLength = file.file().length();

				// retrieve and remove (= mark as already handled) the corresponding file entry
				final StorageTransactionFile entryFile = fileEntries.removeFor(file.number());
				if(entryFile == null)
				{
					// special case: empty file was created but not registered, can be safely ignored
					if(file == lastFile && actualFileLength == 0)
					{
						unregisteredEmptyLastFileNumber = file.number();
						continue;
					}

					// if the transactions file is present, it must be consistent (i.e. account for all files)
					throw new RuntimeException(
						this.channelIndex() + " could not find transactions entry for file " + file.number()
					); // (06.09.2014)EXCP: proper exception
				}

				/* (18.06.2015 TM)TODO: handle files registered as deleted but not deleted yet
				 * files that were registered as deleted but still linger around can/must be
				 * safely deleted and removed from the dataFiles collection.
				 */
//				if(entryFile.isDeleted())
//				{
//
//				}

				// compare file lengths (head file special case: can be valid if longer, i.e. uncommitted write)
				if(entryFile.length() == actualFileLength || file == lastFile && entryFile.length() < actualFileLength)
				{
					// actual file length is valid
					continue;
				}

				// inconsistent file length compared to transactions file, throw exception
				// (11.09.2014 TM)EXCP: proper exception
				throw new RuntimeException(
					this.channelIndex() + " Length " + actualFileLength + " of file "
					+ file.number() + " is inconsinstent with the transactions entry's length of " + entryFile.length()
				);
			}

			// check that all remaining file entries are deleted files. No non-deleted file may be missing!
			for(final StorageTransactionFile remainingFileEntry : fileEntries.values())
			{
				if(remainingFileEntry.isDeleted())
				{
					continue;
				}

				// (06.09.2014)EXCP: proper exception
				throw new RuntimeException(
					"Non-deleted data file not found: channel " + this.channelIndex()
					+ ", file " + remainingFileEntry.fileNumber()
				);
			}

			/* at this point it is guaranteed that all transactions entries and existing files
			 * are viable in terms of file lengths. Viable means exact same length for any non-last file and
			 * actual file length equal to or greater than logged length for last file (i.e. there can be one
			 * uncommitted write at the end which can be safely truncated later on).
			 */

			// return required information about uber special case
			return unregisteredEmptyLastFileNumber;
		}




		@Override
		public StorageIdRangeAnalysis initializeStorage(
			final long                        taskTimestamp                   ,
			final long                        consistentStoreTimestamp        ,
			final StorageInventory            storageInventory                ,
			final StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
			final StorageTypeDictionary       oldTypes
		)
		{
//			DEBUGStorage.println(this.channelIndex + " init for consistent timestamp " + consistentStoreTimestamp);

			// validate file lengths, even in case of no files, to validate transactions entries to that state
			final long unregisteredEmptyLastFileNumber = this.validateStorageDataFilesLength(storageInventory);

			// set temporary entity cache evaluator for use in entity initialization
			this.setEntityInitializingCacheEvaluator(entityInitializingCacheEvaluator);

			boolean isEmpty = true;
			try
			{
				isEmpty = storageInventory.dataFiles().isEmpty();

				final StorageIdRangeAnalysis idRangeAnalysis;
				if(isEmpty)
				{
					// initialize if there are no files at all (create first file, ensure transactions file)
					this.initializeForNoFiles(taskTimestamp, storageInventory);
					idRangeAnalysis = StorageIdRangeAnalysis.New(0L, 0L, 0L);
				}
				else
				{
					// register a pending store update to keep state (e.g. GC) in a consistent state.
					this.entityCache.registerPendingStoreUpdate();

					// handle files (read, parse, register items) and ensure transactions file
					idRangeAnalysis = this.initializesForExistingFiles(
						taskTimestamp                  ,
						storageInventory               ,
						consistentStoreTimestamp       ,
						oldTypes                       ,
						unregisteredEmptyLastFileNumber
					);
				}

				this.resetFileCleanupCursor();
//				DEBUGStorage.println(this.channelIndex + " initialization complete, maxOid = " + maxOid);
				return idRangeAnalysis;
			}
			catch(final RuntimeException e)
			{
				// on any exception, reset (clear) the internal state
				this.clearState();
				throw e;
			}
			finally
			{
				// release temporary reference in any case
				this.clearEntityInitializingCacheEvaluator();

				if(!isEmpty)
				{
					// clear the previously registered pending store update
					this.entityCache.clearPendingStoreUpdate();
				}
			}
		}

		private StorageIdRangeAnalysis initializesForExistingFiles(
			final long                  taskTimestamp                  ,
			final StorageInventory      storageInventory               ,
			final long                  consistentStoreTimestamp       ,
			final StorageTypeDictionary oldTypes                       ,
			final long                  unregisteredEmptyLastFileNumber
		)
		{
			// local variables for readabiligy, debugging and (paranoid) consistency guarantee
			final XGettingSequence<StorageInventoryFile> files    = storageInventory.dataFiles();
			final StorageInventoryFile                   lastFile = files.peek();

			// validate and determine length of last file before any file is processed to recognize errors early
			final long lastFileLength = unregisteredEmptyLastFileNumber >= 0
				? 0
				: this.determineLastFileLength(consistentStoreTimestamp, storageInventory)
			;
//			DEBUGStorage.println(this.channelIndex + " determined last file length as " + lastFileLength);

			// register items (gaps and entities, with latest version of each entity replacing all previous)
			this.registerItems(files, lastFile, lastFileLength);

			// validate entities (only the latest versions)
			final StorageIdRangeAnalysis maxOid = this.validateEntities(oldTypes);

			// ensure transactions file before handling last file as truncation needs to write in it
			this.ensureTransactionsFile(taskTimestamp, storageInventory, unregisteredEmptyLastFileNumber);

			// special-case handle the last file
			this.handleLastFile(lastFile, lastFileLength);

			// check if last file is oversized and should be retired right away (to avoid necessary dummy-store)
			this.checkForNewFile();

			return maxOid;
		}

		private long determineLastFileLength(
			final long             consistentStoreTimestamp,
			final StorageInventory storageInventory
		)
		{
			final StorageTransactionsFileAnalysis tFileAnalysis = storageInventory.transactionsFileAnalysis();

			if(tFileAnalysis == null || tFileAnalysis.isEmpty())
			{
				/*
				 * if no transactions file was present, it must be assumed that the last file is consistent
				 * (e.g. user manually deleted the transactions file in a consistent database)
				 */
				return storageInventory.dataFiles().last().file().length();
			}
			else if(tFileAnalysis.headFileLatestTimestamp() == consistentStoreTimestamp)
			{
				return tFileAnalysis.headFileLatestLength();
			}
			else if(tFileAnalysis.headFileLastConsistentStoreTimestamp() == consistentStoreTimestamp)
			{
				// note: covers a successful transfer (which is channel-local) that happened after the store as well!
				return tFileAnalysis.headFileLastConsistentStoreLength();
			}
			else
			{
				// should never happen because of all the validations before
				// (10.06.2014)EXCP: proper exception
				throw new RuntimeException(
					"Inconsistent last timestamps in last file of channel " + this.channelIndex()
				);

			}
		}

		private void registerItems(
			final XGettingSequence<StorageInventoryFile> files         ,
			final StorageInventoryFile                   lastFile      ,
			final long                                   lastFileLength
		)
		{
			// (10.06.2014)TODO: configurable initializer buffer size
			// (16.06.2014 TM)NOTE: funny enough, tests showed no significant difference above page sized buffer
			final StorageDataFileItemIterator iterator = StorageDataFileItemIterator.New(
				StorageDataFileItemIterator.BufferProvider.New(),
				this::initialPutEntity
			);

			for(final StorageInventoryFile file : files)
			{
				try
				{
					this.registerHeadFile(file);
					if(file != lastFile)
					{
						iterator.iterateStoredItems(file.fileChannel(), 0, file.file().length());
					}
					else
					{
						// iterate BEFORE truncation to avoid modifying the file in case of errors in items
						iterator.iterateStoredItems(lastFile.fileChannel(), 0, lastFileLength);
					}
				}
				catch(final Exception e)
				{
					throw new StorageException("Exception while initializing storage file " + file.file(), e);
				}
			}
		}

		private StorageIdRangeAnalysis validateEntities(final StorageTypeDictionary oldTypes)
		{
			return this.entityCache.validateEntities(oldTypes);
		}

		private void initializeForNoFiles(final long taskTimestamp, final StorageInventory storageInventory)
		{
			// ensure transcations file BEFORE adding the first file as it writes a transactions entry
			this.ensureTransactionsFile(taskTimestamp, storageInventory, -1);
			this.addFirstFile();
		}

		private void ensureTransactionsFile(
			final long             taskTimestamp                  ,
			final StorageInventory storageInventory               ,
			final long             unregisteredEmptyLastFileNumber
		)
		{
			final StorageTransactionsFileAnalysis trFileAn = storageInventory.transactionsFileAnalysis();
			final StorageLockedChannelFile transactionsFile;

			if(trFileAn == null || trFileAn.isEmpty())
			{
				// get or create new
				transactionsFile = trFileAn == null
					? this.createTransactionsFile()
					: trFileAn.transactionsFile()
				;

				// validate length of both cases anyway. 0-length is essential before deriving content
				if(transactionsFile.length() != 0)
				{
					throw new StorageException("Invalid transactions file in channel " + this.channelIndex);
				}

				// guaranteed empty transaction file gets its content derived from the inventory
				this.deriveTransactionsFile(taskTimestamp, storageInventory, transactionsFile);
			}
			else
			{
				// already existing non-empty transactions file: just use it.
				transactionsFile = trFileAn.transactionsFile();
			}

			this.setTransactionsFile(transactionsFile);

			if(unregisteredEmptyLastFileNumber >= 0)
			{
				this.writeTransactionsEntryFileCreation(0, taskTimestamp, unregisteredEmptyLastFileNumber);
			}

		}

		private StorageLockedChannelFile createTransactionsFile()
		{
			return this.storageFileProvider.provideTransactionsFile(this.channelIndex());
		}

		private void deriveTransactionsFile(
			final long                     taskTimestamp   ,
			final StorageInventory         storageInventory,
			final StorageLockedChannelFile tfile
		)
		{
			// (07.09.2014)TODO: derive transaction file function to modify behavior (e.h. throw exception instead)
			final XGettingSequence<StorageInventoryFile> files   = storageInventory.dataFiles()       ;
			final ByteBuffer                             buffer  = this.entryBufferFileCreation       ;
			final long                                   address = this.entryBufferFileCreationAddress;
			final StorageFileWriter                      writer  = this.writer                        ;

			long timestamp = taskTimestamp - storageInventory.dataFiles().size() - 1;

			try
			{
				for(final StorageInventoryFile file : files)
				{
					buffer.clear();
					StorageTransactionsFileAnalysis.Logic.setEntryFileCreation(
						address             ,
						file.file().length(),
						++timestamp         ,
						file.number()
					);
					writer.write(tfile, buffer);
				}

				// flush out all changes in the end
				writer.flush(tfile);
			}
			catch(final Exception e)
			{
				StorageFile.closeSilent(tfile);
				throw e;
			}
		}

		private void writeTransactionsEntryFileCreation(
			final long length   ,
			final long timestamp,
			final long number
		)
		{
			this.entryBufferFileCreation.clear();
			StorageTransactionsFileAnalysis.Logic.setEntryFileCreation(
				this.entryBufferFileCreationAddress,
				length                             ,
				timestamp                          ,
				number
			);
			this.writer.write(this.fileTransactions, this.entryBufferFileCreation);
		}

		private void writeTransactionsEntryStore(
			final long length   ,
			final long timestamp
		)
		{
			this.entryBufferStore.clear();
			StorageTransactionsFileAnalysis.Logic.setEntryStore(
				this.entryBufferStoreAddress,
				length                      ,
				timestamp
			);
			this.writer.write(this.fileTransactions, this.entryBufferStore);
//			this.writer.flush(this.fileTransactions); // (12.02.2015 TM)NOTE: replaced by outside flush
		}

		private void writeTransactionsEntryTransfer(
			final long length          ,
			final long timestamp       ,
			final long sourcefileNumber,
			final long sourcefileOffset
		)
		{
//			DEBUGStorage.println(this.channelIndex + " writing transfer entry "
//				+sourcefileNumber + " -> " + this.headFile.number() + "\t"
//				+length + "\t"
//				+timestamp + "\t"
//			);
			this.entryBufferTransfer.clear();
			StorageTransactionsFileAnalysis.Logic.setEntryTransfer(
				this.entryBufferTransferAddress,
				length                         ,
				timestamp                      ,
				sourcefileNumber               ,
				sourcefileOffset
			);
			this.writer.write(this.fileTransactions, this.entryBufferTransfer);
//			DEBUGStorage.println(this.channelIndex + " written transfer entry");
		}

		private void writeTransactionsEntryFileDeletion(
			final long length   ,
			final long timestamp,
			final long number
		)
		{
			this.entryBufferFileDeletion.clear();
			StorageTransactionsFileAnalysis.Logic.setEntryFileDeletion(
				this.entryBufferFileDeletionAddress,
				length                             ,
				timestamp                          ,
				number
			);
			this.writer.write(this.fileTransactions, this.entryBufferFileDeletion);
		}

		private void writeTransactionsEntryFileTruncation(
			final long newLength,
			final long timestamp,
			final long number   ,
			final long oldLength
		)
		{
			this.entryBufferFileTruncation.clear();
			StorageTransactionsFileAnalysis.Logic.setEntryFileTruncation(
				this.entryBufferFileTruncationAddress,
				newLength                            ,
				timestamp                            ,
				number                               ,
				oldLength
			);
			this.writer.write(this.fileTransactions, this.entryBufferFileTruncation);
		}

		private void setTransactionsFile(final StorageLockedChannelFile transactionsFile)
		{
			this.fileTransactions = transactionsFile;
		}

		final void clearState()
		{
			this.clearRegisteredFiles();
			this.entityCache.clearState();
		}

		final void handleLastFile(
			final StorageInventoryFile lastFile      ,
			final long                 lastFileLength
		)
		{
			if(lastFileLength != lastFile.file().length())
			{
//				DEBUGStorage.println(this.channelIndex + " truncating last file to " + lastFileLength + " " + lastFile);
				// reaching here means in any case that the file has to be truncated and its header must be updated

				// write truncation entry (BEFORE the actual truncate)
				this.writeTransactionsEntryFileTruncation(
					lastFileLength                               ,
					this.timestampProvider.currentNanoTimestamp(),
					lastFile.number()                            ,
					lastFile.file().length()
				);
				this.writer.flush(this.fileTransactions);

				// (20.06.2014 TM)TODO: truncator function to give a chance to evaluate / rescue the doomed data
				this.writer.truncate(lastFile, lastFileLength);
			}
		}

		final boolean initialPutEntity(final long address, final long availableItemLength)
		{
			final long length = BinaryPersistence.getEntityLength(address);
			if(length < 0)
			{
				this.headFile.registerGap(checkArrayRange(-length));
				return true; // gap appended successfully
			}
			if(availableItemLength < BinaryPersistence.entityHeaderLength())
			{
				return false; // incomplete entity header, cannot register
			}

//			System.out.println(
//				this.channelIndex + "\t" + this.headFile.number() + "\t" + typeId + "\t" + objcId
//				+ "\t" + length + "\t" + this.headFile.dataLength() + "\t" + this.headFile.totalLength()
//			);

			/* validation is postponed to after registering all entities for the following reasons:
			 * 1.) only the latest version of each entity is validated, not all previous ones (huge performance win)
			 * 2.) makes initialization refactoring-friendly: old versions are not checked against changed type
			 */

			final StorageEntity.Implementation entity = this.entityCache.putEntity(address);
			/*
			 * note:
			 * intentionally no markEntityForChangedData here, as entities are initially not
			 * guaranteed to be reachable. They might be unreachable (= junk) entities that
			 * only exist because the storage file has not yet been cleaned up and might reference already
			 * deleted entities. This would cause false positive zombie OID encounters.
			 */
			entity.updateStorageInformation(
				checkArrayRange(length),
				this.headFile,
				to_int(this.headFile.totalLength())
			);
			this.headFile.increaseContentLength(length);

			// cache fully available (=small) entities in advance to avoid numerous inefficient disc reads later on.
			if(availableItemLength >= length && !this.entityInitializingCacheEvaluator.clearEntityCache(
				this.entityCache.cacheSize(),
				entity.lastTouched,
				entity
			))
			{
//				DEBUGStorage.println(this.channelIndex + " initial-caching data for " + entity);
				this.putLiveEntityData(entity, address, length, length);
			}

//			System.out.println("\t\t\t\t" + this.headFile.dataLength() + "\t" + this.headFile.totalLength());
//			System.out.println("");
			return true; // entity registered successfully
		}

		@Override
		public void exportData(final StorageIoHandler fileHandler)
		{
			// copy transactions file first so that an incomplete data file transfer can be recognized later.
			final StorageLockedChannelFile backupTrsFile = fileHandler.copyTransactions(this.fileTransactions);
			backupTrsFile.close();

			this.iterateStorageFiles(file ->
			{
				final StorageInventoryFile backupDatFile = fileHandler.copyData(file);
				backupDatFile.close();
			});
		}

		@Override
		public final StorageRawFileStatistics.ChannelStatistics createRawFileStatistics()
		{
			StorageDataFile.Implementation file;
			final StorageDataFile.Implementation currentFile = file = this.headFile;

			long liveDataLength  = 0;
			long totalDataLength = 0;
			final BulkList<FileStatistics.Implementation> fileStatistics = BulkList.New();

			do
			{
				file = file.next;
				liveDataLength  += file.dataLength();
				totalDataLength += file.totalLength();
				fileStatistics.add(
					new FileStatistics.Implementation(
						file.number(),
						file.file(),
						file.dataLength(),
						file.totalLength()
					)
				);
			}
			while(file != currentFile);

			return new StorageRawFileStatistics.ChannelStatistics.Implementation(
				this.channelIndex(),
				fileStatistics.size(),
				liveDataLength,
				totalDataLength,
				fileStatistics
			);
		}

		@Override
		public final boolean incrementalFileCleanupCheck(final long nanoTimeBudgetBound)
		{
			return this.internalCheckForCleanup(nanoTimeBudgetBound, this.fileEvaluator);
		}

		@Override
		public final void resetFileCleanupCursor()
		{
			this.fileCleanupCursor = this.headFile.next;
//			DEBUGStorage.println(this.channelIndex + " resetted housekeeping to first file " + this.housekeepingFile.number() + " for head file " + this.headFile.number());
		}

		@Override
		public final boolean issuedFileCleanupCheck(
			final long                               nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolver
		)
		{
//			DEBUGStorage.println(this.channelIndex + " processing issued file cleanup check, time bound = "
//				+ nanoTimeBudgetBound
//			);

			/*
			 * An explicitely issues file cleanup check has to reset the cursor (start from beginning) and no matter
			 * if it completes or not, the cursor has to be reset again at the end.
			 * Rationale:
			 * 1.)
			 * different dissolving evaluators judge files differently and therefore must check all the files
			 * by themselves and also not just leave their last checked file for another evaluator to continue,
			 * potentially skipping files before that that other evaluator would have judged differently,
			 * but let the other (e.g. internal) evaluator evaluate all files again, i.e. reset.
			 *
			 * 2.)
			 * Having to re-check already checked files (either for the internal evaluator due to resetting or
			 * for the same passed evaluator on multiple calls) is extremely quick and the cleanup checking
			 * will quickly get to the next file that actually required cleanup or complete quickly.
			 */
			this.resetFileCleanupCursor();
			try
			{
				return this.internalCheckForCleanup(
					nanoTimeBudgetBound,
					coalesce(fileDissolver, this.fileEvaluator)
				);
			}
			finally
			{
				this.resetFileCleanupCursor();
			}
		}

		private void deletePendingFile(final StorageDataFile.Implementation file)
		{
//			DEBUGStorage.println(this.channelIndex + " deleted pending file " + file);
			if(this.pendingFileDeletes < 1)
			{
				/* (31.10.2014 TM)TODO: Proper storage inconsistency handling
				 *  May never just throw an exception and potentially kill the channel thread
				 *  Instead must signal the storage managr (one way or another) to shutdown so that no other
				 *  thread continues working and ruins something.
				 */
				throw new RuntimeException(this.channelIndex() + " has inconsistent pending deletes: count = " + this.pendingFileDeletes + ", wants to delete " + file); // (31.10.2014 TM)EXCP: proper exception
			}
			this.pendingFileDeletes--;
			this.deleteFile(file);
		}

		private boolean internalCheckForCleanup(
			final long                               nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolver
		)
		{
			if(!DEBUG_ENABLE_FILE_CLEANUP)
			{
				return true;
			}

			if(this.fileCleanupCursor == null)
			{
//				DEBUGStorage.println(this.channelIndex + " aborting file house keeping (all files checked)");
				return true;
			}

//			DEBUGStorage.println(this.channelIndex + " cleanupcheck with budget of " + (nanoTimeBudgetBound - System.nanoTime()));

//			DEBUGStorage.println(this.channelIndex + " checks for file cleanup with budget " + (nanoTimeBudgetBound - System.nanoTime()));

			/* (24.09.2014 TM)TOdO: Channel refuses to clean up files sometimes
			 * Sometimes, the housekeepingfile of one channel is null when cleanup is issued,
			 * resulting in old files not being cleaned up at all.
			 * Question is:
			 * - Why is the the housekeeping file null if there is still something to clean up?
			 *   especially if the big file is the only file and it obviously hadn't been checked.
			 * (17.11.2014 TM)NOTE:
			 * Don't know if this can happen at all since the fixed GC race condition.
			 * Endless testing with both incremental and issued full house keeping never caused any problem since then.
			 */
			StorageDataFile.Implementation cycleAnchorFile = this.fileCleanupCursor;

			// intentionally no minimum first loop execution as cleanup is not important if the system has heavy load
			while(this.fileCleanupCursor != null && System.nanoTime() < nanoTimeBudgetBound)
			{
				// never check current head file for dissolving
//				DEBUGStorage.println(this.channelIndex + " (head " + this.headFile.number() + ")" + " checking " + this.fileCleanupCursor);

				// this never applies to head files automatically
				if(this.fileCleanupCursor.hasNoUsers())
				{
					// delete pending file and do special case checking
					this.deletePendingFile(this.fileCleanupCursor);

					// account for special case of removed file being the anchor file (sadly redundant to below)
					if(this.fileCleanupCursor == cycleAnchorFile)
					{
						this.fileCleanupCursor = cycleAnchorFile = cycleAnchorFile.next;
						continue;
					}
				}
				else if(fileDissolver.needsDissolving(this.fileCleanupCursor))
				{
					if(this.fileCleanupCursor == this.headFile)
					{
						this.createNextStorageFile();
					}

//					DEBUGStorage.println(this.channelIndex + " dissolves " + this.fileCleanupCursor);
					if(!this.incrementalDissolveStorageFile(this.fileCleanupCursor, nanoTimeBudgetBound))
					{
//						DEBUGStorage.println(this.channelIndex + " dissolving not completed of " + this.housekeepingFile);
						continue;
					}
					// file has been dissolved completely and deleted, do special case checking here as well.

					// account for special case of removed file being the anchor file (sadly redundant to above)
					if(this.fileCleanupCursor == cycleAnchorFile)
					{
						this.fileCleanupCursor = cycleAnchorFile = cycleAnchorFile.next;
						continue;
					}

					/* Reaching here means normal case of advancing the house keeping file.
					 * Either a healthy file or a removed file that is not the anchor special case.
					 */
				}

				// Advance to next file, abort if full cycle is completed.
				if((this.fileCleanupCursor = this.fileCleanupCursor.next) == cycleAnchorFile)
				{
					// if there are still pending deletes, file house keeping cannot be turned off
					if(this.pendingFileDeletes > 0)
					{
//						DEBUGStorage.println(this.channelIndex + " still has pending deletes: " + this.pendingFileDeletes);

						// at least one more file is pending deletion
						break;
					}

//					DEBUGStorage.println(this.channelIndex + " completed file checking");

					/* House keeping can be completely disabled for now as everything has been checked.
					 * Will be resetted by the next write, see #resetHousekeeping.
					 */
//					DEBUGStorage.println(this.channelIndex + " completed file checking.");
					this.fileCleanupCursor = null;
				}
			}

//			DEBUGStorage.println(this.channelIndex + " done with file checking. Complete: " + (this.housekeepingFile == null) + ". Time left: " + (nanoTimeBudgetBound - System.nanoTime()));
			return this.fileCleanupCursor == null;
		}

		private boolean incrementalDissolveStorageFile(
			final StorageDataFile.Implementation file               ,
			final long                           nanoTimeBudgetBound
		)
		{
//			DEBUGStorage.println("incrementally dissolving " + file);

			if(this.incrementalTransferEntities(file, nanoTimeBudgetBound))
			{
//				DEBUGStorage.println(" * dissolved completely, deleting: " + file);

				// decrement user count if all entities have been transferred.
				if(file.decrementUserCount())
				{
//					DEBUGStorage.println(this.channelIndex + " deleting right away: " + file);

					// if file's content was migrated completely and there are no more users, remove the file
					this.deleteFile(file);
					return true;
				}

//				DEBUGStorage.println(this.channelIndex + " scheduling for later deletion: " + file);

				// file has no more content but can't be deleted yet. Schedule for later deletion..
				this.pendingFileDeletes++;
				return false;
			}

//			DEBUGStorage.println(" * incrementally dissolving not done yet: " + file);
			return false;
		}

		private void deleteFile(final StorageDataFile.Implementation file)
		{
//			DEBUGStorage.println(this.channelIndex + " deleting " + file);

			file.detach();
			file.close();

			/* must write transaction file entry BEFORE actually deleting the file (inverted logic)
			 * Otherwise, consider the following scenario:
			 * File gets physically deleted, but process was terminated before the transactions file entry could have
			 * been written. On the next start, the initialization validation would expect the file to still exist
			 * (because it found no deletion entry), but the file is no longer there. From the validation's
			 * perspective, this is a missing file and therefore an error (happened once during testing!).
			 *
			 * The registration logic must be inverted in this case:
			 * First register the file to be deleted (no longer needed), then, after that entry is ensured,
			 * the file can be physically deleted (or left alive because of a killed process).
			 * This way, the next startup validation know that the file is no longer needed and can react accrodingly
			 * (keep it alive to re-evaluate it or delete it, etc.)
			 */
			this.writeTransactionsEntryFileDeletion(file.totalLength(), this.timestampProvider.currentNanoTimestamp(), file.number());
			this.writer.flush(this.fileTransactions);
			this.writeListener.registerDelete(file);

			// physically delete file after the transactions entry is ensured
			this.writer.delete(file);
		}

		private boolean incrementalTransferEntities(final StorageDataFile.Implementation file, final long nanoTimeBudgetBound)
		{
			// check for new head file in any case
			this.checkForNewFile();

			try
			{
				// dissolve file to as much head files as needed.
				while(file.hasContent() && System.nanoTime() < nanoTimeBudgetBound)
				{
//					DEBUGStorage.println("transferring one head chain of " + file);
					this.transferOneChainToHeadFile(file);
//					DEBUGStorage.println(" * result: " + file);
				}
			}
			finally
			{
				/* this method only gets called if there is something to transfer, hence at least one entity must have
				 * been transferred, hence a flush is always apropriate at the end. This also applies to exceptions.
				 */
				this.flush();
			}


//			DEBUGStorage.println(" * transfer returning (" + System.nanoTime() + " / " + nanoTimeBudgetBound + "): " + file);

			// if entity migration was completed before time ran out, the file has no more content.
			return !file.hasContent();
		}


		ImportHelper importHelper;

		final void prepareImport()
		{
			this.importHelper = new ImportHelper(this.headFile);
			try
			{
				this.createNextStorageFile();
			}
			catch(final Exception e)
			{
				this.importHelper = null;
				throw e; // (25.07.2014 TM)EXCP: proper exception
			}
		}

		public void copyData(final StorageChannelImportSourceFile importFile)
		{
//			DEBUGStorage.println(this.channelIndex + " processing import source file " + importFile);
			importFile.iterateBatches(this.importHelper.setFile(importFile));

			// batches have been written without flushing/forcing for performance reasons. Ensure that at the end.
			this.flush();
		}

		public void commitImport(final long taskTimestamp)
		{
//			DEBUGStorage.println(this.channelIndex + " committing import data (entity registering)");

			// caching variables
			final StorageEntityCache.Implementation entityCache = this.entityCache;
			final StorageDataFile.Implementation    headFile    = this.headFile   ;

			final long currentTotalLength = this.headFile.totalLength();
			      long loopFileLength     = currentTotalLength;

			// (05.01.2015)TODO: batch copying must ensure that entity position limit of 2 GB is not exceeded
			for(final StorageChannelImportBatch batch : this.importHelper.importBatches)
			{
				// register each entity in the batch (possibly just one)
				for(StorageChannelImportEntity entity = batch.first(); entity != null; entity = entity.next())
				{
					entityCache
					.putEntity(entity.objectId(), entity.type())
					.updateStorageInformation(entity.length(), headFile, checkArrayRange(loopFileLength));
					loopFileLength += entity.length();
				}
			}

			final long copyLength = loopFileLength - currentTotalLength;
			this.headFile.increaseContentLength(copyLength);
			this.cleanupImportHelper();

//			DEBUGStorage.println(this.channelIndex + " writing import store entry for " + this.headFile);
			this.writeTransactionsEntryStore(loopFileLength, taskTimestamp);
			this.flush(); // flush before reporting the store to the listener
			this.writeListener.registerStore(headFile, currentTotalLength, copyLength);
		}

		final void cleanupImportHelper()
		{
			this.importHelper = null;
		}

		final void importBatch(final StorageFile file, final long position, final long length)
		{
			// ignore dummy batches (e.g. transfer file continuation head dummy) and no-op batches in general
			if(length == 0)
			{
				return;
			}

			this.checkForNewFile();
//			DEBUGStorage.println(this.channelIndex + " copying batch from source @" + position + "[" + length + "] to file #" + this.headFile.number());
			this.writer.copy(file, this.headFile, position, length);
		}

		final void rollbackImport()
		{
			if(this.importHelper == null)
			{
				// already rolled back, abort before deleting valid files
				return;
			}

			final StorageDataFile.Implementation first  = this.headFile.next;
			StorageDataFile.Implementation       doomed = this.importHelper.preImportHeadFile.next;
			this.headFile.next = null;
			(first.prev = this.headFile = this.importHelper.preImportHeadFile).next = first;

			final BulkList<IOException> exceptions = BulkList.New();
			while(doomed != null)
			{
				try
				{
					doomed.terminate(this.writer);
				}
				catch(final IOException e)
				{
					exceptions.add(e);
				}
				doomed = doomed.next;
			}
			this.cleanupImportHelper();

			if(!exceptions.isEmpty())
			{
				throw new RuntimeException(exceptions.first()); // (25.07.2014 TM)EXCP: proper exception
			}
		}


		final class ImportHelper implements Consumer<StorageChannelImportBatch>
		{
			final StorageDataFile.Implementation      preImportHeadFile;
			final BulkList<StorageChannelImportBatch> importBatches     = BulkList.New(1000);
			      StorageFile                         file             ;


			ImportHelper(final StorageDataFile.Implementation preImportHeadFile)
			{
				super();
				this.preImportHeadFile = preImportHeadFile;
			}

			@Override
			public void accept(final StorageChannelImportBatch batch)
			{
				this.importBatches.add(batch);
				Implementation.this.importBatch(this.file, batch.fileOffset(), batch.fileLength());
			}

			final ImportHelper setFile(final StorageFile file)
			{
				this.file = file;
				return this;
			}


		}


	}

}
