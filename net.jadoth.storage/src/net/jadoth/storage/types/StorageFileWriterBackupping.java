package net.jadoth.storage.types;

import java.nio.ByteBuffer;


public final class StorageFileWriterBackupping implements StorageFileWriter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageFileWriter         delegate    ;
	private final StorageBackupItemEnqueuer itemEnqueuer;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	StorageFileWriterBackupping(
		final StorageFileWriter         delegate    ,
		final StorageBackupItemEnqueuer itemEnqueuer
	)
	{
		super();
		this.delegate     = delegate    ;
		this.itemEnqueuer = itemEnqueuer;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final long writeStore(final StorageDataFile<?> targetFile, final ByteBuffer[] byteBuffers)
	{
		final long oldFileLength = targetFile.length();
		final long byteCount = this.delegate.writeStore(targetFile, byteBuffers);
		
		// every item increases the user count, even if its the same file multiple times.
		targetFile.incrementUserCount();
		
		// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
		this.itemEnqueuer.enqueueCopyingItem(targetFile, oldFileLength, byteCount, null);
		
		return byteCount;
	}
	
	@Override
	public final long writeImport(
		final StorageFile        sourceFile  ,
		final long               sourceOffset,
		final long               length      ,
		final StorageDataFile<?> targetFile
	)
	{
		final long oldFileLength = targetFile.length();
		
		this.delegate.writeImport(sourceFile, sourceOffset, length, targetFile);
		
		// every item increases the user count, even if its the same file multiple times.
		targetFile.incrementUserCount();
		
		// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
		this.itemEnqueuer.enqueueCopyingItem(targetFile, oldFileLength, length, null);
		
		return length;
	}
	
	@Override
	public final long writeTransfer(
		final StorageDataFile<?> sourceFile  ,
		final long               sourceOffset,
		final long               length      ,
		final StorageDataFile<?> targetFile
	)
	{
		this.delegate.writeTransfer(sourceFile, sourceOffset, length, targetFile);
		
		// every item increases the user count, even if its the same file multiple times.
		sourceFile.incrementUserCount();
		targetFile.incrementUserCount();
		
		// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
		this.itemEnqueuer.enqueueCopyingItem(sourceFile, sourceOffset, length, targetFile);
		
		return length;
	}
	
	@Override
	public final long writeTransactionEntryStore(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile       ,
		final long                     dataFileOffset ,
		final long                     storeLength
	)
	{
		final long oldLength = transactionFile.length();
		final long byteCount = this.delegate.writeTransactionEntryStore(
			transactionFile,
			byteBuffers    ,
			dataFile       ,
			dataFileOffset ,
			storeLength
		);
		
		// every item increases the user count, even if its the same file multiple times.
		transactionFile.incrementUserCount();
		
		// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
		this.itemEnqueuer.enqueueCopyingItem(transactionFile, oldLength, byteCount, null);
		
		return byteCount;
	}
	
	@Override
	public final long writeTransactionEntryTransfer(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile       ,
		final long                     dataFileOffset ,
		final long                     storeLength
	)
	{
		final long oldLength = transactionFile.length();
		final long byteCount = this.delegate.writeTransactionEntryTransfer(transactionFile, byteBuffers, dataFile, dataFileOffset, storeLength);
		
		// every item increases the user count, even if its the same file multiple times.
		transactionFile.incrementUserCount();
		
		// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
		this.itemEnqueuer.enqueueCopyingItem(transactionFile, oldLength, byteCount, null);
		
		return byteCount;
	}
	
	@Override
	public final long writeTransactionEntryDelete(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile
	)
	{
		final long oldLength = transactionFile.length();
		final long byteCount = this.delegate.writeTransactionEntryDelete(transactionFile, byteBuffers, dataFile);
		
		// every item increases the user count, even if its the same file multiple times.
		transactionFile.incrementUserCount();
		
		// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
		this.itemEnqueuer.enqueueCopyingItem(transactionFile, oldLength, byteCount, null);
		
		return byteCount;
	}
	
	@Override
	public final long writeTransactionEntryCreate(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile
	)
	{
		final long oldLength = transactionFile.length();
		final long byteCount = this.delegate.writeTransactionEntryCreate(
			transactionFile,
			byteBuffers    ,
			dataFile
		);
		
		// every item increases the user count, even if its the same file multiple times.
		transactionFile.incrementUserCount();
		
		// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
		this.itemEnqueuer.enqueueCopyingItem(transactionFile, oldLength, byteCount, null);
		
		return byteCount;
	}

	@Override
	public final void truncate(final StorageLockedFile file, final long newLength)
	{
		this.delegate.truncate(file, newLength);
		this.itemEnqueuer.enqueueTruncatingItem(file, newLength);
	}
	
	
	
	public static final class Provider implements StorageFileWriter.Provider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final StorageBackupItemEnqueuer  backupItemEnqueuer;
		final StorageFileWriter.Provider wrappedProvider   ;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Provider(
			final StorageBackupItemEnqueuer  backupItemEnqueuer,
			final StorageFileWriter.Provider wrappedProvider
		)
		{
			super();
			this.backupItemEnqueuer = backupItemEnqueuer;
			this.wrappedProvider    = wrappedProvider   ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public StorageFileWriter provideWriter(final int channelIndex)
		{
			final StorageFileWriter delegateWriter = this.wrappedProvider.provideWriter(channelIndex);
			
			return new StorageFileWriterBackupping(delegateWriter, this.backupItemEnqueuer);
		}

	}
	
}
