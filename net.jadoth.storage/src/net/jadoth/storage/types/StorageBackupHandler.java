package net.jadoth.storage.types;

import java.io.File;

public interface StorageBackupHandler
{
	public void copyFile(
		StorageLockedChannelFile sourceFile    ,
		long                     sourcePosition,
		long                     length        ,
		StorageLockedChannelFile targetFile
	);
	
	public void truncateFile(
		StorageLockedChannelFile file     ,
		long                     newLength
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
		
		private final File[]                 channelTargetDirectories;
		private final StorageBackupItemQueue itemQueue               ;
		private       boolean                running                 ;
		
		/* (15.02.2019 TM)FIXME: JET-55: Backup Thread exception handling
		 * Can't just throw exceptions since they would simply terminate the backup thread
		 * and leave the rest (application and storage channel thrads) unaffected.
		 * There must be a kind of exception callback to report exceptions to.
		 */
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final File[]                 channelTargetDirectories,
			final StorageBackupItemQueue itemQueue
		)
		{
			super();
			this.channelTargetDirectories = channelTargetDirectories;
			this.itemQueue                = itemQueue               ;
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
		
		private File resolveTargetFile(final StorageLockedChannelFile sourceFile)
		{
			/* (15.02.2019 TM)TODO: File instantiation is rather costly (see inside). Internal mapping instead?
			 * But is the slight performance gain worth the permanent memory occupation?
			 */
			return new File(this.channelTargetDirectories[sourceFile.channelIndex()], sourceFile.name());
		}
		
		@Override
		public void copyFile(
			final StorageLockedChannelFile sourceFile    ,
			final long                     sourcePosition,
			final long                     length        ,
			final StorageLockedChannelFile targetFile
		)
		{
			final File backupTargetFile = this.resolveTargetFile(sourceFile);
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME JET-55: StorageBackupHandler#copyFile()
		}

		@Override
		public void truncateFile(
			final StorageLockedChannelFile file     ,
			final long                     newLength
		)
		{
			final File backupTargetFile = this.resolveTargetFile(file);
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
		
	}
	
}
