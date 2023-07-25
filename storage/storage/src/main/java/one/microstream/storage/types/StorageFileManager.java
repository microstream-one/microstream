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


import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.slf4j.Logger;

import one.microstream.X;
import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.chars.VarString;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.exceptions.MultiCauseException;
import one.microstream.memory.XMemory;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionConsistency;
import one.microstream.storage.exceptions.StorageExceptionIoReading;
import one.microstream.storage.exceptions.StorageExceptionIoWriting;
import one.microstream.storage.exceptions.StorageExceptionIoWritingChunk;
import one.microstream.storage.types.StorageRawFileStatistics.FileStatistics;
import one.microstream.storage.types.StorageTransactionsAnalysis.EntryAggregator;
import one.microstream.typing.Disposable;
import one.microstream.typing.XTypes;
import one.microstream.util.BufferSizeProvider;
import one.microstream.util.logging.Logging;


// note that the name channel refers to the entity hash channel, not a nio channel
public interface StorageFileManager extends StorageChannelResetablePart, Disposable
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

	public StorageLiveDataFile currentStorageFile();

	public void iterateStorageFiles(Consumer<? super StorageLiveDataFile> procedure);

	public boolean incrementalFileCleanupCheck(long nanoTimeBudgetBound);

	public boolean issuedFileCleanupCheck(long nanoTimeBudgetBound);

	public void exportData(StorageLiveFileProvider fileProvider);

	public StorageRawFileStatistics.ChannelStatistics createRawFileStatistics();

	// this is not "reset" in terms of "set to initial state", more like a "go back to the start of the chain".
	public void restartFileCleanupCursor();



	public final class Default implements StorageFileManager, StorageFileUser
	{
		private final static Logger logger = Logging.getLogger(Default.class);
		
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
		private final StorageLiveFileProvider              fileProvider                 ;
		private final StorageDataFileEvaluator             dataFileEvaluator            ;
		private final StorageEntityCache.Default           entityCache                  ;
		private final StorageWriteController               writeController              ;
		private final StorageFileWriter                    writer                       ;
		private final StorageBackupHandler                 backupHandler                ;
		
		// to avoid permanent lambda instantiation
		private final Consumer<? super StorageLiveDataFile.Default> deleter        = this::deleteFile       ;
		private final Consumer<? super StorageLiveDataFile.Default> pendingDeleter = this::deletePendingFile;
		
		
		// state 1.1: entry buffers. Don't need to be resetted. See comment in reset().

		// all ".clear()" calls on these buffers are only for flushing them out. Filling them happens only via address.
		private final ByteBuffer
			entryBufferFileCreation   = XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthFileCreation())  ,
			entryBufferStore          = XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthStore())         ,
			entryBufferTransfer       = XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthTransfer())      ,
			entryBufferFileDeletion   = XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthFileCreation())  ,
			entryBufferFileTruncation = XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthFileTruncation())
		;
		
		private final Iterable<? extends ByteBuffer>
			entryBufferWrapFileCreation   = X.ArrayView(this.entryBufferFileCreation  ),
			entryBufferWrapStore          = X.ArrayView(this.entryBufferStore         ),
			entryBufferWrapTransfer       = X.ArrayView(this.entryBufferTransfer      ),
			entryBufferWrapFileDeletion   = X.ArrayView(this.entryBufferFileDeletion  ),
			entryBufferWrapFileTruncation = X.ArrayView(this.entryBufferFileTruncation)
		;

		private final long
			entryBufferFileCreationAddress   = XMemory.getDirectByteBufferAddress(this.entryBufferFileCreation)  ,
			entryBufferStoreAddress          = XMemory.getDirectByteBufferAddress(this.entryBufferStore)         ,
			entryBufferTransferAddress       = XMemory.getDirectByteBufferAddress(this.entryBufferTransfer)      ,
			entryBufferFileDeletionAddress   = XMemory.getDirectByteBufferAddress(this.entryBufferFileDeletion)  ,
			entryBufferFileTruncationAddress = XMemory.getDirectByteBufferAddress(this.entryBufferFileTruncation)
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

		private StorageTransactionsFileCleaner.Default transactionFileCleaner;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final int                                  channelIndex                 ,
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider,
			final StorageTimestampProvider             timestampProvider            ,
			final StorageLiveFileProvider              fileProvider                 ,
			final StorageDataFileEvaluator             dataFileEvaluator            ,
			final StorageEntityCache.Default           entityCache                  ,
			final StorageWriteController               writeController              ,
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
			this.fileProvider                  =     notNull(fileProvider)                 ;
			this.entityCache                   =     notNull(entityCache)                  ;
			this.writeController               =     notNull(writeController)              ;
			this.writer                        =     notNull(writer)                       ;
			this.backupHandler                 =     mayNull(backupHandler)                ;
			
			this.standardByteBuffer = XMemory.allocateDirectNative(
				standardBufferSizeProvider.provideBufferSize()
			);
			
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void dispose()
		{
			this.clearRegisteredFiles();
			this.deleteBuffers();
		}

		final boolean isFileCleanupEnabled()
		{
			return this.writeController.isFileCleanupEnabled();
		}

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

				// set new file. Enqueuing in the file's item chain is done for the whole sub chain
				current.typeInFile      = headFile.typeInFile(current.typeInFile.type);
								
				// update position to the one in the target file (old length plus current copy length)
				current.storagePosition = XTypes.to_int(targetFileOldTotalLength + copyLength);

				// advance to next entity and add current entity's length to the total copy length
				copyLength += current.length;
				current = (last = current).fileNext;
			}
			while(current.storagePosition == copyStart + copyLength);

			// can only reach here if there is at least one entity to transfer

			// update source file to keep consistency as it might not be cleared completely
			sourceFile.removeHeadBoundChain(current, copyLength);

			// update target file's content length. Must be done here as next transfer depends on updated length
			headFile.addChainToTail(first, last);

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

			final StorageLiveDataFile.Default headFile = this.headFile;

			// do the actual file-level copying in one go at the end and validate the byte count to be sure
			this.writer.writeTransfer(sourceFile, copyStart, copyLength, headFile);

			// increase content length by length of chain
			// (15.02.2019 TM)NOTE: changed from arithmetic inside #addChainToTail to directly using copyLength in here.
			headFile.increaseContentLength(copyLength);

			final long newHeadFileLength = headFile.totalLength();
			final long timestamp         = this.timestampProvider.currentNanoTimestamp();
			this.writeTransactionsEntryTransfer(sourceFile, copyStart, copyLength, timestamp, newHeadFileLength);
			
			/*
			 * Note:
			 * It can happen that a transfer is written completely but the process terminates right before the
			 * transactions entry for it is written.
			 * This causes the next initialization to truncate a perfectly fine and complete transfer chunk
			 * because it cannot find the transactions entry validating that chunk.
			 * However, this is not a problem but happens by design. Data can never be lost by this behavior:
			 * If the process terminates before the entry write can be executed, there can also be no subsequent
			 * file cleanup that deletes the old data file. Hence, the transferred data still exists within it
			 * and gets registered as live data on the next initialization (and probably gets transferred then
			 * once again).
			 */
		}
	
		final StorageLiveDataFile.Default createLiveDataFile(
			final AFile file        ,
			final int   channelIndex,
			final long  number
		)
		{
			return new StorageLiveDataFile.Default(
				            this         ,
				    notNull(file)        ,
				notNegative(channelIndex),
				notNegative(number)
			);
		}

		private void createNewStorageFile(final long fileNumber)
		{

			final AFile file = this.fileProvider.provideDataFile(
				this.channelIndex(),
				fileNumber
			);
			file.ensureExists();

			/*
			 * File#length is incredibly slow compared to FileChannel#size (although irrelevant here),
			 * but still the file length has to be checked before the channel is created, etc.
			 */
			if(!file.isEmpty())
			{
				throw new StorageExceptionIoWriting("New storage file is not empty: " + file);
			}

			// create and register StorageFile instance with an attached channel
			final StorageLiveDataFile.Default dataFile = this.createLiveDataFile(file, this.channelIndex(), fileNumber);
			this.registerStorageHeadFile(dataFile);
			this.writeTransactionsEntryFileCreation(0, this.timestampProvider.currentNanoTimestamp(), fileNumber);
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
		public void iterateStorageFiles(final Consumer<? super StorageLiveDataFile> procedure)
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
			final long physicalLength = this.headFile.size();
			final long expectedLength = this.headFile.totalLength();
			
			if(physicalLength != expectedLength)
			{
				throw new StorageExceptionIoWriting(
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
			final long   writeCount       = this.writer.writeStore(this.headFile, X.ArrayView(dataBuffers));
			final long   newTotalLength   = oldTotalLength + writeCount;
			
			if(newTotalLength != this.headFile.size())
			{
				throwImpossibleStoreLengthException(timestamp, oldTotalLength, writeCount, dataBuffers);
			}
			
			this.uncommittedDataLength = writeCount;
			
			this.writeTransactionsEntryStore(this.headFile, oldTotalLength, writeCount, timestamp, newTotalLength);

			this.restartFileCleanupCursor();

			return storagePositions;
		}

		@Override
		public final void rollbackWrite()
		{
			this.writer.truncate(this.headFile, this.headFile.totalLength(), this.fileProvider);
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
		
		final void loadData(
			final StorageLiveDataFile.Default dataFile   ,
			final StorageEntity.Default       entity     ,
			final long                        length     ,
			final long                        cacheChange
		)
		{
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

		@Override
		public final StorageInventory readStorage()
		{
			if(this.headFile != null)
			{
				throw new StorageExceptionIoReading(this.channelIndex() + " already initialized");
			}

			final StorageTransactionsAnalysis      transactionsAnalysis = this.readTransactionsFile();
			final EqHashTable<Long, StorageDataInventoryFile> dataFiles = EqHashTable.New();
			this.fileProvider.collectDataFiles(
				StorageDataInventoryFile::New,
				f ->
					dataFiles.add(f.number(), f),
				this.channelIndex()
			);
			dataFiles.keys().sort(XSort::compare);

			return StorageInventory.New(this.channelIndex(), dataFiles, transactionsAnalysis);
		}

		final StorageTransactionsAnalysis readTransactionsFile()
		{
			final StorageLiveTransactionsFile file = this.createTransactionsFile();

			if(!file.exists())
			{
				/* (11.09.2014 TM)TODO: missing transactions file handler function
				 * default implementation just returns null.
				 * Also see TO-DO for derive function.
				 */
				return null;
			}

			try
			{
				final EntryAggregator aggregator = file.processBy(new EntryAggregator(this.channelIndex()));
				return aggregator.yield(file);
			}
			catch(final Exception e)
			{
				StorageClosableFile.close(file, e);
				throw new StorageException(e);
			}
		}

		private long validateStorageDataFilesLength(
			final StorageInventory                            storageInventory             ,
			final EqHashTable<Long, StorageDataInventoryFile> supplementedMissingEmptyFiles
		)
		{
			final StorageTransactionsAnalysis tFileAnalysis = storageInventory.transactionsFileAnalysis();
			long unregisteredEmptyLastFileNumber = -1; // -1 for "none"

			if(tFileAnalysis == null || tFileAnalysis.transactionsFileEntries().isEmpty())
			{
				// no transaction file (content) present. Abort and derive later.
				// (06.09.2014 TM)TODO: configurable MissingTransactionsFileHandler callback
				return unregisteredEmptyLastFileNumber;
			}

			final XGettingSequence<StorageDataInventoryFile> dataFiles   = storageInventory.dataFiles().values();
			final EqHashTable<Long, StorageTransactionEntry> fileEntries = EqHashTable.New(tFileAnalysis.transactionsFileEntries());
			final StorageDataInventoryFile                   lastFile    = dataFiles.peek();

			for(final StorageDataInventoryFile file : dataFiles)
			{
				final long actualFileLength = file.size();

				// retrieve and remove (= mark as already handled) the corresponding file entry
				final StorageTransactionEntry entryFile = fileEntries.removeFor(file.number());
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
					);
				}

				/* (18.06.2015 TM)TODO: handle files registered as deleted but not deleted yet
				 * files that were registered as deleted but still linger around can/must be
				 * safely deleted and removed from the dataFiles collection.
				 */

				// compare file lengths (head file special case: can be valid if longer, i.e. uncommitted write)
				if(entryFile.length() == actualFileLength || file == lastFile && entryFile.length() < actualFileLength)
				{
					// actual file length is valid
					continue;
				}

				// inconsistent file length compared to transactions file, throw exception
				throw new StorageExceptionConsistency(
					this.channelIndex() + " Length " + actualFileLength + " of file "
					+ file.number() + " is inconsistent with the transactions entry's length of " + entryFile.length()
				);
			}
			
			// check that all remaining file entries are deleted files. No non-deleted file may be missing!
			for(final StorageTransactionEntry remainingFileEntry : fileEntries.values())
			{
				if(remainingFileEntry.isDeleted())
				{
					continue;
				}
				
				if(remainingFileEntry.isEmpty())
				{
					this.supplementedMissingEmptyFile(
						supplementedMissingEmptyFiles,
						remainingFileEntry.fileNumber()
					);
					continue;
				}

				throw new StorageException(
					"Non-deleted non-empty data file not found: channel " + this.channelIndex()
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
		
		protected void supplementedMissingEmptyFile(
			final EqHashTable<Long, StorageDataInventoryFile> supplementedMissingEmptyFiles,
			final long                                        fileNumber
		)
		{
			final AFile missingEmptyFile = this.fileProvider.provideDataFile(
				this.channelIndex,
				fileNumber
			);
			missingEmptyFile.ensureExists();
			final StorageDataInventoryFile supplementedDataFile = StorageDataInventoryFile.New(
				missingEmptyFile, this.channelIndex, fileNumber
			);
			supplementedMissingEmptyFiles.add(fileNumber, supplementedDataFile);
		}

		@Override
		public StorageIdAnalysis initializeStorage(
			final long             taskTimestamp           ,
			final long             consistentStoreTimestamp,
			final StorageInventory storageInventory        ,
			final StorageChannel   parent
		)
		{

			final EqHashTable<Long, StorageDataInventoryFile> supplementedMissingEmptyFiles = EqHashTable.New();
			
			// validate file lengths, even in case of no files, to validate transactions entries to that state
			final long unregisteredEmptyLastFileNumber = this.validateStorageDataFilesLength(
				storageInventory,
				supplementedMissingEmptyFiles
			);
			
			final StorageInventory effectiveStorageInventory = this.determineEffectiveStorageInventory(
				storageInventory,
				supplementedMissingEmptyFiles
			);

			boolean isEmpty = true;
			try
			{
				isEmpty = effectiveStorageInventory.dataFiles().isEmpty();

				final StorageIdAnalysis idAnalysis;
				if(isEmpty)
				{
					// initialize if there are no files at all (create first file, ensure transactions file)
					this.initializeForNoFiles(taskTimestamp, effectiveStorageInventory);
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
						effectiveStorageInventory      ,
						consistentStoreTimestamp       ,
						unregisteredEmptyLastFileNumber
					);
					
					// initialization plus synchronization with existing files.
					this.initializeBackupHandler(effectiveStorageInventory);
				}

				this.transactionFileCleaner = new StorageTransactionsFileCleaner.Default(
					this.fileTransactions,
					this.channelIndex,
					this.dataFileEvaluator.transactionFileMaximumSize(),
					this.fileProvider,
					this.writer
				);
			
				this.restartFileCleanupCursor();
				
				return idAnalysis;
			}
			catch(final RuntimeException e)
			{
				//as this instance won't be restarted any more, destroy allocated buffers
				parent.dispose();
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
		
		protected StorageInventory determineEffectiveStorageInventory(
			final StorageInventory                            storageInventory             ,
			final EqHashTable<Long, StorageDataInventoryFile> supplementedMissingEmptyFiles
		)
		{
			if(supplementedMissingEmptyFiles.isEmpty())
			{
				return storageInventory;
			}
			
			final EqHashTable<Long, StorageDataInventoryFile> completeDataFiles = EqHashTable.New(
				storageInventory.dataFiles()
			)
			.addAll(supplementedMissingEmptyFiles)
			;

			completeDataFiles.keys().sort(XSort::compare);
			
			return StorageInventory.New(
				storageInventory.channelIndex(),
				completeDataFiles.immure(),
				storageInventory.transactionsFileAnalysis()
			);
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
			 * occurrence counts and defines type, length and position in the storage. All further occurrences
			 * (meaning EARLIER versions) of an already encountered Entity/OID are simply ignored.
			 */
			
			// local variables for readability, debugging and (paranoid) consistency guarantee
			final XGettingSequence<StorageDataInventoryFile> files = storageInventory.dataFiles().values();

			// validate and determine length of last file before any file is processed to recognize errors early
			final long lastFileLength = unregisteredEmptyLastFileNumber >= 0
				? 0
				: this.determineLastFileLength(consistentStoreTimestamp, storageInventory)
			;

			// register items (gaps and entities, with latest version of each entity replacing all previous)
			final StorageEntityInitializer<StorageLiveDataFile.Default> initializer =
				StorageEntityInitializer.New(this.entityCache, f ->
					StorageLiveDataFile.New(this, f)
				)
			;
			this.headFile = initializer.registerEntities(files, lastFileLength);

			// validate entities (only the latest versions) before potential transaction file derivation
			final StorageIdAnalysis idAnalysis = this.entityCache.validateEntities();

			// ensure transactions file before handling last file as truncation needs to write in it
			this.ensureTransactionsFile(taskTimestamp, storageInventory, unregisteredEmptyLastFileNumber);

			// special-case handle the last file
			this.handleLastFile(this.headFile, lastFileLength);

			// check if last file is over-sized and should be retired right away.
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
				return storageInventory.dataFiles().values().last().size();
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
				throw new StorageExceptionConsistency(
					"Inconsistent last timestamps in last file of channel " + this.channelIndex()
				);
			}
		}
								
		private void initializeForNoFiles(final long taskTimestamp, final StorageInventory storageInventory)
		{
			// ensure translations file BEFORE adding the first file as it writes a transactions entry
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
			final StorageLiveTransactionsFile transactionsFile;

			if(trFileAn == null || trFileAn.isEmpty())
			{
				// get or create new
				transactionsFile = trFileAn == null
					? this.createTransactionsFile()
					: trFileAn.transactionsFile()
				;

				// validate length of both cases anyway. 0-length is essential before deriving content
				if(transactionsFile.size() != 0)
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

		private StorageLiveTransactionsFile createTransactionsFile()
		{
			final AFile file = this.fileProvider.provideTransactionsFile(this.channelIndex());
			file.ensureExists();
			
			return StorageLiveTransactionsFile.New(file, this.channelIndex());
		}

		private void deriveTransactionsFile(
			final long                        taskTimestamp   ,
			final StorageInventory            storageInventory,
			final StorageLiveTransactionsFile tfile
		)
		{
			final XGettingSequence<StorageDataInventoryFile> files   = storageInventory.dataFiles().values();
			final ByteBuffer                                 buffer  = this.entryBufferFileCreation         ;
			final long                                       address = this.entryBufferFileCreationAddress  ;
			final StorageFileWriter                          writer  = this.writer                          ;

			long timestamp = taskTimestamp - storageInventory.dataFiles().size() - 1;

			try
			{
				for(final StorageDataInventoryFile file : files)
				{
					buffer.clear();
					StorageTransactionsAnalysis.Logic.setEntryFileCreation(
						address      ,
						file.size()  ,
						++timestamp  ,
						file.number()
					);
					writer.write(tfile, this.entryBufferWrapFileCreation);
				}
			}
			catch(final Exception e)
			{
				StorageClosableFile.close(tfile, e);
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
			StorageTransactionsAnalysis.Logic.setEntryFileCreation(
				this.entryBufferFileCreationAddress,
				length                             ,
				timestamp                          ,
				number
			);
			this.writer.writeTransactionEntryCreate(this.fileTransactions, this.entryBufferWrapFileCreation, this.headFile);
		}

		private void writeTransactionsEntryStore(
			final StorageLiveDataFile dataFile              ,
			final long                dataFileOffset        ,
			final long                storeLength           ,
			final long                timestamp             ,
			final long                headFileNewTotalLength
		)
		{
			this.entryBufferStore.clear();
			StorageTransactionsAnalysis.Logic.setEntryStore(
				this.entryBufferStoreAddress,
				headFileNewTotalLength      ,
				timestamp
			);
			this.writer.writeTransactionEntryStore(
				this.fileTransactions    ,
				this.entryBufferWrapStore,
				dataFile                 ,
				dataFileOffset           ,
				storeLength
			);
		}

		private void writeTransactionsEntryTransfer(
			final StorageLiveDataFile sourceFile            ,
			final long                sourcefileOffset      ,
			final long                copyLength            ,
			final long                timestamp             ,
			final long                headNewFileTotalLength
		)
		{
			this.entryBufferTransfer.clear();
			StorageTransactionsAnalysis.Logic.setEntryTransfer(
				this.entryBufferTransferAddress,
				headNewFileTotalLength         ,
				timestamp                      ,
				sourceFile.number()            ,
				sourcefileOffset
			);
			
			this.writer.writeTransactionEntryTransfer(
				this.fileTransactions,
				this.entryBufferWrapTransfer,
				sourceFile,
				sourcefileOffset,
				copyLength
			);
		}

		private void writeTransactionsEntryFileDeletion(
			final StorageLiveDataFile.Default dataFile ,
			final long                        timestamp
		)
		{
			this.entryBufferFileDeletion.clear();
			StorageTransactionsAnalysis.Logic.setEntryFileDeletion(
				this.entryBufferFileDeletionAddress,
				dataFile.totalLength()             ,
				timestamp                          ,
				dataFile.number()
			);
			this.writer.writeTransactionEntryDelete(this.fileTransactions, this.entryBufferWrapFileDeletion, dataFile);
		}

		private void writeTransactionsEntryFileTruncation(
			final StorageLiveDataFile.Default lastFile ,
			final long                        timestamp,
			final long                        newLength
		)
		{
			this.entryBufferFileTruncation.clear();
			StorageTransactionsAnalysis.Logic.setEntryFileTruncation(
				this.entryBufferFileTruncationAddress,
				newLength                            ,
				timestamp                            ,
				lastFile.number()                    ,
				lastFile.size()
			);
			this.writer.writeTransactionEntryTruncate(this.fileTransactions, this.entryBufferWrapFileTruncation, lastFile, newLength);
		}

		private void setTransactionsFile(final StorageLiveTransactionsFile transactionsFile)
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
		
		/**
		 * The deleteBuffers method is used to allow an early deallocation
		 * of the used DirectByteBuffers in order to reduce the off-heap
		 * memory footprint without the need to relay on the GC.
		 * after calling this method the StorageManager is left in a inoperable state.
		 */
		public final void deleteBuffers()
		{
			logger.debug("Destroying all buffers explicitly!");

			XMemory.deallocateDirectByteBuffer(this.entryBufferFileCreation);
			XMemory.deallocateDirectByteBuffer(this.entryBufferStore);
			XMemory.deallocateDirectByteBuffer(this.entryBufferTransfer);
			XMemory.deallocateDirectByteBuffer(this.entryBufferFileDeletion);
			XMemory.deallocateDirectByteBuffer(this.entryBufferFileTruncation);
			XMemory.deallocateDirectByteBuffer(this.standardByteBuffer);
		}

		final void handleLastFile(
			final StorageLiveDataFile.Default lastFile      ,
			final long                        lastFileLength
		)
		{
			if(lastFileLength != lastFile.size())
			{
				// reaching here means in any case that the file has to be truncated and its header must be updated

				final long timestamp = this.timestampProvider.currentNanoTimestamp();
				
				// write truncation entry (BEFORE the actual truncate)
				this.writeTransactionsEntryFileTruncation(lastFile, timestamp, lastFileLength);

				// (20.06.2014 TM)TODO: truncator function to give a chance to evaluate / rescue the doomed data
				this.writer.truncate(lastFile, lastFileLength, this.fileProvider);
			}
		}
		
		@Override
		public void exportData(final StorageLiveFileProvider fileProvider)
		{
			final AFile transactionsFile = fileProvider.provideTransactionsFile(this.channelIndex());
			AFS.executeWriting(transactionsFile, wf ->
				this.fileTransactions.copyTo(wf)
			);

			this.iterateStorageFiles(file ->
			{
				final AFile exportFile = fileProvider.provideDataFile(file.channelIndex(), file.number());
				AFS.executeWriting(exportFile, wf ->
					file.copyTo(wf)
				);
			});
		}
		
		private static FileStatistics createFileStatistics(final StorageLiveDataFile.Default file)
		{
			return FileStatistics.New(
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
			final BulkList<FileStatistics> fileStatistics = BulkList.New();

			do
			{
				file = file.next;
				liveDataLength  += file.dataLength();
				totalDataLength += file.totalLength();
				
				final FileStatistics fileStats = createFileStatistics(file);
				fileStatistics.add(fileStats);
			}
			while(file != currentFile);

			return StorageRawFileStatistics.ChannelStatistics.New(
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
		}

		@Override
		public final boolean issuedFileCleanupCheck(final long nanoTimeBudgetBound)
		{
			return this.internalCheckForCleanup(nanoTimeBudgetBound, this.dataFileEvaluator);
		}
		
		public boolean issuedTransactionFileCheck(final boolean checkSize)
		{
			return this.internalTransactionFileCheck(checkSize);
		}

		private void deletePendingFile(final StorageLiveDataFile.Default file)
		{
			if(this.pendingFileDeletes < 1)
			{
				/* (31.10.2014 TM)TODO: Proper storage inconsistency handling
				 *  May never just throw an exception and potentially kill the channel thread
				 *  Instead must signal the storage manager (one way or another) to shutdown so that no other
				 *  thread continues working and ruins something.
				 */
				throw new StorageExceptionConsistency(
					this.channelIndex() + " has inconsistent pending deletes: count = "
					+ this.pendingFileDeletes + ", wants to delete " + file
				);
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
			
			this.writeController.validateIsFileCleanupEnabled();

			if(this.fileCleanupCursor == null)
			{
				return true;
			}


			StorageLiveDataFile.Default cycleAnchorFile = this.fileCleanupCursor;

			// intentionally no minimum first loop execution as cleanup is not important if the system has heavy load
			while(this.fileCleanupCursor != null && System.nanoTime() < nanoTimeBudgetBound)
			{
				// never check current head file for dissolving

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

					if(!this.incrementalDissolveStorageFile(this.fileCleanupCursor, nanoTimeBudgetBound))
					{
						continue;
					}
					// file has been dissolved completely and deleted, do special case checking here as well.

					// account for special case of removed file being the anchor file (sadly redundant to above)
					if(this.fileCleanupCursor == cycleAnchorFile)
					{
						this.fileCleanupCursor = cycleAnchorFile = cycleAnchorFile.next;
						continue;
					}

					/* Reaching here means normal case of advancing the house-keeping file.
					 * Either a healthy file or a removed file that is not the anchor special case.
					 */
				}

				// Advance to next file, abort if full cycle is completed.
				if((this.fileCleanupCursor = this.fileCleanupCursor.next) == cycleAnchorFile)
				{
					// if there are still pending deletes, file house keeping cannot be turned off
					if(this.pendingFileDeletes > 0)
					{

						// at least one more file is pending deletion
						break;
					}


					/* House-keeping can be completely disabled for now as everything has been checked.
					 * Will be resetted by the next write, see #resetHousekeeping.
					 */
					this.fileCleanupCursor = null;
				}
			}

			return this.fileCleanupCursor == null;
		}

		private boolean incrementalDissolveStorageFile(
			final StorageLiveDataFile.Default file               ,
			final long                        nanoTimeBudgetBound
		)
		{

			if(this.incrementalTransferEntities(file, nanoTimeBudgetBound))
			{
				if(file.unregisterUsageClosingData(this, this.deleter))
				{
					return true;
				}


				// file has no more content but can't be deleted yet. Schedule for later deletion.
				this.pendingFileDeletes++;
				return false;
			}

			return false;
		}

		private void deleteFile(final StorageLiveDataFile.Default file)
		{

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

			// (12.08.2020 TM)FIXME: priv#351: where and how to check whether files may be deleted? Here? Weird!
			
			// physically delete file after the transactions entry is ensured
			this.writer.delete(file, this.writeController, this.fileProvider);
		}

		private boolean incrementalTransferEntities(
			final StorageLiveDataFile.Default file               ,
			final long                        nanoTimeBudgetBound
		)
		{
			// check for new head file in any case
			this.checkForNewFile();

			// dissolve file to as much head files as needed.
			while(file.hasContent() && System.nanoTime() < nanoTimeBudgetBound)
			{
				this.transferOneChainToHeadFile(file);
			}


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
					if(file.hasContent())
					{
						return file.head.fileNext;
					}
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
				throw new StorageException(e);
			}
		}

		public void copyData(final StorageImportSource importSource)
		{
			importSource.iterateBatches(this.importHelper.setSource(importSource));
		}

		public void commitImport(final long taskTimestamp)
		{

			// caching variables
			final StorageEntityCache.Default  entityCache = this.entityCache;
			final StorageLiveDataFile.Default headFile    = this.headFile   ;

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

			this.writeTransactionsEntryStore(this.headFile, oldTotalLength, copyLength, taskTimestamp, loopFileLength);
		}

		final void cleanupImportHelper()
		{
			this.importHelper = null;
		}

		final void importBatch(final StorageImportSource source, final long position, final long length)
		{
			// ignore dummy batches (e.g. transfer file continuation head dummy) and no-op batches in general
			if(length == 0)
			{
				return;
			}

			this.checkForNewFile();
			this.writer.writeImport(source, position, length, this.headFile);
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
				throw new StorageException(new MultiCauseException(exceptions.toArray(RuntimeException.class)));
			}
		}
		
		private void terminateFile(final StorageLiveDataFile.Default file)
		{
			// (12.08.2020 TM)FIXME: priv#351: where and how to check whether files may be deleted? Here? Weird!
			file.close();
			this.writer.delete(file, this.writeController, this.fileProvider);
		}

		final class ImportHelper implements Consumer<StorageChannelImportBatch>
		{
			final StorageLiveDataFile.Default         preImportHeadFile;
			final BulkList<StorageChannelImportBatch> importBatches     = BulkList.New(1000);
			      StorageImportSource                 source           ;


			ImportHelper(final StorageLiveDataFile.Default preImportHeadFile)
			{
				super();
				this.preImportHeadFile = preImportHeadFile;
			}

			@Override
			public void accept(final StorageChannelImportBatch batch)
			{
				this.importBatches.add(batch);
				StorageFileManager.Default.this.importBatch(this.source, batch.batchOffset(), batch.batchLength());
			}

			final ImportHelper setSource(final StorageImportSource source)
			{
				this.source = source;
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
		
		private boolean internalTransactionFileCheck(final boolean checkSize)
		{
			if(this.fileTransactions == null)
			{
				return true;
			}
			
			this.transactionFileCleaner.compactTransactionsFile(checkSize);
			
			return true;
		}
		
	}
		
}
