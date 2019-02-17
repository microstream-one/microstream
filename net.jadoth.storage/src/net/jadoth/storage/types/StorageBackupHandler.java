package net.jadoth.storage.types;

import net.jadoth.X;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingEnum;

public interface StorageBackupHandler
{
	public void initialize(
		StorageChannelFile                          transactionFile,
		XGettingEnum<? extends StorageNumberedFile> storageFiles
	);
	
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
	
	
	
	public final class Implementation implements StorageBackupHandler, Runnable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageFileProvider    backupFileProvider;
		private final ChannelInventory[]     channelInventories;
		private final StorageBackupItemQueue itemQueue         ;
		private       boolean                running           ;
		
		/* (15.02.2019 TM)FIXME: JET-55: Backup Thread exception handling
		 * Can't just throw exceptions since they would simply terminate the backup thread
		 * and leave the rest (application and storage channel thrads) unaffected.
		 * There must be a kind of exception callback to report exceptions to.
		 */
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final int                    channelCount      ,
			final StorageFileProvider    backupFileProvider,
			final StorageBackupItemQueue itemQueue
		)
		{
			super();
			this.backupFileProvider = backupFileProvider;
			this.itemQueue          = itemQueue         ;
			this.channelInventories = X.Array(ChannelInventory.class, channelCount, i ->
				new ChannelInventory(channelCount, backupFileProvider, backupFileProvider.provideTransactionsFile(i))
			);
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
		
		private StorageBackupFile resolveTargetFile(final StorageNumberedFile sourceFile)
		{
			/* (15.02.2019 TM)TODO: File instantiation is rather costly (see inside). Internal mapping instead?
			 * But is the slight performance gain worth the permanent memory occupation?
			 */
			return this.channelInventories[sourceFile.channelIndex()].ensureBackupFile(sourceFile);
		}
		
		@Override
		public void initialize(
			final StorageChannelFile                          transactionFile,
			final XGettingEnum<? extends StorageNumberedFile> storageFiles
		)
		{
			for(final StorageNumberedFile storageFile : storageFiles)
			{
				final StorageBackupFile backupTargetFile = this.resolveTargetFile(storageFile);
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
			final StorageBackupFile backupTargetFile = this.resolveTargetFile(sourceFile);
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME JET-55: StorageBackupHandler#copyFile()
		}

		@Override
		public void truncateFile(
			final StorageNumberedFile file     ,
			final long                newLength
		)
		{
			final StorageBackupFile backupTargetFile = this.resolveTargetFile(file);
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
			
			private final int                                  channelIndex      ;
			private final StorageFileProvider                  backupFileProvider;
			private final EqHashTable<Long, StorageBackupFile> inventory          = EqHashTable.New();
			private final StorageBackupFile                    transactionFile;
			
			
			
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
			
			final StorageBackupFile ensureBackupFile(final StorageNumberedFile dataFile)
			{
				
			}
			
		}
		
	}
	
}
