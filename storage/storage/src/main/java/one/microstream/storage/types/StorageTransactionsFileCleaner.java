package one.microstream.storage.types;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
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
			private long lastFileLength;
			private long lastTimeStamp;
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
				this.lastFileLength = fileLength;
				this.lastTimeStamp  = timestamp;
			}
			
			public void setDeletion(final long fileLength, final long timestamp)
			{
				this.deletionFileLength = fileLength;
				this.deletionTimeStamp  = timestamp;
			}
	
			@Override
			public String toString() {
				return "FileTransactionInfo "
						+ "\n[ creationFileLength=" + this.creationFileLength + ", creationTimeStamp="
						+ formatTimeStamp(this.creationTimeStamp) + "\n, lastFileLength=" + this.lastFileLength + ", lastTimeStamp="
						+ formatTimeStamp(this.lastTimeStamp) + "\n, deletionFileLength=" + this.deletionFileLength + ", deletionTimeStamp="
						+ formatTimeStamp(this.deletionTimeStamp) + "]\n";
			}
			
			private static String formatTimeStamp(final long timestamp) {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date(Storage.millisecondsToSeconds(timestamp)));
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
				
				this.currentTransactionInfo.setStore(FileLength, this.currentTransactionInfo.lastTimeStamp);

				return true;
			}
	
			private boolean handleEntryTransfer(final long address, final long availableEntryLength)
			{
				if(availableEntryLength < Logic.LENGTH_TRANSFER)
				{
					return false;
				}
				
				final long FileLength = Logic.getFileLength(address);
				final long timestamp  = Logic.getEntryTimestamp(address);
				
				this.currentTransactionInfo.setStore(FileLength, timestamp);
				
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
		
		private final StorageLiveTransactionsFile StorageLiveTransactionsFile;
		private final int channelIndex;
		private final long transactionFileSizeLimit;
		private final StorageFileWriter storageFileWriter;
		private final StorageLiveFileProvider fileProvider;
		
		
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
			this.StorageLiveTransactionsFile = fileTransactions;
			this.fileProvider = fileProvider;
			this.storageFileWriter = storageFileWriter;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void compactTransactionsFile(final boolean checkSize)
		{
			if(checkSize == true && this.StorageLiveTransactionsFile.size() > this.transactionFileSizeLimit)
			{
				logger.info("Transaction file {} size exceeds limit of {} bytes", this.StorageLiveTransactionsFile.identifier(), this.transactionFileSizeLimit);
						
				final LinkedHashMap<Long, FileTransactionInfo> transactions = this.collectEntries();
				this.removeDeletedEntries(transactions);
				
				this.storageFileWriter.truncate(this.StorageLiveTransactionsFile, 0, this.fileProvider);
				this.writeTransactionLog(transactions);
			}
		}
		
		private LinkedHashMap<Long, FileTransactionInfo> collectEntries()
		{
			final Collector collector = new Collector();
			StorageTransactionsAnalysis.Logic.processInputFile(this.StorageLiveTransactionsFile.file().tryUseReading(), collector);
			return collector.transactions();
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
				this.storageFileWriter.writeTransactionEntryCreate(this.StorageLiveTransactionsFile, entryBufferWrapFileCreation, null);
									
				entryBufferStore.clear();
				StorageTransactionsAnalysis.Logic.setEntryStore(
					entryBufferStoreAddress,
					v.lastFileLength,
					v.lastTimeStamp
				);
				
				this.storageFileWriter.writeTransactionEntryStore(
					this.StorageLiveTransactionsFile,
					entryBufferWrapStore,
					null,
					0,
					v.lastFileLength);
				});
			
				transactions.forEach( (k,v) -> {
					if(v.deletionTimeStamp > 0) {
						entryBufferFileDeletion.clear();
						StorageTransactionsAnalysis.Logic.setEntryFileDeletion(
							entryBufferFileDeletionAddress,
							v.deletionFileLength,
							v.deletionTimeStamp,
							k
						);
						this.storageFileWriter.writeTransactionEntryDelete(this.StorageLiveTransactionsFile, entryBufferWrapFileDeletion, null);
				}
						
			});
			
			XMemory.deallocateDirectByteBuffer(entryBufferFileCreation);
			XMemory.deallocateDirectByteBuffer(entryBufferStore);
			XMemory.deallocateDirectByteBuffer(entryBufferFileDeletion);
		}
		
	}
	
}