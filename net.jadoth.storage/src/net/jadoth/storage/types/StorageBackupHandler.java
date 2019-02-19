package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.IOException;

import net.jadoth.X;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.XSort;
import net.jadoth.storage.types.StorageBackupHandler.Implementation.ChannelInventory;

public interface StorageBackupHandler
{
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
		final int                         channelCount      ,
		final StorageFileProvider         backupFileProvider,
		final StorageBackupItemQueue      itemQueue         ,
		final StorageBackupProblemHandler problemHandler
	)
	{
		final ChannelInventory[] cis = X.Array(ChannelInventory.class, channelCount, i ->
		{
			final StorageNumberedFile transactionsFile = backupFileProvider.provideTransactionsFile(i);
			final StorageBackupFile   backupFile       = StorageBackupFile.New(transactionsFile);
			return new ChannelInventory(i, backupFileProvider, backupFile);
		});
		
		return new StorageBackupHandler.Implementation(
			cis                    ,
			notNull(itemQueue)     ,
			notNull(problemHandler)
		);
	}
	
	public final class Implementation implements StorageBackupHandler, Runnable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ChannelInventory[]          channelInventories;
		private final StorageBackupItemQueue      itemQueue         ;
		private final StorageBackupProblemHandler problemHandler    ;
		
		private boolean running;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final ChannelInventory[]          channelInventories,
			final StorageBackupItemQueue      itemQueue         ,
			final StorageBackupProblemHandler problemHandler
		)
		{
			super();
			this.itemQueue          = itemQueue         ;
			this.channelInventories = channelInventories;
			this.problemHandler     = problemHandler    ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public synchronized final boolean isRunning()
		{
			return this.running;
		}
		
		@Override
		public synchronized final void setRunning(final boolean running)
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
			catch(final Exception e)
			{
				// (19.02.2019 TM)FIXME: JET-55: Problem Handling
				this.problemHandler.reportAllKindsOfPeskyProblems(0, 0);
				throw new Error(e); // reaching here means an error in the problem handler for not throwing an exception.
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
		
		final void updateExistingBackup(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			if(storageInventory.dataFiles().isEmpty())
			{
				// (19.02.2019 TM)FIXME: JET-55: Problem Handling
				this.problemHandler.reportAllKindsOfPeskyProblems(0, 0);
				throw new Error(); // reaching here means an error in the problem handler for not throwing an exception.
			}
			
			final long lastStorageFileNumber = storageInventory.dataFiles().keys().last();
			final long lastBackupFileNumber  = backupInventory.dataFiles().keys().last();
			
			if(lastBackupFileNumber > lastStorageFileNumber)
			{
				// (19.02.2019 TM)FIXME: JET-55: Problem Handling
				this.problemHandler.reportAllKindsOfPeskyProblems(0, 0);
				throw new Error(); // reaching here means an error in the problem handler for not throwing an exception.
			}
			
			for(final StorageInventoryFile storageFile : storageInventory.dataFiles().values())
			{
				final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(storageFile);
				if(backupTargetFile.exists())
				{
					if(backupTargetFile.length() == storageFile.length())
					{
						// perfect match, everything is fine
						continue;
					}
					
					if(backupTargetFile.number() == lastBackupFileNumber)
					{
						this.copyFile(
							storageFile,
							backupTargetFile.length(),
							storageFile.length() - backupTargetFile.length(),
							backupTargetFile
						);
					}
					
					// (19.02.2019 TM)FIXME: JET-55: Problem Handling
					this.problemHandler.reportAllKindsOfPeskyProblems(0, 0);
					throw new Error(); // reaching here means an error in the problem handler for not throwing an exception.
				}
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
			catch(final IOException e)
			{
				// (19.02.2019 TM)FIXME: JET-55: Problem Handling
				this.problemHandler.reportAllKindsOfPeskyProblems(0, 0);
				throw new Error(e); // reaching here means an error in the problem handler for not throwing an exception.
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
			
			if(file != null)
			{
				file.decrementUserCount();
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
				catch(final Exception e)
				{
					// (19.02.2019 TM)FIXME: JET-55: Generic Problem Handling
					this.problemHandler.reportAllKindsOfPeskyProblems(0, 0);
					throw new Error(e); // reaching here means an error in the problem handler for not throwing an exception.
				}
			}
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
