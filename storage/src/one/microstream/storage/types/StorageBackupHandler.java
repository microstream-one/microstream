package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.persistence.internal.UtilPersistenceIo;
import one.microstream.storage.exceptions.StorageExceptionBackupCopying;
import one.microstream.storage.exceptions.StorageExceptionBackupEmptyStorageBackupAhead;
import one.microstream.storage.exceptions.StorageExceptionBackupEmptyStorageForNonEmptyBackup;
import one.microstream.storage.exceptions.StorageExceptionBackupInconsistentFileLength;
import one.microstream.storage.types.StorageBackupHandler.Implementation.ChannelInventory;

public interface StorageBackupHandler extends Runnable
{
	public StorageBackupSetup setup();
	
	public void initialize(int channelIndex);
	
	public void synchronize(StorageInventory storageInventory);
	
	public void copyFilePart(
		StorageInventoryFile sourceFile    ,
		long                 sourcePosition,
		long                 length
	);
	
	public void truncateFile(
		StorageInventoryFile file     ,
		long                 newLength
	);
	
	public void deleteFile(
		StorageInventoryFile file
	);
	
	public default StorageBackupHandler start()
	{
		this.setRunning(true);
		return this;
	}
	
	public default StorageBackupHandler stop()
	{
		this.setRunning(false);
		return this;
	}
	
	public boolean isRunning();
	
	public StorageBackupHandler setRunning(boolean running);
	
	
	
	public static StorageBackupHandler New(
		final StorageBackupSetup       backupSetup      ,
		final int                      channelCount     ,
		final StorageBackupItemQueue   itemQueue        ,
		final StorageChannelController channelController,
		final StorageDataFileValidator validator
	)
	{
		final StorageFileProvider backupFileProvider = backupSetup.backupFileProvider();
		
		final ChannelInventory[] cis = X.Array(ChannelInventory.class, channelCount, i ->
		{
			return new ChannelInventory(i, backupFileProvider);
		});
		
		return new StorageBackupHandler.Implementation(
	                cis               ,
			notNull(backupSetup)      ,
			notNull(itemQueue)        ,
			notNull(channelController),
			notNull(validator)
		);
	}
	
	public final class Implementation implements StorageBackupHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		private final StorageBackupSetup       backupSetup       ;
		private final ChannelInventory[]       channelInventories;
		private final StorageBackupItemQueue   itemQueue         ;
		private final StorageChannelController channelController ;
		private final StorageDataFileValidator validator         ;
		
		private boolean running;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final ChannelInventory[]       channelInventories,
			final StorageBackupSetup       backupSetup       ,
			final StorageBackupItemQueue   itemQueue         ,
			final StorageChannelController channelController ,
			final StorageDataFileValidator validator
		)
		{
			super();
			this.channelInventories = channelInventories;
			this.backupSetup        = backupSetup       ;
			this.itemQueue          = itemQueue         ;
			this.channelController  = channelController ;
			this.validator          = validator         ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final StorageBackupSetup setup()
		{
			return this.backupSetup;
		}
		
		@Override
		public final synchronized boolean isRunning()
		{
			return this.running;
		}
		
		@Override
		public final synchronized StorageBackupHandler setRunning(final boolean running)
		{
			this.running = running;
			return this;
		}
		
		private StorageBackupFile resolveBackupTargetFile(final StorageNumberedFile sourceFile)
		{
			return this.channelInventories[sourceFile.channelIndex()].ensureBackupFile(sourceFile);
		}
		

		@Override
		public void initialize(final int channelIndex)
		{
			try
			{
				this.tryInitialize(channelIndex);
			}
			catch(final RuntimeException e)
			{
				this.channelController.registerDisruptingProblem(e);
				throw e;
			}
		}
		
		@Override
		public void synchronize(final StorageInventory storageInventory)
		{
			try
			{
				this.trySynchronize(storageInventory);
			}
			catch(final RuntimeException e)
			{
				this.channelController.registerDisruptingProblem(e);
				throw e;
			}
		}
		
		@Override
		public void run()
		{
			// must be the method instead of the field to check the lock but don't conver the whole loop
			while(this.isRunning())
			{
				try
				{
					if(!this.itemQueue.processNextItem(this, 10_000))
					{
						this.validator.freeMemory();
					}
				}
				catch(final InterruptedException e)
				{
					// still not sure about the viability of interruption handling in a case like this.
					this.stop();
				}
				catch(final RuntimeException e)
				{
					this.channelController.registerDisruptingProblem(e);
					throw e;
				}
			}
		}
		
		private void tryInitialize(final int channelIndex)
		{
			final ChannelInventory backupInventory = this.channelInventories[channelIndex];
			backupInventory.ensureRegisteredFiles();
		}
		
		private void trySynchronize(final StorageInventory storageInventory)
		{
			final ChannelInventory backupInventory = this.channelInventories[storageInventory.channelIndex()];
			if(backupInventory.dataFiles.isEmpty())
			{
				this.fillEmptyBackup(storageInventory, backupInventory);
			}
			else
			{
				this.updateExistingBackup(storageInventory, backupInventory);
			}
		}
		
		final void fillEmptyBackup(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			for(final StorageInventoryFile storageFile : storageInventory.dataFiles().values())
			{
				final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(storageFile);
				this.copyFile(storageFile, backupTargetFile);
			}
			
			final StorageInventoryFile transactionFile = storageInventory.transactionsFileAnalysis().transactionsFile();
			final StorageBackupFile backupTransactionFile = this.resolveBackupTargetFile(transactionFile);
			this.copyFile(transactionFile, backupTransactionFile);
		}
		
		private void validateStorageInventoryForExistingBackup(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			if(!storageInventory.dataFiles().isEmpty())
			{
				return;
			}
			
			throw new StorageExceptionBackupEmptyStorageForNonEmptyBackup(
				backupInventory.channelIndex(),
				backupInventory.dataFiles()
			);
		}
		
		private void validateBackupFileProgress(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			final long lastStorageFileNumber = storageInventory.dataFiles().keys().last();
			final long lastBackupFileNumber  = backupInventory.dataFiles().keys().last();
			
			if(lastBackupFileNumber <= lastStorageFileNumber)
			{
				return;
			}
			
			throw new StorageExceptionBackupEmptyStorageBackupAhead(
				storageInventory,
				backupInventory.dataFiles()
			);
		}
		
		final void updateExistingBackup(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			this.validateStorageInventoryForExistingBackup(storageInventory, backupInventory);
			this.validateBackupFileProgress(storageInventory, backupInventory);

			final long lastBackupFileNumber = backupInventory.dataFiles().keys().last();
			for(final StorageInventoryFile storageFile : storageInventory.dataFiles().values())
			{
				final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(storageFile);
				
				// non-existant files have either not been backupped, yet, or a "healable" error.
				if(!backupTargetFile.exists())
				{
					// in any case, the storage file is simply copied (backed up)
					this.copyFile(storageFile, backupTargetFile);
					continue;
				}
				
				final long storageFileLength      = storageFile.length();
				final long backupTargetFileLength = backupTargetFile.length();
				
				// existing file with matching length means everything is fine
				if(storageFileLength == backupTargetFileLength)
				{
					// continue with next file
					continue;
				}

				// the last/latest/highest existing backup file can validly diverge in length.
				if(backupTargetFile.number() == lastBackupFileNumber)
				{
					// missing length is copied to update the backup file
					this.copyFilePart(
						storageFile,
						backupTargetFileLength,
						storageFileLength - backupTargetFileLength,
						backupTargetFile
					);
					continue;
				}
				
				// any existing non-last backup file with divergent length is a consistency error
				throw new StorageExceptionBackupInconsistentFileLength(
					storageInventory           ,
					backupInventory.dataFiles(),
					storageFile                ,
					storageFileLength          ,
					backupTargetFile           ,
					backupTargetFileLength
				);
			}
			
			this.synchronizeTransactionFile(storageInventory, backupInventory);
		}
		
		private void deleteBackupTransactionFile(final ChannelInventory backupInventory)
		{
			final StorageBackupFile backupTransactionFile = backupInventory.ensureTransactionsFile();
			if(!backupTransactionFile.exists())
			{
				return;
			}
			
			final StorageNumberedFile deletionTargetFile = this.backupSetup.backupFileProvider()
				.provideDeletionTargetFile(backupTransactionFile)
			;
			
			if(deletionTargetFile == null)
			{
				if(backupTransactionFile.delete())
				{
					return;
				}

				// (02.10.2014 TM)EXCP: proper exception
				throw new RuntimeException("Could not delete file " + backupTransactionFile);
			}
			
			final String movedTargetFileName = this.createDeletionFileName(backupTransactionFile);
			final File actualTargetFile = new File(deletionTargetFile.qualifier(), movedTargetFileName) ;
			UtilPersistenceIo.move(new File(backupTransactionFile.identifier()), actualTargetFile);
		}
		
		private String createDeletionFileName(final StorageBackupFile backupTransactionFile)
		{
			final String currentName = backupTransactionFile.name();
			final int lastDotIndex = currentName.lastIndexOf('.');
			
			final String namePrefix;
			final String nameSuffix;
			if(lastDotIndex >= 0)
			{
				namePrefix = currentName.substring(0, lastDotIndex);
				nameSuffix = currentName.substring(lastDotIndex);
			}
			else
			{
				namePrefix = currentName;
				nameSuffix = "";
			}
			
			final SimpleDateFormat sdf = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss_SSS");
			final String newFileName = namePrefix + sdf.format(System.currentTimeMillis()) + nameSuffix;
			
			return newFileName;
		}
		
		private void synchronizeTransactionFile(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			// tfa null means there is no transactions file. A non-existing transactions file later on is an error.
			final StorageTransactionsFileAnalysis tfa = storageInventory.transactionsFileAnalysis();
			if(tfa == null)
			{
				this.deleteBackupTransactionFile(backupInventory);
				return;
			}
			
			final StorageInventoryFile storageTransactionsFile = tfa.transactionsFile();
			final StorageBackupFile    backupTransactionFile   = backupInventory.ensureTransactionsFile();
			
			if(!backupTransactionFile.exists())
			{
				// if the backup transaction file does not exist, yet, the actual file is simply copied.
				this.copyFile(storageTransactionsFile, backupTransactionFile);
				return;
			}

			final long storageFileLength      = storageTransactionsFile.length();
			final long backupTargetFileLength = backupTransactionFile.length();
			
			if(backupTargetFileLength != storageFileLength)
			{
				// on any mismatch, the backup transaction file is deleted (potentially moved&renamed) and rebuilt.
				this.deleteBackupTransactionFile(backupInventory);
				this.copyFile(storageTransactionsFile, backupTransactionFile);
			}
		}
				
		private void copyFile(
			final StorageInventoryFile storageFile     ,
			final StorageBackupFile    backupTargetFile
		)
		{
			this.copyFilePart(storageFile, 0, storageFile.length(), backupTargetFile);
		}
		
		private void copyFilePart(
			final StorageInventoryFile sourceFile      ,
			final long                 sourcePosition  ,
			final long                 length          ,
			final StorageBackupFile    backupTargetFile
		)
		{
			try
			{
				final FileChannel sourceChannel = sourceFile.fileChannel();
				final FileChannel targetChannel = backupTargetFile.fileChannel();
				
				final long oldBackupFileLength = targetChannel.size();
				
				try
				{
					final long byteCount = sourceChannel.transferTo(sourcePosition, length, targetChannel);
					StorageFileWriter.validateIoByteCount(length, byteCount);
					targetChannel.force(false);
					
//					if(Storage.isDataFile(sourceFile))
//					{
//						XDebug.println(
//							"\nBackup copy:"
//							+ "\nSource File: " + sourceFile.identifier()       + "(" + sourcePosition + " + " + length + " -> " + (sourcePosition + length) + ")"
//							+ "\nBackup File: " + backupTargetFile.identifier() + "(" + oldBackupFileLength + " -> " + targetChannel.size() + ")"
//						);
//					}
					
					this.validator.validateFile(backupTargetFile, oldBackupFileLength, length);
				}
				catch(final Exception e)
				{
					throw new StorageExceptionBackupCopying(sourceFile, sourcePosition, length, backupTargetFile, e);
				}
				finally
				{
					backupTargetFile.close();
				}
			}
			catch(final Exception e)
			{
				throw new StorageExceptionBackupCopying(sourceFile, sourcePosition, length, backupTargetFile, e);
			}
		}
		
		@Override
		public void copyFilePart(
			final StorageInventoryFile sourceFile    ,
			final long                 sourcePosition,
			final long                 copyLength
		)
		{
			// note: the original target file of the copying is irrelevant. Only the backup target file counts.
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(sourceFile);
			
			copyFilePart(sourceFile, sourcePosition, copyLength, backupTargetFile);

			sourceFile.decrementUserCount();
		}

		@Override
		public void truncateFile(
			final StorageInventoryFile file     ,
			final long                 newLength
		)
		{
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(file);
			
			StorageFileWriter.truncateFile(backupTargetFile, newLength, this.backupSetup.backupFileProvider());
			
			// no user decrement since only the identifier is required and the actual file can well have been deleted.
		}
		
		@Override
		public void deleteFile(final StorageInventoryFile file)
		{
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(file);
			
			StorageFileWriter.deleteFile(backupTargetFile, this.backupSetup.backupFileProvider());
			
			// no user decrement since only the identifier is required and the actual file can well have been deleted.
		}
		
		static final class ChannelInventory implements StorageHashChannelPart
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final int                                  channelIndex      ;
			final StorageFileProvider                  backupFileProvider;
			      StorageBackupFile                    transactionFile   ;
			      EqHashTable<Long, StorageBackupFile> dataFiles         ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			ChannelInventory(
				final int                 channelIndex      ,
				final StorageFileProvider backupFileProvider
			)
			{
				super();
				this.channelIndex       = channelIndex      ;
				this.backupFileProvider = backupFileProvider;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final int channelIndex()
			{
				return this.channelIndex;
			}
			
			public final EqHashTable<Long, StorageBackupFile> dataFiles()
			{
				return this.dataFiles;
			}
			
			final void ensureRegisteredFiles()
			{
				if(this.dataFiles != null)
				{
					// files already registered
					return;
				}
				
				final BulkList<StorageNumberedFile> collectedFiles =
				this.backupFileProvider.collectDataFiles(
					BulkList.New(),
					this.channelIndex()
				)
				.sort(StorageNumberedFile::orderByNumber);
				
				this.dataFiles = EqHashTable.New();
				
				collectedFiles.iterate(this::registerBackupFile);
				
				this.ensureTransactionsFile();
			}
			
			final StorageBackupFile ensureBackupFile(final StorageNumberedFile sourceFile)
			{
				if(Storage.isTransactionFile(sourceFile))
				{
					return this.ensureTransactionsFile();
				}
				
				// note: validation is done by the calling context, depending on its task.
				
				StorageBackupFile backupTargetFile = this.dataFiles.get(sourceFile.number());
				if(backupTargetFile == null)
				{
					final StorageNumberedFile backupRawFile = this.backupFileProvider.provideDataFile(
						this.channelIndex,
						sourceFile.number()
					);
					backupTargetFile = registerBackupFile(backupRawFile);
				}
				
				return backupTargetFile;
			}
			
			private StorageBackupFile registerBackupFile(final StorageNumberedFile backupRawFile)
			{
				final StorageBackupFile backupTargetFile = StorageBackupFile.New(backupRawFile);
				this.dataFiles.add(backupTargetFile.number(), backupTargetFile);
				
				return backupTargetFile;
			}
			
			final StorageBackupFile ensureTransactionsFile()
			{
				if(this.transactionFile == null)
				{
					final StorageNumberedFile rawFile = this.backupFileProvider.provideTransactionsFile(
						this.channelIndex
					);
					this.transactionFile = StorageBackupFile.New(rawFile);
				}
				
				return this.transactionFile;
			}
			
		}
		
	}
	
}
