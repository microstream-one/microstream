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
		public final long writeStore(
			final StorageLiveDataFile            targetFile ,
			final Iterable<? extends ByteBuffer> byteBuffers
		)
		{
			final long oldTargetFileLength = targetFile.size();
			final long byteCount = this.delegate.writeStore(targetFile, byteBuffers);
						
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(targetFile, oldTargetFileLength, byteCount);
			
			return byteCount;
		}
		
		@Override
		public final long writeImport(
			final StorageImportSource source      ,
			final long                sourceOffset,
			final long                copyLength  ,
			final StorageLiveDataFile targetFile
		)
		{
			final long oldTargetFileLength = targetFile.size();
			this.delegate.writeImport(source, sourceOffset, copyLength, targetFile);
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(targetFile, oldTargetFileLength, copyLength);
			
			return copyLength;
		}
		
		@Override
		public final long writeTransfer(
			final StorageLiveDataFile sourceFile  ,
			final long                sourceOffset,
			final long                length      ,
			final StorageLiveDataFile targetFile
		)
		{
			final long oldTargetFileLength = targetFile.size();
			this.delegate.writeTransfer(sourceFile, sourceOffset, length, targetFile);
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueCopyingItem(targetFile, oldTargetFileLength, length);
			
			return length;
		}
		
		@Override
		public final long writeTransactionEntryCreate(
			final StorageLiveTransactionsFile    transactionFile,
			final Iterable<? extends ByteBuffer> byteBuffers    ,
			final StorageLiveDataFile            dataFile
		)
		{
			final long oldLength = transactionFile.size();
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
			final StorageLiveTransactionsFile    transactionFile,
			final Iterable<? extends ByteBuffer> byteBuffers    ,
			final StorageLiveDataFile            dataFile       ,
			final long                           dataFileOffset ,
			final long                           storeLength
		)
		{
			final long oldLength = transactionFile.size();
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
			final StorageLiveTransactionsFile    transactionFile,
			final Iterable<? extends ByteBuffer> byteBuffers    ,
			final StorageLiveDataFile            dataFile       ,
			final long                           dataFileOffset ,
			final long                           storeLength
		)
		{
			final long oldLength = transactionFile.size();
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
			final StorageLiveTransactionsFile    transactionFile,
			final Iterable<? extends ByteBuffer> byteBuffers    ,
			final StorageLiveDataFile            dataFile
		)
		{
			final long oldLength = transactionFile.size();
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
			final StorageLiveTransactionsFile    transactionFile,
			final Iterable<? extends ByteBuffer> byteBuffers    ,
			final StorageLiveDataFile            file           ,
			final long                           newFileLength
		)
		{
			final long oldLength = transactionFile.size();
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
			final StorageLiveChannelFile<?> file        ,
			final long                      newLength   ,
			final StorageFileProvider       fileProvider
		)
		{
			// no user increment since only the identifier is required and the actual file can well be deleted.
			this.delegate.truncate(file, newLength, fileProvider);
			this.itemEnqueuer.enqueueTruncatingItem(file, newLength);
		}
		
		@Override
		public void delete(
			final StorageLiveDataFile    file           ,
			final StorageWriteController writeController,
			final StorageFileProvider    fileProvider
		)
		{
			// no user increment since only the identifier is required and the actual file can well be deleted.
			this.delegate.delete(file, writeController, fileProvider);
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
