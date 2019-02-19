package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import net.jadoth.X;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.XSort;
import net.jadoth.storage.types.StorageBackupHandler.Implementation.ChannelInventory;

public interface StorageBackupHandler
{
	public void initialize(StorageInventory storageInventory);
	
	public void copyFile(
		StorageNumberedFile sourceFile    ,
		long                sourcePosition,
		long                length        ,
		StorageNumberedFile targetFile
	);
	
	public void truncateFile(
		StorageNumberedFile file     ,
		long                newLength
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
			// (17.02.2019 TM)FIXME: JET-55: distinct transaction file
			/* (15.02.2019 TM)TODO: File instantiation is rather costly (see inside). Internal mapping instead?
			 * But is the slight performance gain worth the permanent memory occupation?
			 */
			return this.channelInventories[sourceFile.channelIndex()].ensureBackupFile(sourceFile);
		}
		
		@Override
		public void initialize(final StorageInventory storageInventory)
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
		}
		
		final void updateExistingBackup(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			for(final StorageNumberedFile storageFile : storageInventory.dataFiles().values())
			{
				final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(storageFile);
				/* (16.02.2019 TM)FIXME: JET-55: check backup file
				 * - existence
				 * - length
				 * - inconsistency in any non-last file is an error.
				 * - inconsistency in last file gets compensated.
				 */
			}
		}
		
		@Override
		public void copyFile(
			final StorageNumberedFile sourceFile    ,
			final long                sourcePosition,
			final long                length        ,
			final StorageNumberedFile targetFile
		)
		{
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(sourceFile);
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME JET-55: StorageBackupHandler#copyFile()
		}

		@Override
		public void truncateFile(
			final StorageNumberedFile file     ,
			final long                newLength
		)
		{
			final StorageBackupFile backupTargetFile = this.resolveBackupTargetFile(file);
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME JET-55: StorageBackupHandler#truncateFile()
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
				
				// (17.02.2019 TM)FIXME: JET-55: check length etc? Or is that done somewhere else? Comment accordingly.
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
