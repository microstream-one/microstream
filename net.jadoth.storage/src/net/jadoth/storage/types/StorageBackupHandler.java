package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import net.jadoth.X;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.XSort;
import net.jadoth.storage.exceptions.StorageExceptionBackupCopying;
import net.jadoth.storage.exceptions.StorageExceptionBackupEmptyStorageBackupAhead;
import net.jadoth.storage.exceptions.StorageExceptionBackupEmptyStorageForNonEmptyBackup;
import net.jadoth.storage.exceptions.StorageExceptionBackupInconsistentFileLength;
import net.jadoth.storage.types.StorageBackupHandler.Implementation.ChannelInventory;

public interface StorageBackupHandler extends Runnable
{
	public StorageBackupSetup setup();
	
	public void initialize(StorageInventory storageInventory);
	
	public void copyFile(
		StorageInventoryFile sourceFile    ,
		long                 sourcePosition,
		long                 length        ,
		StorageInventoryFile targetFile
	);
	
	public void truncateFile(
		StorageInventoryFile file     ,
		long                 newLength
	);
	
	public void deleteFile(
		StorageInventoryFile file
	);
	
	public default void start()
	{
		this.setRunning(true);
	}
	
	public default void stop()
	{
		this.setRunning(false);
	}
	
	public boolean isRunning();
	
	public void setRunning(boolean running);
	
	
	
	public static StorageBackupHandler New(
		final StorageBackupSetup       backupSetup      ,
		final int                      channelCount     ,
		final StorageBackupItemQueue   itemQueue        ,
		final StorageChannelController channelController
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
			notNull(channelController)
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
		
		private boolean running;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final ChannelInventory[]       channelInventories,
			final StorageBackupSetup       backupSetup       ,
			final StorageBackupItemQueue   itemQueue         ,
			final StorageChannelController channelController
		)
		{
			super();
			this.channelInventories = channelInventories;
			this.backupSetup        = backupSetup       ;
			this.itemQueue          = itemQueue         ;
			this.channelController  = channelController ;
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
		public final synchronized void setRunning(final boolean running)
		{
			this.running = running;
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
					this.itemQueue.processNextItem(this);
				}
				catch(final InterruptedException e)
				{
					// still not sure about the viability of interruption handling in the general case.
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
				
				// existing file with matching length means everything is fine
				if(backupTargetFile.length() == storageFile.length())
				{
					// continue with next file
					continue;
				}

				// the last/latest/highest existing backup file can validly diverge in length.
				if(backupTargetFile.number() == lastBackupFileNumber)
				{
					// missing length is copied to update the backup file
					this.copyFile(
						storageFile,
						backupTargetFile.length(),
						storageFile.length() - backupTargetFile.length(),
						backupTargetFile
					);
					continue;
				}
				
				// any existing non-last backup file with divergent length is a consistency error
				throw new StorageExceptionBackupInconsistentFileLength(
					storageInventory           ,
					backupInventory.dataFiles(),
					storageFile                ,
					backupTargetFile
				);
			}
		}
				
		private void copyFile(
			final StorageInventoryFile storageFile     ,
			final StorageBackupFile    backupTargetFile
		)
		{
			this.copyFile(storageFile, 0, storageFile.length(), backupTargetFile);
		}
		
		private void copyFile(
			final StorageInventoryFile storageFile     ,
			final long                 sourcePosition  ,
			final long                 length          ,
			final StorageBackupFile    backupTargetFile
		)
		{
			try
			{
				storageFile.channel().transferTo(sourcePosition, length, backupTargetFile.channel());
				
				// backup file always gets closed right away.
				backupTargetFile.close();
			}
			catch(final Exception e)
			{
				throw new StorageExceptionBackupCopying(storageFile, sourcePosition, length, backupTargetFile, e);
			}
		}
		
		@Override
		public void copyFile(
			final StorageInventoryFile sourceFile    ,
			final long                 sourcePosition,
			final long                 length        ,
			final StorageInventoryFile targetFile
		)
		{
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(sourceFile);
			
			// (19.02.2019 TM)FIXME: JET-55: StorageBackupHandler#copyFile()
			
			if(sourceFile != null)
			{
				sourceFile.decrementUserCount();
			}
			if(targetFile != null)
			{
				targetFile.decrementUserCount();
			}
		}

		@Override
		public void truncateFile(
			final StorageInventoryFile file     ,
			final long                 newLength
		)
		{
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(file);
			
			// FIXME JET-55: StorageBackupHandler#truncateFile()
			
			file.decrementUserCount();
		}
		
		@Override
		public void deleteFile(final StorageInventoryFile file)
		{
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(file);
			
			// FIXME JET-55: StorageBackupHandler.Implementation#deleteFile()
			
			file.decrementUserCount();
			throw new net.jadoth.meta.NotImplementedYetError();
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
				if(file.number() == Storage.transactionsFileNumber())
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
