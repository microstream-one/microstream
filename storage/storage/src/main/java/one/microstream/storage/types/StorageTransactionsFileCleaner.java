package one.microstream.storage.types;

/*-
 * #%L
 * MicroStream Storage
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import org.slf4j.Logger;

import one.microstream.X;
import one.microstream.memory.XMemory;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.types.StorageTransactionsAnalysis.EntryIterator;
import one.microstream.storage.types.StorageTransactionsAnalysis.Logic;
import one.microstream.util.logging.Logging;

public interface StorageTransactionsFileCleaner
{
	/**
	 * Reduces the size of the storage transactions log file if it exceeds the configured limit.
	 * <br>
	 * To shrink the file size all store, transfer, and truncation entries are combined into one single store entry
	 * for each storage files. FileCreation entries are kept, FileDeletion entries are kept
	 * if the storage data file still exists on the file system. Otherwise all entries related
	 * to deleted files are removed if the storage data file does no more exist.
	 * 
	 * @param checkSize if false the file is compacted regardless of its current size.
	 */
	public void compactTransactionsFile(boolean checkSize);
	
	public final class Default implements StorageTransactionsFileCleaner
	{
		private static class FileTransactionInfo
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private long creationFileLength;
			private long creationTimeStamp;
			private long storeFileLength;
			private long storeTimeStamp;
			private long deletionFileLength;
			private long deletionTimeStamp;
	
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			public FileTransactionInfo()
			{
				super();
			}
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			public void setCreation(final long fileLength, final long timestamp)
			{
				this.creationFileLength = fileLength;
				this.creationTimeStamp  = timestamp;
			}
			
			public void setStore(final long fileLength, final long timestamp)
			{
				this.storeFileLength = fileLength;
				this.storeTimeStamp  = timestamp;
			}
			
			public void setDeletion(final long fileLength, final long timestamp)
			{
				this.deletionFileLength = fileLength;
				this.deletionTimeStamp  = timestamp;
			}
				
		}
		
		private static class Collector implements EntryIterator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final LinkedHashMap<Long, FileTransactionInfo> transactions;
			private FileTransactionInfo currentTransactionInfo;
						
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			public Collector()
			{
				super();
				this.transactions = new LinkedHashMap<>();
			}
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			public LinkedHashMap<Long, FileTransactionInfo> transactions()
			{
				return this.transactions;
			}
			
			@Override
			public boolean accept(final long address, final long availableEntryLength)
			{
				// check for and skip gaps / comments
				if(availableEntryLength < 0)
				{
					return true;
				}
				
				switch(Logic.getEntryType(address))
				{
					case Logic.TYPE_FILE_CREATION  : return this.handleEntryFileCreation  (address, availableEntryLength);
					case Logic.TYPE_STORE          : return this.handleEntryStore         (address, availableEntryLength);
					case Logic.TYPE_TRANSFER       : return this.handleEntryTransfer      (address, availableEntryLength);
					case Logic.TYPE_FILE_TRUNCATION: return this.handleEntryFileTruncation(address, availableEntryLength);
					case Logic.TYPE_FILE_DELETION  : return this.handleEntryFileDeletion  (address, availableEntryLength);
					default:
					{
						throw new StorageException("Unknown transactions entry type: " + Logic.getEntryType(address));
					}
				}
			}
	
			private boolean handleEntryFileDeletion(final long address, final long availableEntryLength)
			{
				if(availableEntryLength < Logic.LENGTH_FILE_DELETION)
				{
					return false;
				}
				
				final long fileNumber = Logic.getFileNumber(address);
				final long fileLength = Logic.getFileLength(address);
				final long timestamp  = Logic.getEntryTimestamp(address);
				
				final FileTransactionInfo info = this.transactions.get(fileNumber);
				if(info == null)
				{
					throw new RuntimeException("no FileTransactionInfo for file found, file number: " + fileNumber);
				}
				
				info.setDeletion(fileLength, timestamp);
				
				return true;
			}
	
			private boolean handleEntryFileTruncation(final long address, final long availableEntryLength)
			{
				if(availableEntryLength < Logic.LENGTH_FILE_TRUNCATION)
				{
					return false;
				}
				
				final long FileLength = Logic.getFileLength(address);
				
				this.currentTransactionInfo.setStore(FileLength, this.currentTransactionInfo.storeTimeStamp);

				return true;
			}
	
			private boolean handleEntryTransfer(final long address, final long availableEntryLength)
			{
				if(availableEntryLength < Logic.LENGTH_TRANSFER)
				{
					return false;
				}
				
				final long FileLength = Logic.getFileLength(address);
				
				this.currentTransactionInfo.setStore(FileLength, this.currentTransactionInfo.storeTimeStamp);
				
				return true;
			}
	
			private boolean handleEntryStore(final long address, final long availableEntryLength)
			{
				if(availableEntryLength < Logic.LENGTH_STORE)
				{
					return false;
				}
				
				final long FileLength = Logic.getFileLength(address);
				final long timestamp  = Logic.getEntryTimestamp(address);
				
				this.currentTransactionInfo.setStore(FileLength, timestamp);

				return true;
			}
	
			private boolean handleEntryFileCreation(final long address, final long availableEntryLength)
			{
				if(availableEntryLength < Logic.LENGTH_FILE_CREATION)
				{
					return false;
				}
				
				final long fileNumber = Logic.getFileNumber(address);
				final long fileLength = Logic.getFileLength(address);
				final long timestamp  = Logic.getEntryTimestamp(address);
				
				this.currentTransactionInfo = new FileTransactionInfo();
				this.currentTransactionInfo.setCreation(fileLength, timestamp);
				
				if(null != this.transactions.putIfAbsent(fileNumber, this.currentTransactionInfo))
				{
					throw new RuntimeException("duplicated creation entry!");
				}
						
				return true;
			}
			
		}
	
		private final static Logger logger = Logging.getLogger(StorageTransactionsFileCleaner.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageLiveTransactionsFile storageLiveTransactionsFile;
		private final int channelIndex;
		private final long transactionFileSizeLimit;
		private final StorageFileWriter storageFileWriter;
		private final StorageLiveFileProvider fileProvider;

		/**
		 * The most actual store time stamp in all processed files.
		 * Required if a file has only transfers but no stores.
		 */
		private long lastStoreTimestamp;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(
			final StorageLiveTransactionsFile fileTransactions,
			final int channelIndex,
			final long transactionFileSizeLimit,
			final StorageLiveFileProvider fileProvider,
			final StorageFileWriter storageFileWriter)
		{
			super();
			this.channelIndex = channelIndex;
			this.transactionFileSizeLimit = transactionFileSizeLimit;
			this.storageLiveTransactionsFile = fileTransactions;
			this.fileProvider = fileProvider;
			this.storageFileWriter = storageFileWriter;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void compactTransactionsFile(final boolean checkSize)
		{
			if(checkSize == true && this.storageLiveTransactionsFile.size() > this.transactionFileSizeLimit)
			{
				logger.info("Transaction file {} size exceeds limit of {} bytes", this.storageLiveTransactionsFile.identifier(), this.transactionFileSizeLimit);
				this.compactTransactionsFileInternal();
			}
			else if(checkSize == false)
			{
				this.compactTransactionsFileInternal();
			}
		}
		
		private void compactTransactionsFileInternal()
		{
			logger.info("Compacting transaction file {}", this.storageLiveTransactionsFile.identifier());
			
			final LinkedHashMap<Long, FileTransactionInfo> transactions = this.collectEntries();
			this.lastStoreTimestamp = this.getLastStoreTimestamp(transactions);
			
			this.removeDeletedEntries(transactions);
			
			this.storageFileWriter.truncate(this.storageLiveTransactionsFile, 0, this.fileProvider);
			this.writeTransactionLog(transactions);
		}
		
		private LinkedHashMap<Long, FileTransactionInfo> collectEntries()
		{
			final Collector collector = new Collector();
			StorageTransactionsAnalysis.Logic.processInputFile(this.storageLiveTransactionsFile.file().tryUseReading(), collector);
			return collector.transactions();
		}
		
		private long getLastStoreTimestamp(final LinkedHashMap<Long, FileTransactionInfo> transactions)
		{
			return transactions.values().stream().mapToLong((x)->x.storeTimeStamp).max().orElse(0);
		}
			
		private void removeDeletedEntries(final LinkedHashMap<Long, FileTransactionInfo> transactions)
		{
			transactions.entrySet().removeIf( e -> {
				logger.debug("channel {} file {} no more existing, removing all entries from transactions log ", this.channelIndex, e.getKey());
				return !this.fileProvider.provideDataFile(this.channelIndex, e.getKey()).exists();
			});
		}
		
		private void writeTransactionLog(final LinkedHashMap<Long, FileTransactionInfo> transactions)
		{
			final ByteBuffer
			entryBufferFileCreation   = XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthFileCreation()),
			entryBufferStore          = XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthStore())       ,
			entryBufferFileDeletion   = XMemory.allocateDirectNative(StorageTransactionsAnalysis.Logic.entryLengthFileDeletion());
			
			final Iterable<? extends ByteBuffer>
			entryBufferWrapFileCreation   = X.ArrayView(entryBufferFileCreation  ),
			entryBufferWrapStore          = X.ArrayView(entryBufferStore         ),
			entryBufferWrapFileDeletion   = X.ArrayView(entryBufferFileDeletion  );
			
			final long
			entryBufferFileCreationAddress   = XMemory.getDirectByteBufferAddress(entryBufferFileCreation)  ,
			entryBufferStoreAddress          = XMemory.getDirectByteBufferAddress(entryBufferStore)         ,
			entryBufferFileDeletionAddress   = XMemory.getDirectByteBufferAddress(entryBufferFileDeletion)  ;
			
			StorageTransactionsAnalysis.Logic.initializeEntryFileCreation  (entryBufferFileCreationAddress  );
			StorageTransactionsAnalysis.Logic.initializeEntryStore         (entryBufferStoreAddress         );
			StorageTransactionsAnalysis.Logic.initializeEntryFileDeletion  (entryBufferFileDeletionAddress  );
			
			transactions.forEach( (k,v) -> {
				
				entryBufferFileCreation.clear();
				StorageTransactionsAnalysis.Logic.setEntryFileCreation(
					entryBufferFileCreationAddress,
					v.creationFileLength,
					v.creationTimeStamp,
					k
				);
				this.storageFileWriter.writeTransactionEntryCreate(this.storageLiveTransactionsFile, entryBufferWrapFileCreation, null);
									
								
				if(v.storeTimeStamp > 0) {
			
					entryBufferStore.clear();
					StorageTransactionsAnalysis.Logic.setEntryStore(
						entryBufferStoreAddress,
						v.storeFileLength,
						v.storeTimeStamp
					);
					
					this.storageFileWriter.writeTransactionEntryStore(
						this.storageLiveTransactionsFile,
						entryBufferWrapStore,
						null,
						0,
						v.storeFileLength);
				}
				else
				{
					//special case: there is no real store entry that sets a correct time stamp and file length.
					//This may happen if older data file are deleted and the current one has only transfers.
					
					entryBufferStore.clear();
					StorageTransactionsAnalysis.Logic.setEntryStore(
						entryBufferStoreAddress,
						v.storeFileLength,
						this.lastStoreTimestamp
					);
					
					this.storageFileWriter.writeTransactionEntryStore(
						this.storageLiveTransactionsFile,
						entryBufferWrapStore,
						null,
						0,
						v.storeFileLength);
				}
							
				if(v.deletionTimeStamp > 0) {
					entryBufferFileDeletion.clear();
					StorageTransactionsAnalysis.Logic.setEntryFileDeletion(
						entryBufferFileDeletionAddress,
						v.deletionFileLength,
						v.deletionTimeStamp,
						k
					);
					this.storageFileWriter.writeTransactionEntryDelete(this.storageLiveTransactionsFile, entryBufferWrapFileDeletion, null);
				}
			});
			
			XMemory.deallocateDirectByteBuffer(entryBufferFileCreation);
			XMemory.deallocateDirectByteBuffer(entryBufferStore);
			XMemory.deallocateDirectByteBuffer(entryBufferFileDeletion);
		}
		
	}
	
}
