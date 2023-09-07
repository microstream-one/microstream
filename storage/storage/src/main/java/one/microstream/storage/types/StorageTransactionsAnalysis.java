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

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AReadableFile;
import one.microstream.chars.VarString;
import one.microstream.collections.ConstList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.memory.XMemory;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionConsistency;
import one.microstream.storage.exceptions.StorageExceptionIoReading;

public interface StorageTransactionsAnalysis
{
	public long headFileLastConsistentStoreLength();

	public long headFileLastConsistentStoreTimestamp();

	public long headFileLatestLength();

	public long headFileLatestTimestamp();
	
	public long maxTimestamp();

	public StorageLiveTransactionsFile transactionsFile();

	public XGettingTable<Long, ? extends StorageTransactionEntry> transactionsFileEntries();

	public default boolean isEmpty()
	{
		return this.transactionsFileEntries().isEmpty();
	}

	
	public final class Logic
	{
		/*
		 * All entries have the following format:
		 * [1B length][1B type][8B timestamp][8B new file length]{8B target file number}{8B special offset}
		 * with [] fields being mandatory and {} fields being optional.
		 *
		 * examples:
		 * transactions_0.sft
		 *
		 * FileCreation (26 byte)
		 * Length[1]   Type[1]   FileLength[8]   Timestamp[8]          FileNumber[8]
		 * 26          0         0               1404033352111000000   97
		 *
		 * Store (18 byte)
		 * Length[1]   Type[1]   FileLength[8]   Timestamp[8]
		 * 18          1         589874700       1404033352748000000
		 *
		 * Transfer (34 byte)
		 * Length[1]   Type[1]   FileLength[8]   Timestamp[8]          SourceFileNumber[8]   SourceFileOffset[8]
		 * 36          2         889874786       1404033352748000000   45                    46841
		 *
		 * FileTruncation(34 byte)
		 * Length[1]   Type[1]   FileLength[8]   Timestamp[8]          FileNumber[8]         OldFileLength[8]
		 * 26          3         889874000       1404033352111000000   97                    889874786
		 *
		 * FileDeletion (26 byte)
		 * Length[1]   Type[1]   FileLength[8]   Timestamp[8]          FileNumber[8]
		 * 26          3         889874786       1404033352111000000   97
		 *
		 * Gap ( >= 1byte)
		 * -Length[1]  (Content)[arbitrary size of "-Length - 1"]
		 * -24         S.O.M.E .C.O.M.M.E.N.T.
		 */
		static final byte
			LENGTH_ENTRY_LENGTH               = (byte)XMemory.byteSize_byte()                              ,
			LENGTH_ENTRY_TYPE                 = (byte)XMemory.byteSize_byte()                              ,
			LENGTH_ENTRY_TIMESTAMP            = (byte)XMemory.byteSize_long()                              ,
			LENGTH_FILE_LENGTH                = (byte)XMemory.byteSize_long()                              ,
			LENGTH_FILE_NUMBER                = (byte)XMemory.byteSize_long()                              ,

			TYPE_FILE_CREATION                = 0  /* length: 26 | binary header pattern: 1A00 */         ,
			TYPE_STORE                        = 1  /* length: 18 | binary header pattern: 1201 */         ,
			TYPE_TRANSFER                     = 2  /* length: 34 | binary header pattern: 2202 */         ,
			TYPE_FILE_TRUNCATION              = 3  /* length: 34 | binary header pattern: 2203 */         ,
			TYPE_FILE_DELETION                = 4  /* length: 26 | binary header pattern: 1A04 */         ,

			OFFSET_COMMON_LENGTH              = 0                                                         ,
			OFFSET_COMMON_TYPE                = (byte)(OFFSET_COMMON_LENGTH         + LENGTH_ENTRY_LENGTH),
			OFFSET_COMMON_TIMESTAMP           = (byte)(OFFSET_COMMON_TYPE           + LENGTH_ENTRY_TYPE  ),
			OFFSET_COMMON_FILE_LENGTH         = (byte)(OFFSET_COMMON_TIMESTAMP      + LENGTH_FILE_LENGTH ),
			LENGTH_COMMON                     = (byte)(OFFSET_COMMON_FILE_LENGTH    + LENGTH_FILE_LENGTH ),

			OFFSET_COMMON_FILE_NUMBER         = LENGTH_COMMON,
			LENGTH_COMMON_NUMBERED            = (byte)(OFFSET_COMMON_FILE_NUMBER    + LENGTH_FILE_NUMBER ),

			OFFSET_COMMON_SPECIAL_OFFSET      = LENGTH_COMMON_NUMBERED                                    ,
			LENGTH_COMMON_MAXIMUM             = (byte)(OFFSET_COMMON_SPECIAL_OFFSET + LENGTH_FILE_LENGTH ),

			LENGTH_FILE_CREATION              = LENGTH_COMMON_NUMBERED                                    ,
			LENGTH_STORE                      = LENGTH_COMMON                                             ,
			LENGTH_TRANSFER                   = LENGTH_COMMON_MAXIMUM                                     ,
			LENGTH_FILE_TRUNCATION            = LENGTH_COMMON_MAXIMUM                                     ,
			LENGTH_FILE_DELETION              = LENGTH_COMMON_NUMBERED
		;

		public static byte entryLengthFileCreation()
		{
			return LENGTH_FILE_CREATION;
		}

		public static byte entryLengthStore()
		{
			return LENGTH_STORE;
		}

		public static byte entryLengthTransfer()
		{
			return LENGTH_TRANSFER;
		}

		public static byte entryLengthFileTruncation()
		{
			return LENGTH_FILE_TRUNCATION;
		}

		public static byte entryLengthFileDeletion()
		{
			return LENGTH_FILE_DELETION;
		}

		public static void initializeEntry(final long address, final byte length, final byte type)
		{
			XMemory.set_byte(address + OFFSET_COMMON_LENGTH, length);
			XMemory.set_byte(address + OFFSET_COMMON_TYPE  , type  );
		}

		public static void initializeEntryFileCreation(final long address)
		{
			initializeEntry(address, entryLengthFileCreation(), TYPE_FILE_CREATION);
		}

		public static void initializeEntryStore(final long address)
		{
			initializeEntry(address, entryLengthStore(), TYPE_STORE);
		}

		public static void initializeEntryTransfer(final long address)
		{
			initializeEntry(address, entryLengthTransfer(), TYPE_TRANSFER);
		}

		public static void initializeEntryFileDeletion(final long address)
		{
			initializeEntry(address, entryLengthFileDeletion(), TYPE_FILE_DELETION);
		}

		public static void initializeEntryFileTruncation(final long address)
		{
			initializeEntry(address, entryLengthFileTruncation(), TYPE_FILE_TRUNCATION);
		}

		public static void setEntryCommon(final long address, final long fileLength, final long timestamp)
		{
			XMemory.set_long(address + OFFSET_COMMON_FILE_LENGTH, fileLength);
			XMemory.set_long(address + OFFSET_COMMON_TIMESTAMP  , timestamp );
		}

		public static void setEntryFileCreation(
			final long address   ,
			final long fileLength,
			final long timestamp ,
			final long fileNumber
		)
		{
			setEntryCommon(address, fileLength, timestamp);
			XMemory.set_long(address + OFFSET_COMMON_FILE_NUMBER, fileNumber);
		}

		public static byte getEntryLength(final long address)
		{
			return XMemory.get_byte(address + OFFSET_COMMON_LENGTH);
		}
		
		public static final StorageTransactionsEntryType mapEntryType(final byte entryTypeKey)
		{
			switch(entryTypeKey)
			{
				case Logic.TYPE_FILE_CREATION  : return StorageTransactionsEntryType.FILE_CREATION  ;
				case Logic.TYPE_STORE          : return StorageTransactionsEntryType.DATA_STORE     ;
				case Logic.TYPE_TRANSFER       : return StorageTransactionsEntryType.DATA_TRANSFER  ;
				case Logic.TYPE_FILE_TRUNCATION: return StorageTransactionsEntryType.FILE_TRUNCATION;
				case Logic.TYPE_FILE_DELETION  : return StorageTransactionsEntryType.FILE_DELETION  ;
				default:
				{
					throw new StorageException("Unknown transactions entry type: " + entryTypeKey);
				}
			}
		}

		public static byte getEntryType(final long address)
		{
			return XMemory.get_byte(address + OFFSET_COMMON_TYPE);
		}
		
		public static StorageTransactionsEntryType resolveEntryType(final long address)
		{
			return mapEntryType(getEntryType(address));
		}

		public static long getEntryTimestamp(final long address)
		{
			return XMemory.get_long(address + OFFSET_COMMON_TIMESTAMP);
		}

		public static long getFileLength(final long address)
		{
			return XMemory.get_long(address + OFFSET_COMMON_FILE_LENGTH);
		}

		public static long getFileNumber(final long address)
		{
			return XMemory.get_long(address + OFFSET_COMMON_FILE_NUMBER);
		}

		public static long getSpecialOffset(final long address)
		{
			return XMemory.get_long(address + OFFSET_COMMON_SPECIAL_OFFSET);
		}

		public static void setEntryStore(final long address, final long fileLength, final long timestamp)
		{
			setEntryCommon(address, fileLength, timestamp);
		}

		public static void setEntryTransfer(
			final long address         ,
			final long fileLength      ,
			final long timestamp       ,
			final long sourcefileNumber,
			final long sourcefileOffset
		)
		{
			setEntryCommon(address, fileLength, timestamp);
			XMemory.set_long(address + OFFSET_COMMON_FILE_NUMBER   , sourcefileNumber );
			XMemory.set_long(address + OFFSET_COMMON_SPECIAL_OFFSET, sourcefileOffset );
		}

		public static void setEntryFileDeletion(
			final long address   ,
			final long fileLength,
			final long timestamp ,
			final long fileNumber
		)
		{
			setEntryCommon(address, fileLength, timestamp);
			XMemory.set_long(address + OFFSET_COMMON_FILE_NUMBER, fileNumber);
		}

		public static void setEntryFileTruncation(
			final long address   ,
			final long fileLength,
			final long timestamp ,
			final long fileNumber,
			final long oldLength
		)
		{
			setEntryCommon(address, fileLength, timestamp);
			XMemory.set_long(address + OFFSET_COMMON_FILE_NUMBER   , fileNumber);
			XMemory.set_long(address + OFFSET_COMMON_SPECIAL_OFFSET, oldLength );
		}

		public static <P extends EntryIterator> P processInputFile(
			final AReadableFile file          ,
			final P             entryProcessor
		)
		{
			return processInputFile(file, 0, file.size(), entryProcessor);
		}

		public static <P extends EntryIterator> P processInputFile(
			final AReadableFile file          ,
			final long          startPosition ,
			final long          length        ,
			final P             entryProcessor
		)
		{
			final long actualFileLength    = file.size()    ;
			final long boundPosition       = startPosition + length;
			      long currentFilePosition = startPosition         ;

			if(currentFilePosition < 0 || currentFilePosition > actualFileLength)
			{
				throw new IndexBoundsException(actualFileLength, currentFilePosition);
			}
			if(boundPosition < 0 || boundPosition > actualFileLength)
			{
				throw new IndexBoundsException(actualFileLength, boundPosition);
			}

			final ByteBuffer buffer  = XMemory.allocateDirectNativeDefault();
			final long       address = XMemory.getDirectByteBufferAddress(buffer);

			// process whole file part by part
			while(currentFilePosition < boundPosition)
			{
				buffer.clear();

				// end of file special case: adjust buffer limit if buffer would exceed the bounds
				if(currentFilePosition + buffer.limit() >= boundPosition)
				{
					// cast (value range) safety is guaranteed by if above
					buffer.limit((int)(boundPosition - currentFilePosition));
				}

				// loop is guaranteed to terminate as it depends on the buffer capacity and the file length
				file.readBytes(buffer, currentFilePosition);

				// buffer is guaranteed to be filled exactely to its limit in any case
				final long progress = processBufferedEntities(address, buffer.limit(), entryProcessor);
				currentFilePosition += progress;
			}

			return entryProcessor;
		}

		private static long processBufferedEntities(
			final long          startAddress    ,
			final long          bufferDataLength,
			final EntryIterator entityProcessor
		)
		{
			// bufferBound is the bounding address (exclusive) of the data available in the buffer
			final long bufferBound = startAddress + bufferDataLength;

			// every entity start must be at least one long size before the actual bound to safely read its length
			final long entityStartBound = bufferBound - LENGTH_ENTRY_LENGTH;

			// iteration variable, initialized with the data start address
			long address = startAddress;

			// total byte length of the current entity. Invalid initial value, must be replaced in any case
			int entryLength = 0;

			// iterate over and process every complete entity record, skip all gaps, revert trailing complete entity
			while(true) // loop gets terminated by end-of-data recognition logic specific to the found case
			{
				// read length of current entry (actual or gap)
				entryLength = getEntryLength(address); // implicit cast!

				if(entryLength == 0)
				{
					// entity length may never be 0 or the iteration will hang forever
					throw new StorageException("Zero length transactions entry.");
				}

				// depending on the processor logic, incomplete entity data can still be enough (e.g. only needs header)
				if(!entityProcessor.accept(address, bufferBound - address))
				{
					// only advance to start of current incomplete entity
					return address - startAddress;
				}

				// advance iteration and check for end of current buffered data
				if((address += Math.abs(entryLength)) > entityStartBound)
				{
					// only advance to start of next item (of currently unknowable length)
					return address - startAddress;
				}
			}
		}

		public static VarString parseFile(final AFile file)
		{
			return parseFile(file, VarString.New());
		}

		public static VarString parseFile(final AFile file, final VarString vs)
		{
			Throwable suppressed = null;
			final AReadableFile rFile = file.useReading();
			try
			{
				if(!rFile.exists())
				{
					return vs;
				}
				
				return parseFile(rFile, vs);
			}
			catch(final IOException e)
			{
				suppressed = e;
				throw new StorageExceptionIoReading(e);
			}
			finally
			{
				AFS.close(rFile, suppressed);
			}
		}

		public static VarString parseFile(final AReadableFile file, final VarString vs) throws IOException
		{
			StorageTransactionsAnalysis.Logic.processInputFile(
				file,
				new EntryAssembler(vs)
			);
			
			return vs;
		}

		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 * 
		 * @throws UnsupportedOperationException when called
		 */
		private Logic()
		{
			// static only
			throw new UnsupportedOperationException();
		}
	}

	@FunctionalInterface
	public interface EntryIterator
	{
		public boolean accept(long address, long availableEntryLength);
	}


	public final class EntryAssembler implements EntryIterator
	{
		private static String formateTimeStamp(final Date timestamp)
		{
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(timestamp);
		}

		public static XGettingSequence<String> header()
		{
			return ConstList.New(
				"Type"             ,
				"Timestamp"        ,
				"Time Delta (ms)"  ,
				"Timestamp(long)"  ,
				"Resulting Length" ,
				"Length Change"    ,
				"Current Head File",
				"Significant File" ,
				"Special Offset"
			);
		}

		public static VarString assembleHeader(final VarString vs, final String separator)
		{
			notNull(separator);
			for(final String s : header())
			{
				vs.add(s).add(separator);
			}
			vs.deleteLast(separator.length());
			return vs;
		}

		private final VarString vs;

		private long currentHeadFileNumber;
		private long lastFileLength       ;
		private long lastTimestamp        ;


		public EntryAssembler(final VarString vs)
		{
			super();
			this.vs = vs;
		}

		public VarString content()
		{
			return this.vs;
		}

		@Override
		public boolean accept(final long address, final long availableEntryLength)
		{
			// check for and skip gaps / comments
			if(availableEntryLength < 0)
			{
				return this.assembleGap(address, -availableEntryLength);
			}

			switch(Logic.getEntryType(address))
			{
				case Logic.TYPE_FILE_CREATION  : return this.assembleEntryFileCreation  (address, availableEntryLength);
				case Logic.TYPE_STORE          : return this.assembleEntryStore         (address, availableEntryLength);
				case Logic.TYPE_TRANSFER       : return this.assembleEntryTransfer      (address, availableEntryLength);
				case Logic.TYPE_FILE_TRUNCATION: return this.assembleEntryFileTruncation(address, availableEntryLength);
				case Logic.TYPE_FILE_DELETION  : return this.assembleEntryFileDeletion  (address, availableEntryLength);
				default:
				{
					throw new StorageException("Unknown transactions entry type: " + Logic.getEntryType(address));
				}
			}
		}

		private boolean assembleGap(final long address, final long availableItemLength)
		{
			// 2 bytes per char are required
			final char[] array = new char[(int)availableItemLength >> 1];
			XMemory.copyRangeToArray(address, array);
			this.vs.add(array);
			return true;
		}

		private void addCommonTimestampPart(final long address)
		{
			final long timestamp = Logic.getEntryTimestamp(address);

			this.vs
			.add(formateTimeStamp(new Date(Storage.millisecondsToSeconds(timestamp)))).tab()
			.add(Storage.millisecondsToSeconds(timestamp - this.lastTimestamp)).tab()
			.add(timestamp).tab()
			;

			this.lastTimestamp = timestamp;
		}

		private void addCommonFileLengthDifference(final long address)
		{
			this.vs
			.add(Logic.getFileLength(address)).tab()
			.add(Logic.getFileLength(address) - this.lastFileLength).tab()
			;
			this.lastFileLength = Logic.getFileLength(address);
		}

		private void addCommonCurrentHeadFile()
		{
			this.vs.add(this.currentHeadFileNumber).tab();
		}


		private boolean assembleEntryFileCreation(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_FILE_CREATION)
			{
				return false;
			}

			// reset as it is no longer valid for a new file (would mess up common part calculation otherwise)
			this.lastFileLength = 0;

			this.vs.add(StorageTransactionsEntryType.FILE_CREATION.typeName()).tab();
			this.addCommonTimestampPart(address);
			this.addCommonFileLengthDifference(address);
			this.addCommonCurrentHeadFile();
			this.vs.add(this.currentHeadFileNumber = Logic.getFileNumber(address)).lf();

			return true;
		}

		private boolean assembleEntryStore(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_STORE)
			{
				return false;
			}
			
			this.vs
			.add(StorageTransactionsEntryType.DATA_STORE.typeName()).tab();
			this.addCommonTimestampPart(address);
			this.addCommonFileLengthDifference(address);
			this.addCommonCurrentHeadFile();
			this.vs.deleteLast().lf();
			// store only consists of type and common part

			return true;
		}

		private boolean assembleEntryTransfer(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_TRANSFER)
			{
				return false;
			}


			this.vs
			.add(StorageTransactionsEntryType.DATA_TRANSFER.typeName()).tab();
			this.addCommonTimestampPart(address);
			this.addCommonFileLengthDifference(address);
			this.addCommonCurrentHeadFile();
			this.vs
			.add(Logic.getFileNumber(address)).tab()
			.add(Logic.getSpecialOffset(address)).lf()
			;
			return true;
		}

		private boolean assembleEntryFileTruncation(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_FILE_TRUNCATION)
			{
				return false;
			}
			this.vs
			.add(StorageTransactionsEntryType.FILE_TRUNCATION.typeName()).tab();
			this.addCommonTimestampPart(address);
			this.addCommonFileLengthDifference(address);
			this.addCommonCurrentHeadFile();
			this.vs
			.add(Logic.getFileNumber(address)).tab()
			.add(Logic.getSpecialOffset(address)).lf()
			;
			return true;
		}

		private boolean assembleEntryFileDeletion(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_FILE_DELETION)
			{
				return false;
			}

			this.vs
			.add(StorageTransactionsEntryType.FILE_DELETION.typeName()).tab();
			this.addCommonTimestampPart(address);
			this.vs
			.add('0').tab()
			.add(-Logic.getFileLength(address)).tab()
			;
			this.addCommonCurrentHeadFile();
			this.vs.add(Logic.getFileNumber(address)).lf();

			return true;
		}

	}

	
	
	public final class EntryAggregator implements EntryIterator
	{
		private final EqHashTable<Long, StorageTransactionEntry.Default> files = EqHashTable.New();

		private final int  hashIndex;

		private long lastConsistentStoreLength   ;
		private long lastConsistentStoreTimestamp;
		private long currentStoreLength          ;
		private long currentStoreTimestamp       ;
		private long maxTimeStamp;
		
		private long currentFileNumber            = -1;



		public EntryAggregator(final int hashIndex)
		{
			super();
			this.hashIndex = hashIndex;
		}

		@Override
		public boolean accept(final long address, final long availableItemLength)
		{
			// check for and skip gaps / comments
			if(availableItemLength < 0)
			{
				return true;
			}

			switch(Logic.getEntryType(address))
			{
				case Logic.TYPE_FILE_CREATION  : return this.handleEntryFileCreation  (address, availableItemLength);
				case Logic.TYPE_STORE          : return this.handleEntryStore         (address, availableItemLength);
				case Logic.TYPE_TRANSFER       : return this.handleEntryTransfer      (address, availableItemLength);
				case Logic.TYPE_FILE_TRUNCATION: return this.handleEntryFileTruncation(address, availableItemLength);
				case Logic.TYPE_FILE_DELETION  : return this.handleEntryFileDeletion  (address, availableItemLength);
				default:
				{
					throw new StorageException("Unknown transactions entry type: " + Logic.getEntryType(address));
				}
			}
		}

		private boolean handleEntryFileCreation(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_FILE_CREATION)
			{
				return false;
			}

			final long number = Logic.getFileNumber(address);
			if(number <= this.currentFileNumber)
			{
				throw new StorageExceptionConsistency(
					this.hashIndex + " Inconsistent file number order of new file: "
					+ number + " <= " + this.currentFileNumber
				);
			}

			final long fileLength = Logic.getFileLength(address);
			if(fileLength < 0)
			{
				throw new StorageExceptionConsistency(
					this.hashIndex + " Inconsistent file length of new file " + number + ": " + fileLength
				);
			}

			// timestamp is intentionally ignored as file creation happens AFTER a store has been issued.
			this.updateMaxTimestamp(Logic.getEntryTimestamp(address));

			// entry is consistent, register completed file and reset values for new file.
			this.registerCurrentFile();

			/*
			 * must ensure that the new file's length is associated to the latest store timestamp,
			 * even if it is another file.
			 */
			this.lastConsistentStoreTimestamp = this.currentStoreTimestamp;
			this.lastConsistentStoreLength    = this.currentStoreLength    = fileLength;
			this.currentFileNumber            = number;

			return true;
		}

		private void registerCurrentFile()
		{
			if(this.currentFileNumber < 0)
			{
				return;
			}
			this.files.add(
				this.currentFileNumber,
				new StorageTransactionEntry.Default(this.currentFileNumber, this.currentStoreLength)
			);
		}

		private boolean handleEntryStore(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_STORE)
			{
				return false;
			}

			final long fileLength = Logic.getFileLength(address);
			if(fileLength < this.currentStoreLength)
			{
				throw new StorageExceptionConsistency(
					this.hashIndex + " Inconsistent file length of file " + this.currentFileNumber + ": "
					+ fileLength + " < " + this.currentStoreLength
				);
			}

			final long timestamp = Logic.getEntryTimestamp(address);
			this.updateMaxTimestamp(timestamp);
			if(timestamp <= this.currentStoreTimestamp)
			{
				throw new StorageExceptionConsistency(
					this.hashIndex + " Inconsistent timestamp of file " + this.currentFileNumber + ": "
					+ timestamp + " <= " + this.currentStoreTimestamp
				);
			}

			this.lastConsistentStoreTimestamp = this.currentStoreTimestamp;
			this.lastConsistentStoreLength    = this.currentStoreLength;
			this.currentStoreTimestamp        = timestamp;
			this.currentStoreLength           = fileLength;
			return true;
		}

		private boolean handleEntryTransfer(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_TRANSFER)
			{
				return false;
			}

			final long fileLength = Logic.getFileLength(address);
			if(fileLength < this.currentStoreLength)
			{
				throw new StorageExceptionConsistency(
					this.hashIndex + " Inconsistent file length of file " + this.currentFileNumber + ": "
					+ fileLength + " < " + this.currentStoreLength
				);
			}

			this.updateMaxTimestamp(Logic.getEntryTimestamp(address));
			
			/* lastConsistentStoreTimestamp is not updated to associate the new file length with the old timestamp
			 * i.e. when an inter-channel rollback has to occur, the transfer part is not rolled back, as it is
			 * channel-local
			 */
			this.lastConsistentStoreLength = this.currentStoreLength = fileLength;
			/* currentStoreTimestamp is NOT updated as a transfer is channel-local, therefore won't affect
			 * store consistency anyway and can happen after the store has been issued but before it is processed.
			 */
			return true;
		}

		private boolean handleEntryFileTruncation(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_FILE_TRUNCATION)
			{
				return false;
			}

			final long fileNumber = Logic.getFileNumber(address);
			if(fileNumber != this.currentFileNumber)
			{
				throw new StorageException(
					this.hashIndex + " Invalid truncation file number: "
					+ fileNumber + " (file " + this.currentFileNumber + ")"
				);
			}

			/* Not on old length:
			 * Old length cannot be validated against current length as the entry corresponding to the old length
			 * (the actual file length at the moment) might be missing, or more precisely will usually be missing.
			 * That is the reason for the truncate in the first place.
			 * So old length is nothing more than additional information for debugging purposes as the system
			 * can hardly be aware of it (only case is if a truncate has to be done caused by another channel)
			 */

			final long newLength = Logic.getFileLength(address);
			if(newLength < 0 || newLength > this.currentStoreLength)
			{
				throw new StorageExceptionConsistency(
					"Inconsistent new length in truncation entry: " + newLength + " vs. "
					+ this.currentStoreLength + " (file " + this.currentFileNumber + ")"
				);
			}

			this.updateMaxTimestamp(Logic.getEntryTimestamp(address));
			this.lastConsistentStoreLength = this.currentStoreLength = newLength;

			return true;
		}

		private boolean handleEntryFileDeletion(final long address, final long availableItemLength)
		{
			if(availableItemLength < Logic.LENGTH_FILE_DELETION)
			{
				return false;
			}

			final long number = Logic.getFileNumber(address);
			final StorageTransactionEntry.Default file = this.files.get(number);
			if(file == null)
			{
				throw new StorageException(this.hashIndex + " No file found in entries with number " + number);
			}
			file.isDeleted = true;

			this.updateMaxTimestamp(Logic.getEntryTimestamp(address));
			
			return true;
		}

		final StorageTransactionsAnalysis yield(final StorageLiveTransactionsFile transactionsFile)
		{
			// register latest file
			this.registerCurrentFile();

			return new StorageTransactionsAnalysis.Default(
				transactionsFile                 ,
				this.files                       ,
				this.lastConsistentStoreLength   ,
				this.lastConsistentStoreTimestamp,
				this.currentStoreLength          ,
				this.currentStoreTimestamp       ,
				this.maxTimeStamp
			);
		}
		
		private void updateMaxTimestamp(final long timestamp)
		{
			this.maxTimeStamp = Math.max(this.maxTimeStamp, timestamp);
		}

	}



	public final class Default implements StorageTransactionsAnalysis
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageLiveTransactionsFile                            transactionsFile                    ;
		private final XGettingTable<Long, ? extends StorageTransactionEntry> transactionsFileEntries             ;
		private final long                                                   headFileLastConsistentStoreLength   ;
		private final long                                                   headFileLastConsistentStoreTimestamp;
		private final long                                                   headFileLatestLength                ;
		private final long                                                   headFileLatestTimestamp             ;
		private final long                                                   maxTimestamp                        ;


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageLiveTransactionsFile                            transactionsFile                    ,
			final XGettingTable<Long, ? extends StorageTransactionEntry> transactionsFileEntries             ,
			final long                                                   headFileLastConsistentStoreLength   ,
			final long                                                   headFileLastConsistentStoreTimestamp,
			final long                                                   headFileLatestLength                ,
			final long                                                   headFileLatestTimestamp             ,
			final long                                                   maxTimestamp
		)
		{
			super();
			this.transactionsFile                     = notNull(transactionsFile)           ;
			this.transactionsFileEntries              = transactionsFileEntries             ;
			this.headFileLastConsistentStoreLength    = headFileLastConsistentStoreLength   ;
			this.headFileLastConsistentStoreTimestamp = headFileLastConsistentStoreTimestamp;
			this.headFileLatestLength                 = headFileLatestLength                ;
			this.headFileLatestTimestamp              = headFileLatestTimestamp             ;
			this.maxTimestamp                         = maxTimestamp                        ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final StorageLiveTransactionsFile transactionsFile()
		{
			return this.transactionsFile;
		}

		@Override
		public final XGettingTable<Long, ? extends StorageTransactionEntry> transactionsFileEntries()
		{
			return this.transactionsFileEntries;
		}

		@Override
		public final long headFileLastConsistentStoreLength()
		{
			return this.headFileLastConsistentStoreLength;
		}

		@Override
		public final long headFileLastConsistentStoreTimestamp()
		{
			return this.headFileLastConsistentStoreTimestamp;
		}

		@Override
		public final long headFileLatestLength()
		{
			return this.headFileLatestLength;
		}

		@Override
		public final long headFileLatestTimestamp()
		{
			return this.headFileLatestTimestamp;
		}
		
		@Override
		public final long maxTimestamp()
		{
			return this.maxTimestamp;
		}

	}

}
