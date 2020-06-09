package one.microstream.storage.types;


import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.memory.XMemory;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionIoReading;
import one.microstream.storage.exceptions.StorageExceptionIoWritingChunk;
import one.microstream.storage.types.StorageRawFileStatistics.FileStatistics;
import one.microstream.storage.types.StorageTransactionsAnalysis.EntryAggregator;
import one.microstream.time.XTime;
import one.microstream.typing.XTypes;
import one.microstream.util.BufferSizeProvider;


// note that the name channel refers to the entity hash channel, not an nio channel
public interface StorageFileManager extends StorageChannelResetablePart
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

	@Override
	public int channelIndex();

	@Override
	public void reset();
	
	public long[] storeChunks(long timestamp, ByteBuffer[] dataBuffers) throws StorageExceptionIoWritingChunk;

	public void rollbackWrite();

	public void commitWrite();

	public StorageInventory readStorage();

	public StorageIdAnalysis initializeStorage(
		long             taskTimestamp           ,
		long             consistentStoreTimestamp,
		StorageInventory storageInventory        ,
		StorageChannel   parent
	);

	public ZStorageDataFile<?> currentStorageFile();

	public void iterateStorageFiles(Consumer<? super ZStorageDataFile<?>> procedure);

	public boolean incrementalFileCleanupCheck(long nanoTimeBudgetBound);

	public boolean issuedFileCleanupCheck(long nanoTimeBudget);

	public void exportData(StorageIoHandler fileHandler);

	public StorageRawFileStatistics.ChannelStatistics createRawFileStatistics();

	// this is not "reset" in terms of "set to initial state", more like a "go back to the start of the chain".
	public void restartFileCleanupCursor();



	public final class Default implements StorageFileManager, StorageReaderCallback, StorageFileUser
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		// the only reason for this limit is to have an int instead of a long for the item's file position.
		static final int MAX_FILE_LENGTH = Integer.MAX_VALUE;

		// (22.05.2015 TM)TODO: Debug Flag to disable file cleanup for testing
		private static final boolean DEBUG_ENABLE_FILE_CLEANUP = true;



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

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



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// state 1.0: immutable or stateless (as far as this implementation is concerned)

		private final int                                  channelIndex                 ;
		private final StorageInitialDataFileNumberProvider initialDataFileNumberProvider;
		private final StorageTimestampProvider             timestampProvider            ;
		private final StorageFileProvider                  storageFileProvider          ;
		private final StorageDataFileEvaluator             dataFileEvaluator            ;
		private final StorageEntityCache.Default           entityCache                  ;
		private final StorageFileReader                    reader                       ;
		private final StorageFileWriter                    writer                       ;
		private final StorageBackupHandler                 backupHandler                ;
		
		// to avoid permanent lambda instantiation
		private final Consumer<? super StorageLiveDataFile.Default> deleter        = this::deleteFile       ;
		private final Consumer<? super StorageLiveDataFile.Default> pendingDeleter = this::deletePendingFile;
		
		
		// state 1.1: entry buffers. Don't need to be resetted. See comment in reset().

		// all ".clear()" calls on these buffers are only for flushing them out. Filling them happens only via address.
		private final ByteBuffer[]
			entryBufferFileCreation   = {XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthFileCreation())}  ,
			entryBufferStore          = {XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthStore())}         ,
			entryBufferTransfer       = {XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthTransfer())}      ,
			entryBufferFileDeletion   = {XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthFileCreation())}  ,
			entryBufferFileTruncation = {XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthFileTruncation())}
		;

		private final long
			entryBufferFileCreationAddress   = XMemory.getDirectByteBufferAddress(this.entryBufferFileCreation[0])  ,
			entryBufferStoreAddress          = XMemory.getDirectByteBufferAddress(this.entryBufferStore[0])         ,
			entryBufferTransferAddress       = XMemory.getDirectByteBufferAddress(this.entryBufferTransfer[0])      ,
			entryBufferFileDeletionAddress   = XMemory.getDirectByteBufferAddress(this.entryBufferFileDeletion[0])  ,
			entryBufferFileTruncationAddress = XMemory.getDirectByteBufferAddress(this.entryBufferFileTruncation[0])
		;

		// Entry Buffers have their "effectively immutable" first parts initialized once and never changed again.
		{
			StorageTransactionsAnalysis.Logic.initializeEntryFileCreation  (this.entryBufferFileCreationAddress  );
			StorageTransactionsAnalysis.Logic.initializeEntryStore         (this.entryBufferStoreAddress         );
			StorageTransactionsAnalysis.Logic.initializeEntryTransfer      (this.entryBufferTransferAddress      );
			StorageTransactionsAnalysis.Logic.initializeEntryFileDeletion  (this.entryBufferFileDeletionAddress  );
			StorageTransactionsAnalysis.Logic.initializeEntryFileTruncation(this.entryBufferFileTruncationAddress);
		}
		
		
		// state 2.0: final references to mutable instances, i.e. content must be cleared on reset

		// cleared by clearStandardByteBuffer() / reset().
		private final ByteBuffer standardByteBuffer;
		
		
		// state 3.0: mutable fields. Must be cleared on reset.
		
		// cleared and nulled by clearTransactionsFile() / clearRegisteredFiles() / reset()
		private StorageLiveTransactionsFile fileTransactions;
		
		// cleared and nulled by clearRegisteredFiles() / reset()
		private StorageLiveDataFile.Default fileCleanupCursor;

		// cleared by clearUncommittedDataLength() / reset()
		private long uncommittedDataLength;

		// cleared in reset() directly, but kind of irrelevant.
		private int pendingFileDeletes;
		
		
		// state 3.1: variable length content

		// cleared and nulled by clearRegisteredFiles() / reset()
		private StorageLiveDataFile.Default headFile;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final int                                  channelIndex                 ,
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider,
			final StorageTimestampProvider             timestampProvider            ,
			final StorageFileProvider                  storageFileProvider          ,
			final StorageDataFileEvaluator             dataFileEvaluator            ,
			final StorageEntityCache.Default           entityCache                  ,
			final StorageFileReader                    reader                       ,
			final StorageFileWriter                    writer                       ,
			final BufferSizeProvider                   standardBufferSizeProvider   ,
			final StorageBackupHandler                 backupHandler
		)
		{
			super();
			this.channelIndex                  = notNegative(channelIndex)                 ;
			this.initialDataFileNumberProvider =     notNull(initialDataFileNumberProvider);
			this.timestampProvider             =     notNull(timestampProvider)            ;
			this.dataFileEvaluator             =     notNull(dataFileEvaluator)            ;
			this.storageFileProvider           =     notNull(storageFileProvider)          ;
			this.entityCache                   =     notNull(entityCache)                  ;
			this.reader                        =     notNull(reader)                       ;
			this.writer                        =     notNull(writer)                       ;
			this.backupHandler                 =     mayNull(backupHandler)                ;
			
			this.standardByteBuffer = XMemory.allocateDirectNative(
				standardBufferSizeProvider.provideBufferSize()
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		final <L extends Consumer<StorageEntity.Default>> L iterateEntities(final L logic)
		{
			// (01.04.2016)XXX: not tested yet

			final StorageLiveDataFile.Default head = this.headFile;
			StorageLiveDataFile.Default file = head; // initial reference, but gets handled at the end
			do
			{
				file = file.next;
				final StorageEntity.Default tail = file.tail;
				for(StorageEntity.Default entity = file.head; (entity = entity.fileNext) != tail;)
				{
					logic.accept(entity);
				}
			}
			while(file != head);

			return logic;
		}

		// (08.06.2020 TM)FIXME: priv#49: delete
		@Deprecated
		final boolean isHeadFile(final ZStorageDataFile.Default dataFile)
		{
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		final boolean isHeadFile(final StorageLiveDataFile.Default dataFile)
		{
			return this.headFile == dataFile;
		}

		private void addFirstFile()
		{
			// no need for a resetting catch since this method is called in a resetting context already
			this.createNewStorageFile(
				this.initialDataFileNumberProvider.provideInitialDataFileNumber(this.channelIndex())
			);
		}
		
		final void clearTransactionsFile()
		{
			if(this.fileTransactions != null)
			{
				this.fileTransactions.unregisterUsageClosing(this, null);
				this.fileTransactions = null;
			}
		}

		final void clearRegisteredFiles()
		{
			/* (07.07.2016 TM)TODO: StorageFileCloser
			 * to abstract the delicate task of closing files.
			 * Or better enhance StorageFileProvider to a StorageFileHandler
			 * that handles both creation and closing.
			 */
			this.clearTransactionsFile();

			if(this.headFile == null)
			{
				return; // already cleared or no files in the first place
			}

			final StorageLiveDataFile.Default headFile = this.headFile;

			StorageLiveDataFile.Default file = headFile;
			do
			{
				file.unregisterUsageClosing(this, null);
			}
			while((file = file.next) != headFile);

			this.fileCleanupCursor = this.headFile = null;
		}

		private ByteBuffer buffer(final int length)
		{
			if(length > this.standardByteBuffer.capacity())
			{
				return XMemory.allocateDirectNative(length);
			}
			this.standardByteBuffer.clear().limit(length);

			return this.standardByteBuffer;
		}

		private void clearBuffer(final ByteBuffer buffer)
		{
			buffer.clear();
			if(buffer != this.standardByteBuffer)
			{
				XMemory.deallocateDirectByteBuffer(buffer); // hope this works, not tested yet
			}
		}

		
		final void transferOneChainToHeadFile(final StorageLiveDataFile.Default sourceFile)
		{
			final StorageLiveDataFile.Default headFile = this.headFile           ;
			final StorageEntity.Default   first    = sourceFile.head.fileNext;
			      StorageEntity.Default   last     = null                    ;
			      StorageEntity.Default   current  = first                   ;

			final long copyStart                = first.storagePosition                     ;
			final long targetFileOldTotalLength = headFile.totalLength()                    ;
			final long maximumFileSize          = this.dataFileEvaluator.fileMaximumSize()  ;
			final long freeSpace                = maximumFileSize - targetFileOldTotalLength;
			      long copyLength               = 0                                         ;

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
				current.typeInFile      = headFile.typeInFile(current.typeInFile.type);
								
				// update position to the one in the target file (old length plus current copy length)
				current.storagePosition = XTypes.to_int(targetFileOldTotalLength + copyLength);

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
			final StorageLiveDataFile.Default sourceFile,
			final long                           copyStart ,
			final long                           copyLength
		)
		{
//			DEBUGStorage.println(
//				this.channelIndex + " transerring " + copyLength + " bytes from position " + copyStart
//				+ " from " + sourceFile + " to " + targetFile
//			);
			
			final StorageLiveDataFile.Default headFile = this.headFile;

			// do the actual file-level copying in one go at the end and validate the byte count to be sure
			this.writer.writeTransfer(sourceFile, copyStart, copyLength, headFile);

			// increase content length by length of chain
			// (15.02.2019 TM)NOTE: changed from arithmetic inside #addChainToTail to directly using copyLength in here.
			headFile.increaseContentLength(copyLength);
//			headFile.increaseContentLength(last.storagePosition - first.storagePosition + last.length);
			
			final long newHeadFileLength = headFile.totalLength();
			final long timestamp         = this.timestampProvider.currentNanoTimestamp();
			this.writeTransactionsEntryTransfer(sourceFile, copyStart, copyLength, timestamp, newHeadFileLength);
			
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
		}

	

		private void createNewStorageFile(final long fileNumber)
		{
//			DEBUGStorage.println(this.channelIndex + " creating new head file " + fileNumber);

			final ZStorageInventoryFile file = this.storageFileProvider.provideDataFile(
				this.channelIndex(),
				fileNumber
			).inventorize();

			/*
			 * File#length is incredibly slow compared to FileChannel#size (although irrelevant here),
			 * but still the file length has to be checked before the channel is created, etc.
			 */
			if(file.length() != 0)
			{
				// (29.05.2014 TM)EXCP: proper exception
				throw new StorageException("New storage file is not empty: " + file);
			}

			// create and register StorageFile instance with an attached channel
			this.registerHeadFile(file);
			this.writeTransactionsEntryFileCreation(0, this.timestampProvider.currentNanoTimestamp(), fileNumber);
		}

		private void registerHeadFile(final ZStorageInventoryFile file)
		{
			this.registerStorageHeadFile(StorageLiveDataFile.Default.New(this, file));
		}
		
		private void registerStorageHeadFile(final StorageLiveDataFile.Default storageFile)
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

		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}

		@Override
		public final StorageLiveDataFile.Default currentStorageFile()
		{
			return this.headFile;
		}

		@Override
		public void iterateStorageFiles(final Consumer<? super ZStorageDataFile<?>> procedure)
		{
			// keep current als end marker, but start with first file, use current als last and then quit the loop
			final StorageLiveDataFile.Default current = this.headFile;
			StorageLiveDataFile.Default file = current;
			do
			{
				procedure.accept(file = file.next);
			}
			while(file != current);
		}

		private void checkForNewFile()
		{
			if(this.headFile.needsRetirement(this.dataFileEvaluator))
			{
				this.createNextStorageFile();
			}
		}

		final void createNextStorageFile()
		{
			this.createNewStorageFile(this.headFile.number() + 1);
		}
		
		private long ensureHeadFileTotalLength()
		{
			final long physicalLength = this.headFile.length();
			final long expectedLength = this.headFile.totalLength();
			
			if(physicalLength != expectedLength)
			{
				// (13.03.2019 TM)EXCP: proper exception
				throw new StorageException(
					"Physical length " + physicalLength
					+ " of current head file " + this.headFile.number()
					+ " is not equal its expected length of " + expectedLength
				);
			}
			
			return physicalLength;
		}

		@Override
		public final long[] storeChunks(final long timestamp, final ByteBuffer[] dataBuffers)
			throws StorageExceptionIoWritingChunk
		{
			if(dataBuffers.length == 0)
			{
				return new long[0]; // nothing to write (empty chunk, only header for consistency)
			}
			
			this.checkForNewFile();
			final long   oldTotalLength   = this.ensureHeadFileTotalLength();
			final long[] storagePositions = allChunksStoragePositions(dataBuffers, oldTotalLength);
			final long   writeCount       = this.writer.writeStore(this.headFile, dataBuffers);
			final long   newTotalLength   = oldTotalLength + writeCount;
			
			if(newTotalLength != this.headFile.length())
			{
				throwImpossibleStoreLengthException(timestamp, oldTotalLength, writeCount, dataBuffers);
			}
			
			this.uncommittedDataLength = writeCount;
			
			this.writeTransactionsEntryStore(this.headFile, oldTotalLength, writeCount, timestamp, newTotalLength);
//			DEBUGStorage.println(this.channelIndex + " wrote " + this.uncommittedDataLength + " bytes");

			this.restartFileCleanupCursor();
//			DEBUGStorage.println("Channel " + this.channelIndex + " wrote data for " + timestamp);
			
			return storagePositions;
		}

		@Override
		public final void rollbackWrite()
		{
//			XDebug.debugln(
//				this.channelIndex()
//				+ " rolling back write: truncating "
//				+ this.headFile.file().getName()
//				+ "(length " + this.headFile.file().length()
//				+ ") at " + this.headFile.totalLength()
//			);
			this.writer.truncate(this.headFile, this.headFile.totalLength(), this.storageFileProvider);
		}

		@Override
		public final void commitWrite()
		{
			// commit data length
			this.headFile.increaseContentLength(this.uncommittedDataLength);

			// reset the length change helper field
			this.clearUncommittedDataLength();
		}
		
		final void clearUncommittedDataLength()
		{
			this.uncommittedDataLength = 0;
		}

		// (08.06.2020 TM)FIXME: priv#49: delete
		@Deprecated
		final void loadData(
			final ZStorageDataFile.Default dataFile   ,
			final StorageEntity.Default   entity     ,
			final long                    length     ,
			final long                    cacheChange
		)
		{
//			DEBUGStorage.println(this.channelIndex + " loading entity " + entity);
			final ByteBuffer dataBuffer = this.buffer(X.checkArrayRange(length));
			try
			{
				this.reader.readStorage(dataFile, entity.storagePosition, dataBuffer, this);
				this.putLiveEntityData(entity, XMemory.getDirectByteBufferAddress(dataBuffer), length, cacheChange);
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
		
		final void loadData(
			final StorageLiveDataFile.Default dataFile   ,
			final StorageEntity.Default       entity     ,
			final long                        length     ,
			final long                        cacheChange
		)
		{
//			DEBUGStorage.println(this.channelIndex + " loading entity " + entity);
			final ByteBuffer dataBuffer = this.buffer(X.checkArrayRange(length));
			try
			{
				dataFile.readBytes(dataBuffer, entity.storagePosition);
				this.putLiveEntityData(entity, XMemory.getDirectByteBufferAddress(dataBuffer), length, cacheChange);
			}
			catch(final StorageExceptionIoReading e)
			{
				throw e;
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
			final StorageEntity.Default entity     ,
			final long                         address    ,
			final long                         length     ,
			final long                         cacheChange
		)
		{
			entity.putCacheData(address, length);
			this.entityCache.modifyUsedCacheSize(cacheChange);
		}

		// (08.06.2020 TM)FIXME: priv#49: remove since reading logic is a concern of IoHandler.
		@Override
		public void validateIncrementalRead(
			final ZStorageLockedFile fileChannel  ,
			final long              filePosition ,
			final ByteBuffer        buffer       ,
			final long              lastReadCount
		)
			throws IOException
		{
			if(lastReadCount < 0)
			{
				// (30.06.2013 TM)EXCP: proper exception
				throw new StorageException(this.channelIndex() + " failed to read data at " + filePosition);
			}
			throw new one.microstream.meta.NotImplementedYetError(
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
				// (21.04.2013 TM)EXCP: proper exception
				throw new StorageException(this.channelIndex() + " already initialized");
			}

			final StorageTransactionsAnalysis         transactionsFile = this.readTransactionsFile();
			final EqHashTable<Long, ZStorageInventoryFile> storageFiles     = EqHashTable.New();
			this.storageFileProvider.collectDataFiles(
				f ->
					storageFiles.add(f.number(), f.inventorize()),
				this.channelIndex()
			);
			storageFiles.keys().sort(XSort::compare);

			return new StorageInventory.Default(this.channelIndex(), storageFiles, transactionsFile);
		}

		final StorageTransactionsAnalysis readTransactionsFile()
		{
			final ZStorageInventoryFile file = this.createTransactionsFile();

			if(!file.exists())
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

				final EntryAggregator aggregator = StorageTransactionsAnalysis.Logic.processInputFile(
					channel,
					new EntryAggregator(this.channelIndex())
				);
				return aggregator.yield(file);
			}
			catch(final IOException e)
			{
				ZStorageFile.close(file, e);
				throw new StorageException(e); // (29.08.2014 TM)EXCP: proper exception
			}
		}

		private long validateStorageDataFilesLength(final StorageInventory storageInventory)
		{
			final StorageTransactionsAnalysis tFileAnalysis = storageInventory.transactionsFileAnalysis();
			long unregisteredEmptyLastFileNumber = -1; // -1 for "none"

			if(tFileAnalysis == null || tFileAnalysis.transactionsFileEntries().isEmpty())
			{
				// no transaction file (content) present. Abort and derive later.
				// (06.09.2014 TM)TODO: configurable MissingTransactionsFileHandler callback
				return unregisteredEmptyLastFileNumber;
			}

			final XGettingSequence<ZStorageInventoryFile>    dataFiles = storageInventory.dataFiles().values();
			final EqHashTable<Long, StorageTransactionFileEntry> fileEntries = EqHashTable.New(tFileAnalysis.transactionsFileEntries());
			final ZStorageInventoryFile                      lastFile    = dataFiles.peek();

			for(final ZStorageInventoryFile file : dataFiles)
			{
				final long actualFileLength = file.length();

				// retrieve and remove (= mark as already handled) the corresponding file entry
				final StorageTransactionFileEntry entryFile = fileEntries.removeFor(file.number());
				if(entryFile == null)
				{
					// special case: empty file was created but not registered, can be safely ignored
					if(file == lastFile && actualFileLength == 0)
					{
						unregisteredEmptyLastFileNumber = file.number();
						continue;
					}

					// if the transactions file is present, it must be consistent (i.e. account for all files)
					throw new StorageException(
						this.channelIndex() + " could not find transactions entry for file " + file.number()
					); // (06.09.2014 TM)EXCP: proper exception
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
				throw new StorageException(
					this.channelIndex() + " Length " + actualFileLength + " of file "
					+ file.number() + " is inconsinstent with the transactions entry's length of " + entryFile.length()
				);
			}

			// check that all remaining file entries are deleted files. No non-deleted file may be missing!
			for(final StorageTransactionFileEntry remainingFileEntry : fileEntries.values())
			{
				if(remainingFileEntry.isDeleted())
				{
					continue;
				}

				// (06.09.2014 TM)EXCP: proper exception
				throw new StorageException(
					"Non-deleted data file not found: channel " + this.channelIndex()
					+ ", file " + remainingFileEntry.fileNumber()
				);
			}

			/*
			 * At this point it is guaranteed that all transactions entries and existing files are viable in
			 * terms of file lengths. Viable means exact same length for any non-last file and actual file
			 * length equal to or greater than logged length for last file (i.e. there can be one uncommitted
			 * write at the end which can be safely truncated later on).
			 */

			// return required information about uber special case
			return unregisteredEmptyLastFileNumber;
		}

		@Override
		public StorageIdAnalysis initializeStorage(
			final long             taskTimestamp           ,
			final long             consistentStoreTimestamp,
			final StorageInventory storageInventory        ,
			final StorageChannel   parent
		)
		{
//			DEBUGStorage.println(this.channelIndex + " init for consistent timestamp " + consistentStoreTimestamp);

			// validate file lengths, even in case of no files, to validate transactions entries to that state
			final long unregisteredEmptyLastFileNumber = this.validateStorageDataFilesLength(storageInventory);

			boolean isEmpty = true;
			try
			{
				isEmpty = storageInventory.dataFiles().isEmpty();

				final StorageIdAnalysis idAnalysis;
				if(isEmpty)
				{
					// initialize if there are no files at all (create first file, ensure transactions file)
					this.initializeForNoFiles(taskTimestamp, storageInventory);
					idAnalysis = StorageIdAnalysis.New(0L, 0L, 0L);
					
					// blank initialization to avoid redundantly copying the initial transactions entry (nasty bug).
					this.initializeBackupHandler();
				}
				else
				{
					// register a pending store update to keep state (e.g. GC) in a consistent state.
					this.entityCache.registerPendingStoreUpdate();

					// handle files (read, parse, register items) and ensure transactions file
					idAnalysis = this.initializeForExistingFiles(
						taskTimestamp                  ,
						storageInventory               ,
						consistentStoreTimestamp       ,
						unregisteredEmptyLastFileNumber
					);
					
					// initialization plus synchronization with existing files.
					this.initializeBackupHandler(storageInventory);
				}

				this.restartFileCleanupCursor();
				
//				DEBUGStorage.println(this.channelIndex + " initialization complete, maxOid = " + maxOid);
				return idAnalysis;
			}
			catch(final RuntimeException e)
			{
				// on any exception, reset (clear) the internal state
				parent.reset();
				throw e;
			}
			finally
			{
				if(!isEmpty)
				{
					// clear the previously registered pending store update
					this.entityCache.clearPendingStoreUpdate();
				}
			}
		}
		
		private boolean initializeBackupHandler()
		{
			if(this.backupHandler == null)
			{
				return false;
			}
			
			this.backupHandler.initialize(this.channelIndex());
			
			return true;
		}
		
		private void initializeBackupHandler(final StorageInventory inventory)
		{
			if(!this.initializeBackupHandler())
			{
				return;
			}
			
			this.backupHandler.synchronize(inventory);
		}

		private StorageIdAnalysis initializeForExistingFiles(
			final long             taskTimestamp                  ,
			final StorageInventory storageInventory               ,
			final long             consistentStoreTimestamp       ,
			final long             unregisteredEmptyLastFileNumber
		)
		{
			/*
			 * The data files and all entities in them get initialized in reverse order.
			 * The reason is that for every entity, only the latest, most current version counts.
			 * Reversing the order makes this trivial to implement: for every OID (i.e. entity), only the first
			 * occurance counts and defines type, length and position in the storage. All further occurances
			 * (meaning EARLIER versions) of an already encountered Entity/OID are simply ignored.
			 */
			
			// local variables for readability, debugging and (paranoid) consistency guarantee
			final XGettingSequence<ZStorageInventoryFile> files = storageInventory.dataFiles().values();

			// validate and determine length of last file before any file is processed to recognize errors early
			final long lastFileLength = unregisteredEmptyLastFileNumber >= 0
				? 0
				: this.determineLastFileLength(consistentStoreTimestamp, storageInventory)
			;

			// register items (gaps and entities, with latest version of each entity replacing all previous)
			final StorageEntityInitializer<StorageLiveDataFile.Default> initializer =
				StorageEntityInitializer.New(this.entityCache, f ->
					StorageLiveDataFile.Default.New(this, f)
				)
			;
			this.headFile = initializer.registerEntities(files, lastFileLength);

			// validate entities (only the latest versions) before potential transaction file derivation
			final StorageIdAnalysis idAnalysis = this.entityCache.validateEntities();

			// ensure transactions file before handling last file as truncation needs to write in it
			this.ensureTransactionsFile(taskTimestamp, storageInventory, unregisteredEmptyLastFileNumber);

			// special-case handle the last file
			this.handleLastFile(this.headFile.file, lastFileLength);

			// check if last file is oversized and should be retired right away.
			this.checkForNewFile();

			return idAnalysis;
		}

		private long determineLastFileLength(
			final long             consistentStoreTimestamp,
			final StorageInventory storageInventory
		)
		{
			final StorageTransactionsAnalysis tFileAnalysis = storageInventory.transactionsFileAnalysis();

			if(tFileAnalysis == null || tFileAnalysis.isEmpty())
			{
				/*
				 * if no transactions file was present, it must be assumed that the last file is consistent
				 * (e.g. user manually deleted the transactions file in a consistent database)
				 */
				return storageInventory.dataFiles().values().last().length();
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
				// (10.06.2014 TM)EXCP: proper exception
				throw new StorageException(
					"Inconsistent last timestamps in last file of channel " + this.channelIndex()
				);
			}
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
			final StorageTransactionsAnalysis trFileAn = storageInventory.transactionsFileAnalysis();
			final ZStorageInventoryFile transactionsFile;

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

		private ZStorageInventoryFile createTransactionsFile()
		{
			return this.storageFileProvider.provideTransactionsFile(this.channelIndex()).inventorize();
		}

		private void deriveTransactionsFile(
			final long                 taskTimestamp   ,
			final StorageInventory     storageInventory,
			final ZStorageInventoryFile tfile
		)
		{
			final XGettingSequence<ZStorageInventoryFile> files   = storageInventory.dataFiles().values();
			final ByteBuffer[]                           buffer  = this.entryBufferFileCreation         ;
			final long                                   address = this.entryBufferFileCreationAddress  ;
			final StorageFileWriter                      writer  = this.writer                          ;

			long timestamp = taskTimestamp - storageInventory.dataFiles().size() - 1;

			try
			{
				for(final ZStorageInventoryFile file : files)
				{
					buffer[0].clear();
					StorageTransactionsAnalysis.Logic.setEntryFileCreation(
						address      ,
						file.length(),
						++timestamp  ,
						file.number()
					);
					writer.write(tfile, buffer);
				}
			}
			catch(final Exception e)
			{
				ZStorageFile.close(tfile, e);
				throw e;
			}
		}

		private void writeTransactionsEntryFileCreation(
			final long length   ,
			final long timestamp,
			final long number
		)
		{
			this.entryBufferFileCreation[0].clear();
			StorageTransactionsAnalysis.Logic.setEntryFileCreation(
				this.entryBufferFileCreationAddress,
				length                             ,
				timestamp                          ,
				number
			);
			this.writer.writeTransactionEntryCreate(this.fileTransactions, this.entryBufferFileCreation, this.headFile);
		}

		private void writeTransactionsEntryStore(
			final ZStorageDataFile<?> dataFile              ,
			final long               dataFileOffset        ,
			final long               storeLength           ,
			final long               timestamp             ,
			final long               headFileNewTotalLength
		)
		{
			this.entryBufferStore[0].clear();
			StorageTransactionsAnalysis.Logic.setEntryStore(
				this.entryBufferStoreAddress,
				headFileNewTotalLength      ,
				timestamp
			);
			this.writer.writeTransactionEntryStore(
				this.fileTransactions,
				this.entryBufferStore,
				dataFile             ,
				dataFileOffset       ,
				storeLength
			);
		}

		private void writeTransactionsEntryTransfer(
			final ZStorageDataFile<?> sourceFile            ,
			final long               sourcefileOffset      ,
			final long               copyLength            ,
			final long               timestamp             ,
			final long               headNewFileTotalLength
		)
		{
//			DEBUGStorage.println(this.channelIndex + " writing transfer entry "
//				+sourcefileNumber + " -> " + this.headFile.number() + "\t"
//				+length + "\t"
//				+timestamp + "\t"
//			);
			this.entryBufferTransfer[0].clear();
			StorageTransactionsAnalysis.Logic.setEntryTransfer(
				this.entryBufferTransferAddress,
				headNewFileTotalLength         ,
				timestamp                      ,
				sourceFile.number()            ,
				sourcefileOffset
			);
			
			this.writer.writeTransactionEntryTransfer(
				this.fileTransactions,
				this.entryBufferTransfer,
				sourceFile,
				sourcefileOffset,
				copyLength
			);
//			DEBUGStorage.println(this.channelIndex + " written transfer entry");
		}

		private void writeTransactionsEntryFileDeletion(
			final ZStorageDataFile<?> dataFile ,
			final long               timestamp
		)
		{
			this.entryBufferFileDeletion[0].clear();
			StorageTransactionsAnalysis.Logic.setEntryFileDeletion(
				this.entryBufferFileDeletionAddress,
				dataFile.totalLength()             ,
				timestamp                          ,
				dataFile.number()
			);
			this.writer.writeTransactionEntryDelete(this.fileTransactions, this.entryBufferFileDeletion, dataFile);
		}

		private void writeTransactionsEntryFileTruncation(
			final ZStorageInventoryFile lastFile ,
			final long                 timestamp,
			final long                 newLength
		)
		{
			this.entryBufferFileTruncation[0].clear();
			StorageTransactionsAnalysis.Logic.setEntryFileTruncation(
				this.entryBufferFileTruncationAddress,
				newLength                            ,
				timestamp                            ,
				lastFile.number()                    ,
				lastFile.length()
			);
			this.writer.writeTransactionEntryTruncate(this.fileTransactions, this.entryBufferFileTruncation, lastFile, newLength);
		}

		private void setTransactionsFile(final ZStorageInventoryFile transactionsFile)
		{
			this.fileTransactions = transactionsFile;
			
			transactionsFile.registerUsage(this);
		}
		
		final void clearStandardByteBuffer()
		{
			this.standardByteBuffer.clear();
		}

		@Override
		public final void reset()
		{
			/* Note:
			 * (see field declarations)
             * 1.0) all final fields don't have to (can't) be resetted. Obviously.
             * 1.1) entryBuffers don't have to be resetted since they get filled anew for every write.
			 */
			
			// 2.0) final references to mutable instances
			this.clearStandardByteBuffer();
			
			// 3.X) mutable fields and variable length content
			this.clearUncommittedDataLength();
			this.clearRegisteredFiles();
			
			// at this point, it is either 0 already or it won't matter since everything has been cleared.
			this.pendingFileDeletes = 0;
		}

		final void handleLastFile(
			final ZStorageInventoryFile lastFile      ,
			final long                 lastFileLength
		)
		{
			if(lastFileLength != lastFile.length())
			{
//				XDebug.debugln(
//					this.channelIndex()
//					+ " last file initialization truncating "
//					+ lastFile.file().getName()
//					+ "(length " + lastFile.file().length()
//					+ ") at " + lastFileLength
//				);
				
//				DEBUGStorage.println(this.channelIndex + " truncating last file to " + lastFileLength + " " + lastFile);
				// reaching here means in any case that the file has to be truncated and its header must be updated

				final long timestamp = this.timestampProvider.currentNanoTimestamp();
				
				// write truncation entry (BEFORE the actual truncate)
				this.writeTransactionsEntryFileTruncation(lastFile, timestamp, lastFileLength);

				// (20.06.2014 TM)TODO: truncator function to give a chance to evaluate / rescue the doomed data
				this.writer.truncate(lastFile, lastFileLength, this.storageFileProvider);
			}
		}
		
		@Override
		public void exportData(final StorageIoHandler fileHandler)
		{
			// copy transactions file first so that an incomplete data file transfer can be recognized later.
			final ZStorageInventoryFile backupTrsFile = fileHandler.copyTransactions(this.fileTransactions);
			backupTrsFile.close();

			this.iterateStorageFiles(file ->
			{
				final ZStorageInventoryFile backupDatFile = fileHandler.copyData(file);
				backupDatFile.close();
			});
		}
		
		private static FileStatistics.Default createFileStatistics(final StorageLiveDataFile.Default file)
		{
			return new FileStatistics.Default(
				file.number()    ,
				file.identifier(),
				file.dataLength(),
				file.totalLength()
			);
		}

		@Override
		public final StorageRawFileStatistics.ChannelStatistics createRawFileStatistics()
		{
			StorageLiveDataFile.Default file;
			final StorageLiveDataFile.Default currentFile = file = this.headFile;

			long liveDataLength  = 0;
			long totalDataLength = 0;
			final BulkList<FileStatistics.Default> fileStatistics = BulkList.New();

			do
			{
				file = file.next;
				liveDataLength  += file.dataLength();
				totalDataLength += file.totalLength();
				
				final FileStatistics.Default fileStats = createFileStatistics(file);
				fileStatistics.add(fileStats);
			}
			while(file != currentFile);

			return new StorageRawFileStatistics.ChannelStatistics.Default(
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
			return this.internalCheckForCleanup(nanoTimeBudgetBound, this.dataFileEvaluator);
		}

		@Override
		public final void restartFileCleanupCursor()
		{
			this.fileCleanupCursor = this.headFile.next;
//			DEBUGStorage.println(this.channelIndex + " resetted housekeeping to first file " + this.housekeepingFile.number() + " for head file " + this.headFile.number());
		}

		@Override
		public final boolean issuedFileCleanupCheck(final long nanoTimeBudget)
		{
//			DEBUGStorage.println(this.channelIndex + " processing issued file cleanup check, time budget = "
//				+ nanoTimeBudget
//			);
			
			final long nanoTimeBudgetBound = XTime.calculateNanoTimeBudgetBound(nanoTimeBudget);

			return this.internalCheckForCleanup(nanoTimeBudgetBound, this.dataFileEvaluator);
		}

		private void deletePendingFile(final StorageLiveDataFile.Default file)
		{
//			DEBUGStorage.println(this.channelIndex + " deleted pending file " + file);
			if(this.pendingFileDeletes < 1)
			{
				/* (31.10.2014 TM)TODO: Proper storage inconsistency handling
				 *  May never just throw an exception and potentially kill the channel thread
				 *  Instead must signal the storage managr (one way or another) to shutdown so that no other
				 *  thread continues working and ruins something.
				 */
				throw new StorageException(this.channelIndex() + " has inconsistent pending deletes: count = " + this.pendingFileDeletes + ", wants to delete " + file); // (31.10.2014 TM)EXCP: proper exception
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

//			DEBUGStorage.println(this.channelIndex + " cleanupcheck with budget of " + (nanoTimeBudget));

//			DEBUGStorage.println(this.channelIndex + " checks for file cleanup with budget " + (nanoTimeBudget));
						
			StorageLiveDataFile.Default cycleAnchorFile = this.fileCleanupCursor;

			// intentionally no minimum first loop execution as cleanup is not important if the system has heavy load
			while(this.fileCleanupCursor != null && System.nanoTime() < nanoTimeBudgetBound)
			{
				// never check current head file for dissolving
//				DEBUGStorage.println(this.channelIndex + " (head " + this.headFile.number() + ")" + " checking " + this.fileCleanupCursor);

				// delete pending file and do special case checking. This never applies to head files automatically
				if(!this.fileCleanupCursor.hasUsers())
				{
					// an iterable (non-detached) file with no users can only mean a pending delete.
					if(!this.fileCleanupCursor.executeIfUnsuedData(this.pendingDeleter))
					{
						// should a new usage have been registered right after checking, then break and try again later
						break;
					}
					
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
			final StorageLiveDataFile.Default file               ,
			final long                    nanoTimeBudgetBound
		)
		{
//			DEBUGStorage.println("incrementally dissolving " + file);

			if(this.incrementalTransferEntities(file, nanoTimeBudgetBound))
			{
//				DEBUGStorage.println(" * dissolved completely, deleting: " + file);
				if(file.unregisterUsageClosingData(this, this.deleter))
				{
//					DEBUGStorage.println(this.channelIndex + " deleted right away: " + file);
					return true;
				}

//				DEBUGStorage.println(this.channelIndex + " scheduling for later deletion: " + file);

				// file has no more content but can't be deleted yet. Schedule for later deletion.
				this.pendingFileDeletes++;
				return false;
			}

//			DEBUGStorage.println(" * incrementally dissolving not done yet: " + file);
			return false;
		}

		private void deleteFile(final StorageLiveDataFile.Default file)
		{
//			DEBUGStorage.println(this.channelIndex + " deleting " + file);

			file.detach();
			file.close(); // idempotent. No harm in calling on an already closed file.

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
			this.writeTransactionsEntryFileDeletion(file, this.timestampProvider.currentNanoTimestamp());

			// physically delete file after the transactions entry is ensured
			this.writer.delete(file, this.storageFileProvider);
		}

		private boolean incrementalTransferEntities(
			final StorageLiveDataFile.Default file               ,
			final long                    nanoTimeBudgetBound
		)
		{
			// check for new head file in any case
			this.checkForNewFile();

			// dissolve file to as much head files as needed.
			while(file.hasContent() && System.nanoTime() < nanoTimeBudgetBound)
			{
//				DEBUGStorage.println("transferring one head chain of " + file);
				this.transferOneChainToHeadFile(file);
//				DEBUGStorage.println(" * result: " + file);
			}

//			DEBUGStorage.println(" * transfer returning (" + System.nanoTime() + " / " + nanoTimeBudgetBound + "): " + file);

			// if entity migration was completed before time ran out, the file has no more content.
			return !file.hasContent();
		}
		
		final StorageEntity.Default getFirstEntity()
		{
			final StorageLiveDataFile.Default currentFile = this.currentStorageFile();
			if(currentFile == null)
			{
				// can occur when an exception causes a reset call during initialization
				return null;
			}
			
			final StorageLiveDataFile.Default startingFile = currentFile.next;
			StorageLiveDataFile.Default file = startingFile;
			do
			{
				if(file.head.fileNext != startingFile.tail)
				{
					return file.head.fileNext;
				}
			}
			while((file = file.next) != startingFile);
			
			// no file contains any (proper) entity. So return null.
			return null;
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

		public void copyData(final ZStorageChannelImportSourceFile importFile)
		{
//			DEBUGStorage.println(this.channelIndex + " processing import source file " + importFile);
			importFile.iterateBatches(this.importHelper.setFile(importFile));
		}

		public void commitImport(final long taskTimestamp)
		{
//			DEBUGStorage.println(this.channelIndex + " committing import data (entity registering)");

			// caching variables
			final StorageEntityCache.Default entityCache = this.entityCache;
			final StorageLiveDataFile.Default    headFile    = this.headFile   ;

			final long oldTotalLength = this.headFile.totalLength();
			      long loopFileLength = oldTotalLength;

			// (05.01.2015 TM)TODO: batch copying must ensure that entity position limit of 2 GB is not exceeded
			for(final StorageChannelImportBatch batch : this.importHelper.importBatches)
			{
				// register each entity in the batch (possibly just one)
				for(StorageChannelImportEntity entity = batch.first(); entity != null; entity = entity.next())
				{
					final StorageEntity.Default actual = entityCache.putEntity(entity.objectId(), entity.type());
					actual.updateStorageInformation(entity.length(), X.checkArrayRange(loopFileLength));
					headFile.appendEntry(actual);
					loopFileLength += entity.length();
				}
			}

			final long copyLength = loopFileLength - oldTotalLength;
			headFile.increaseContentLength(copyLength);
			this.cleanupImportHelper();

//			DEBUGStorage.println(this.channelIndex + " writing import store entry for " + this.headFile);
			this.writeTransactionsEntryStore(this.headFile, oldTotalLength, copyLength, taskTimestamp, loopFileLength);
		}

		final void cleanupImportHelper()
		{
			this.importHelper = null;
		}

		final void importBatch(final ZStorageLockedFile file, final long position, final long length)
		{
			// ignore dummy batches (e.g. transfer file continuation head dummy) and no-op batches in general
			if(length == 0)
			{
				return;
			}

			this.checkForNewFile();
//			DEBUGStorage.println(this.channelIndex + " importing batch from source @" + position + "[" + length + "] to file #" + this.headFile.number());
			this.writer.writeImport(file, position, length, this.headFile);
		}

		final void rollbackImport()
		{
			if(this.importHelper == null)
			{
				// already rolled back, abort before deleting valid files
				return;
			}

			final StorageLiveDataFile.Default first  = this.headFile.next;
			StorageLiveDataFile.Default       doomed = this.importHelper.preImportHeadFile.next;
			this.headFile.next = null;
			(first.prev = this.headFile = this.importHelper.preImportHeadFile).next = first;

			final BulkList<RuntimeException> exceptions = BulkList.New();
			while(doomed != null)
			{
				try
				{
					this.terminateFile(doomed);
				}
				catch(final RuntimeException e)
				{
					exceptions.add(e);
				}
				doomed = doomed.next;
			}
			this.cleanupImportHelper();

			if(!exceptions.isEmpty())
			{
				throw new StorageException(exceptions.first()); // (25.07.2014 TM)EXCP: proper exception
			}
		}
		
		private void terminateFile(final StorageLiveDataFile.Default file)
		{
			file.close();
			this.writer.delete(file, this.storageFileProvider);
		}

		final class ImportHelper implements Consumer<StorageChannelImportBatch>
		{
			final StorageLiveDataFile.Default             preImportHeadFile;
			final BulkList<StorageChannelImportBatch> importBatches     = BulkList.New(1000);
			      ZStorageLockedFile                   file             ;


			ImportHelper(final StorageLiveDataFile.Default preImportHeadFile)
			{
				super();
				this.preImportHeadFile = preImportHeadFile;
			}

			@Override
			public void accept(final StorageChannelImportBatch batch)
			{
				this.importBatches.add(batch);
				StorageFileManager.Default.this.importBatch(this.file, batch.fileOffset(), batch.fileLength());
			}

			final ImportHelper setFile(final ZStorageLockedFile file)
			{
				this.file = file;
				return this;
			}


		}
		
		static void throwImpossibleStoreLengthException(
			final long         timestamp            ,
			final long         currentTotalLength   ,
			final long         uncommittedDataLength,
			final ByteBuffer[] dataBuffers
		)
		{
			final VarString vs = VarString.New();
			vs
			.add("Impossible store length:").lf()
			.add("timestamp = ").add(timestamp).lf()
			.add("currentTotalLength = ").add(currentTotalLength).lf()
			.add("uncommittedDataLength = ").add(uncommittedDataLength).lf()
			.add("resulting length = ").add(currentTotalLength + uncommittedDataLength).lf()
			.add("dataBuffers: ")
			;
			if(dataBuffers.length == 0)
			{
				vs.add("[none]");
			}
			else
			{
				for(int i = 0; i < dataBuffers.length; i++)
				{
					vs.lf()
					.add('#').add(i).add(": ")
					.add("limit = ").add(dataBuffers[i].limit()).add(", ")
					.add("position = ").add(dataBuffers[i].position()).add(", ")
					.add("capacity = ").add(dataBuffers[i].capacity()).add(";")
					;
				}
			}
			
			throw new StorageException(vs.toString());
		}

	}
		
}
