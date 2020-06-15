package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.text.SimpleDateFormat;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.io.XIO;
import one.microstream.persistence.internal.UtilPersistenceIo;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionBackupCopying;
import one.microstream.storage.exceptions.StorageExceptionBackupEmptyStorageBackupAhead;
import one.microstream.storage.exceptions.StorageExceptionBackupEmptyStorageForNonEmptyBackup;
import one.microstream.storage.exceptions.StorageExceptionBackupInconsistentFileLength;
import one.microstream.storage.types.StorageBackupHandler.Default.ChannelInventory;

public interface StorageBackupHandler extends Runnable, StorageActivePart
{
	public StorageBackupSetup setup();
	
	public void initialize(int channelIndex);
	
	public void synchronize(StorageInventory storageInventory);
	
	public void copyFilePart(
		StorageFile sourceFile    ,
		long        sourcePosition,
		long        length
	);
	
	public void truncateFile(
		StorageFile file     ,
		long        newLength
	);
	
	public void deleteFile(
		StorageFile file
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
	
	@Override
	public boolean isActive();
	
	public StorageBackupHandler setRunning(boolean running);
	
	
	
	public static StorageBackupHandler New(
		final StorageBackupSetup         backupSetup        ,
		final int                        channelCount       ,
		final StorageBackupItemQueue     itemQueue          ,
		final StorageOperationController operationController,
		final StorageDataFileValidator   validator
	)
	{
		final StorageFileProvider backupFileProvider = backupSetup.backupFileProvider();
		
		final ChannelInventory[] cis = X.Array(ChannelInventory.class, channelCount, i ->
		{
			return new ChannelInventory(i, backupFileProvider);
		});
		
		return new StorageBackupHandler.Default(
	                cis                 ,
			notNull(backupSetup)        ,
			notNull(itemQueue)          ,
			notNull(operationController),
			notNull(validator)
		);
	}
	
	public final class Default implements StorageBackupHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageBackupSetup         backupSetup        ;
		private final ChannelInventory[]         channelInventories ;
		private final StorageBackupItemQueue     itemQueue          ;
		private final StorageOperationController operationController;
		private final StorageDataFileValidator   validator          ;
		
		private boolean running; // being "ordered" to run.
		private boolean active ; // being actually active, e.g. executing the last loop before running check.
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final ChannelInventory[]         channelInventories ,
			final StorageBackupSetup         backupSetup        ,
			final StorageBackupItemQueue     itemQueue          ,
			final StorageOperationController operationController,
			final StorageDataFileValidator   validator
		)
		{
			super();
			this.channelInventories  = channelInventories ;
			this.backupSetup         = backupSetup        ;
			this.itemQueue           = itemQueue          ;
			this.operationController = operationController;
			this.validator           = validator          ;
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
		public final synchronized boolean isActive()
		{
			return this.active;
		}
		
		@Override
		public final synchronized StorageBackupHandler setRunning(final boolean running)
		{
			this.running = running;
			return this;
		}
		
		
		protected StorageBackupFile resolveBackupTargetFile(final StorageFile sourceFile)
		{
			if(sourceFile instanceof StorageLiveDataFile)
			{
				return this.resolveBackupTargetFile((StorageLiveDataFile)sourceFile);
			}
			if(sourceFile instanceof StorageLiveTransactionsFile)
			{
				return this.resolveBackupTargetFile((StorageLiveTransactionsFile)sourceFile);
			}
			
			// (15.06.2020 TM)EXCP: proper exception
			throw new RuntimeException("Unhandled File Type: " + XChars.systemString(sourceFile));
		}
		
		private StorageBackupDataFile resolveBackupTargetFile(final StorageLiveDataFile sourceFile)
		{
			return this.channelInventories[sourceFile.channelIndex()].ensureBackupFile(sourceFile);
		}
		
		private StorageBackupTransactionsFile resolveBackupTargetFile(final StorageLiveTransactionsFile sourceFile)
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
				this.operationController.registerDisruption(e);
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
				this.operationController.registerDisruption(e);
				throw e;
			}
		}
		
		@Override
		public void run()
		{
			// must be the method instead of the field to check the lock but don't conver the whole loop
			try
			{
				this.active = true;
				
				// can not / may not copy storage files if the storage is not running (has locked and opend files, etc.)
				while(this.isRunning() && this.operationController.checkProcessingEnabled())
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
						this.operationController.registerDisruption(e);
						// see outer try-finally for cleanup
						throw e;
					}
				}
			}
			finally
			{
				// must close all open files on any aborting case (after stopping and before throwing an exception)
				this.closeAllDataFiles();
				this.active = false;
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
			for(final ZStorageInventoryFile storageFile : storageInventory.dataFiles().values())
			{
				final ZStorageBackupFile backupTargetFile = this.resolveBackupTargetFile(storageFile);
				this.copyFile(storageFile, backupTargetFile);
			}
			
			final ZStorageInventoryFile transactionFile = storageInventory.transactionsFileAnalysis().transactionsFile();
			final ZStorageBackupFile backupTransactionFile = this.resolveBackupTargetFile(transactionFile);
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
			for(final ZStorageInventoryFile storageFile : storageInventory.dataFiles().values())
			{
				final ZStorageBackupFile backupTargetFile = this.resolveBackupTargetFile(storageFile);
				
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
			final ZStorageBackupFile backupTransactionFile = backupInventory.ensureTransactionsFile();
			if(!backupTransactionFile.exists())
			{
				return;
			}
			
			final ZStorageNumberedFile deletionTargetFile = this.backupSetup.backupFileProvider()
				.provideDeletionTargetFile(backupTransactionFile)
			;
			
			if(deletionTargetFile == null)
			{
				if(backupTransactionFile.delete())
				{
					return;
				}

				// (02.10.2014 TM)EXCP: proper exception
				throw new StorageException("Could not delete file " + backupTransactionFile);
			}
			
			final String movedTargetFileName = this.createDeletionFileName(backupTransactionFile);
			final Path actualTargetFile = XIO.Path(deletionTargetFile.qualifier(), movedTargetFileName) ;
			UtilPersistenceIo.move(XIO.Path(backupTransactionFile.identifier()), actualTargetFile);
		}
		
		private String createDeletionFileName(final ZStorageBackupFile backupTransactionFile)
		{
			final String currentName = backupTransactionFile.name();
			final int lastDotIndex = currentName.lastIndexOf(XIO.fileSuffixSeparator());
			
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
			final StorageTransactionsAnalysis tfa = storageInventory.transactionsFileAnalysis();
			if(tfa == null)
			{
				this.deleteBackupTransactionFile(backupInventory);
				return;
			}
			
			final StorageTransactionsFile       storageTransactionsFile = tfa.transactionsFile();
			final StorageBackupTransactionsFile backupTransactionFile   = backupInventory.ensureTransactionsFile();
			
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
			final ZStorageInventoryFile storageFile     ,
			final ZStorageBackupFile    backupTargetFile
		)
		{
			this.copyFilePart(storageFile, 0, storageFile.length(), backupTargetFile);
		}
		
		private void copyFilePart(
			final StorageFile       sourceFile      ,
			final long              sourcePosition  ,
			final long              length          ,
			final StorageBackupFile backupTargetFile
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
			final StorageFile sourceFile    ,
			final long        sourcePosition,
			final long        copyLength
		)
		{
			// note: the original target file of the copying is irrelevant. Only the backup target file counts.
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(sourceFile);
			
			this.copyFilePart(sourceFile, sourcePosition, copyLength, backupTargetFile);
		}

		@Override
		public void truncateFile(
			final StorageFile file     ,
			final long  newLength
		)
		{
			final ZStorageBackupFile backupTargetFile = this.resolveBackupTargetFile(file);
			
			StorageFileWriter.truncateFile(backupTargetFile, newLength, this.backupSetup.backupFileProvider());
			
			// no user decrement since only the identifier is required and the actual file can well have been deleted.
		}
		
		@Override
		public void deleteFile(final StorageFile file)
		{
			final ZStorageBackupFile backupTargetFile = this.resolveBackupTargetFile(file);
			
			StorageFileWriter.deleteFile(backupTargetFile, this.backupSetup.backupFileProvider());
			
			// no user decrement since only the identifier is required and the actual file can well have been deleted.
		}
		
		final void closeAllDataFiles()
		{
			final DisruptionCollectorExecuting<StorageClosableFile> closer = DisruptionCollectorExecuting.New(file ->
				StorageClosableFile.close(file, null)
			);
			
			for(final ChannelInventory channel : this.channelInventories)
			{
				closer.executeOn(channel.transactionFile);
				for(final StorageBackupDataFile dataFile : channel.dataFiles.values())
				{
					closer.executeOn(dataFile);
				}
			}
			
			if(closer.hasDisruptions())
			{
				// (09.12.2019 TM)EXCP: proper exception
				throw new StorageException(closer.toMultiCauseException());
			}
		}
		
		
		
		static final class ChannelInventory implements StorageHashChannelPart
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final int                                      channelIndex      ;
			final StorageFileProvider                      backupFileProvider;
			      StorageBackupTransactionsFile            transactionFile   ;
			      EqHashTable<Long, StorageBackupDataFile> dataFiles         ;
			
			
			
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
			
			public final EqHashTable<Long, StorageBackupDataFile> dataFiles()
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
				
				final BulkList<StorageBackupDataFile> collectedFiles =
				this.backupFileProvider.collectDataFiles(
					StorageBackupDataFile::New,
					BulkList.New(),
					this.channelIndex()
				)
				.sort(ZStorageNumberedFile::orderByNumber);
				
				this.dataFiles = EqHashTable.New();
				
				collectedFiles.iterate(this::registerBackupFile);
				
				this.ensureTransactionsFile();
			}
			
			final StorageBackupTransactionsFile ensureBackupFile(final StorageLiveTransactionsFile sourceFile)
			{
				return this.ensureTransactionsFile();
			}
			
			
			final StorageBackupDataFile ensureBackupFile(final StorageLiveDataFile sourceFile)
			{
				// note: validation is done by the calling context, depending on its task.
				
				StorageBackupDataFile backupFile = this.dataFiles.get(sourceFile.number());
				if(backupFile == null)
				{
					backupFile = this.backupFileProvider.provideDataFile(
						StorageBackupDataFile::New,
						this.channelIndex,
						sourceFile.number()
					);
					this.dataFiles.add(backupFile.number(), backupFile);
				}
				
				return backupFile;
			}
			
			final StorageBackupTransactionsFile ensureTransactionsFile()
			{
				if(this.transactionFile == null)
				{
					this.transactionFile = this.backupFileProvider.provideTransactionsFile(
						StorageBackupTransactionsFile::New,
						this.channelIndex
					);
				}
				
				return this.transactionFile;
			}
			
		}
		
	}
	
}
