package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.nio.channels.FileChannel;

import net.jadoth.X;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.XSort;
import net.jadoth.meta.XDebug;
import net.jadoth.storage.exceptions.StorageExceptionBackupCopying;
import net.jadoth.storage.exceptions.StorageExceptionBackupEmptyStorageBackupAhead;
import net.jadoth.storage.exceptions.StorageExceptionBackupEmptyStorageForNonEmptyBackup;
import net.jadoth.storage.exceptions.StorageExceptionBackupInconsistentFileLength;
import net.jadoth.storage.types.StorageBackupHandler.Implementation.ChannelInventory;

public interface StorageBackupHandler extends Runnable
{
	public StorageBackupSetup setup();
	
	public void initialize(StorageInventory storageInventory);
	
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
			final StorageNumberedFile rawTransactionsFile   = backupFileProvider.provideTransactionsFile(i);
			final StorageBackupFile   backupTransactionFile = StorageBackupFile.New(rawTransactionsFile);
			return new ChannelInventory(i, backupFileProvider, backupTransactionFile);
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
		public final void initialize(final StorageInventory storageInventory)
		{
			try
			{
				this.tryInitialize(storageInventory);
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
		
		private void tryInitialize(final StorageInventory storageInventory)
		{
			final ChannelInventory backupInventory = this.channelInventories[storageInventory.channelIndex()];
			backupInventory.ensureRegisteredFiles();

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
					
					
					// (28.02.2019 TM)FIXME: /!\ DEBUG (JET-55):
					if(Storage.isDataFile(sourceFile))
					{
						XDebug.println(
							"\nBackup copy:"
							+ "\nSource File: " + sourceFile.identifier()       + "(" + sourcePosition + " + " + length + " -> " + (sourcePosition + length) + ")"
							+ "\nBackup File: " + backupTargetFile.identifier() + "(" + oldBackupFileLength + " -> " + targetChannel.size() + ")"
						);
					}
					
					this.validator.validateFile(backupTargetFile, oldBackupFileLength, length);
				}
				catch(final Exception e)
				{
					throw new StorageExceptionBackupCopying(sourceFile, sourcePosition, length, backupTargetFile);
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
			
			StorageFileWriter.truncate(backupTargetFile, newLength, this.backupSetup.backupFileProvider());
			
			// no user decrement since only the identifier is required and the actual file can well have been deleted.
		}
		
		@Override
		public void deleteFile(final StorageInventoryFile file)
		{
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(file);
			
			StorageFileWriter.delete(backupTargetFile, this.backupSetup.backupFileProvider());
			
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
				final StorageFileProvider backupFileProvider,
				final StorageBackupFile   transactionFile
			)
			{
				super();
				this.channelIndex       = channelIndex      ;
				this.backupFileProvider = backupFileProvider;
				this.transactionFile    = transactionFile   ;
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
				
				final EqHashTable<Long, StorageBackupFile> existingBackupFiles = EqHashTable.New();
				this.backupFileProvider.collectDataFiles(
					f ->
					{
						final StorageBackupFile backupFile = StorageBackupFile.New(f);
						existingBackupFiles.add(f.number(), backupFile);
					},
					this.channelIndex()
				);
				existingBackupFiles.keys().sort(XSort::compare);
				
				this.dataFiles = existingBackupFiles;
				
				this.transactionFile = StorageBackupFile.New(
					this.backupFileProvider.provideTransactionsFile(this.channelIndex)
				);
			}
			
			final StorageBackupFile ensureBackupFile(final StorageNumberedFile file)
			{
				if(Storage.isTransactionFile(file))
				{
					return this.ensureTransactionsFile();
				}
				
				// note: validation is done by the calling context, depending on its task.
				
				StorageBackupFile bf = this.dataFiles.get(file.number());
				if(bf == null)
				{
					final StorageNumberedFile backupTargetFile = this.backupFileProvider.provideDataFile(
						this.channelIndex,
						file.number()
					);
					bf = StorageBackupFile.New(backupTargetFile);
					this.dataFiles.add(file.number(), bf);
				}
				
				return bf;
			}
			
			final StorageBackupFile ensureTransactionsFile()
			{
				if(this.transactionFile == null)
				{
					this.transactionFile = StorageBackupFile.New(
						this.backupFileProvider.provideTransactionsFile(this.channelIndex)
					);
				}
				
				return this.transactionFile;
			}
			
		}
		
	}
	
}
