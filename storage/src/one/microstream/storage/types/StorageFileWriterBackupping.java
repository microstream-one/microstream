package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;


public interface StorageFileWriterBackupping extends StorageFileWriter
{
	public final class Default implements StorageFileWriter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageFileWriter         delegate    ;
		private final StorageBackupItemEnqueuer itemEnqueuer;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
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
		public final long writeStore(final ZStorageDataFile<?> targetFile, final ByteBuffer[] byteBuffers)
		{
			final long oldTargetFileLength = targetFile.length();
			final long byteCount = this.delegate.writeStore(targetFile, byteBuffers);
						
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(targetFile, oldTargetFileLength, byteCount);
			
			return byteCount;
		}
		
		@Override
		public final long writeImport(
			final ZStorageLockedFile  sourceFile  ,
			final long               sourceOffset,
			final long               copyLength  ,
			final ZStorageDataFile<?> targetFile
		)
		{
			final long oldTargetFileLength = targetFile.length();
			this.delegate.writeImport(sourceFile, sourceOffset, copyLength, targetFile);
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(targetFile, oldTargetFileLength, copyLength);
			
			return copyLength;
		}
		
		@Override
		public final long writeTransfer(
			final ZStorageDataFile<?> sourceFile  ,
			final long               sourceOffset,
			final long               length      ,
			final ZStorageDataFile<?> targetFile
		)
		{
			final long oldTargetFileLength = targetFile.length();
			this.delegate.writeTransfer(sourceFile, sourceOffset, length, targetFile);
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(targetFile, oldTargetFileLength, length);
			
			return length;
		}
		
		@Override
		public final long writeTransactionEntryCreate(
			final ZStorageInventoryFile transactionFile,
			final ByteBuffer[]         byteBuffers    ,
			final ZStorageDataFile<?>   dataFile
		)
		{
			final long oldLength = transactionFile.length();
			final long byteCount = this.delegate.writeTransactionEntryCreate(
				transactionFile,
				byteBuffers    ,
				dataFile
			);
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(transactionFile, oldLength, byteCount);
			
			return byteCount;
		}
		
		@Override
		public final long writeTransactionEntryStore(
			final ZStorageInventoryFile transactionFile,
			final ByteBuffer[]         byteBuffers    ,
			final ZStorageDataFile<?>   dataFile       ,
			final long                 dataFileOffset ,
			final long                 storeLength
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
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(transactionFile, oldLength, byteCount);
			
			return byteCount;
		}
		
		@Override
		public final long writeTransactionEntryTransfer(
			final ZStorageInventoryFile transactionFile,
			final ByteBuffer[]             byteBuffers    ,
			final ZStorageDataFile<?>       dataFile       ,
			final long                     dataFileOffset ,
			final long                     storeLength
		)
		{
			final long oldLength = transactionFile.length();
			final long byteCount = this.delegate.writeTransactionEntryTransfer(
				transactionFile,
				byteBuffers    ,
				dataFile       ,
				dataFileOffset ,
				storeLength
			);
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(transactionFile, oldLength, byteCount);
			
			return byteCount;
		}
		
		@Override
		public final long writeTransactionEntryDelete(
			final ZStorageInventoryFile transactionFile,
			final ByteBuffer[]         byteBuffers    ,
			final ZStorageDataFile<?>   dataFile
		)
		{
			final long oldLength = transactionFile.length();
			final long byteCount = this.delegate.writeTransactionEntryDelete(
				transactionFile,
				byteBuffers    ,
				dataFile
			);
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(transactionFile, oldLength, byteCount);
			
			return byteCount;
		}
		
		@Override
		public final long writeTransactionEntryTruncate(
			final ZStorageInventoryFile transactionFile,
			final ByteBuffer[]             byteBuffers    ,
			final ZStorageInventoryFile     file           ,
			final long                     newFileLength
		)
		{
			final long oldLength = transactionFile.length();
			final long byteCount = this.delegate.writeTransactionEntryTruncate(
				transactionFile,
				byteBuffers    ,
				file           ,
				newFileLength
			);
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(transactionFile, oldLength, byteCount);
			
			return byteCount;
		}

		@Override
		public final void truncate(
			final ZStorageInventoryFile file               ,
			final long                 newLength          ,
			final StorageFileProvider  storageFileProvider
		)
		{
			// no user increment since only the identifier is required and the actual file can well be deleted.
			this.delegate.truncate(file, newLength, storageFileProvider);
			this.itemEnqueuer.enqueueTruncatingItem(file, newLength);
		}
		
		@Override
		public void delete(final ZStorageInventoryFile file, final StorageFileProvider storageFileProvider)
		{
			// no user increment since only the identifier is required and the actual file can well be deleted.
			this.delegate.delete(file, storageFileProvider);
			this.itemEnqueuer.enqueueDeletionItem(file);
		}
		
	}
	
	public static StorageFileWriterBackupping.Provider Provider(
		final StorageBackupItemEnqueuer  backupItemEnqueuer,
		final StorageFileWriter.Provider wrappedProvider
	)
	{
		return new StorageFileWriterBackupping.Provider.Default(
			notNull(backupItemEnqueuer),
			notNull(wrappedProvider)
		);
	}

	public interface Provider extends StorageFileWriter.Provider
	{
		
		public static final class Default implements StorageFileWriterBackupping.Provider
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final StorageBackupItemEnqueuer  backupItemEnqueuer;
			final StorageFileWriter.Provider wrappedProvider   ;
					
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Default(
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
				
				return new StorageFileWriterBackupping.Default(
					delegateWriter,
					this.backupItemEnqueuer
				);
			}
			
			@Override
			public StorageFileWriter provideWriter()
			{
				// non-channel-file writing (e.g. lock file) is not part of the backupping (yet), so just pass through.
				return this.wrappedProvider.provideWriter();
			}

		}
	}
	
	
	
}
