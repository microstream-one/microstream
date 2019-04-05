package one.microstream.storage.types;

import one.microstream.concurrency.XThreads;

public interface StorageLockFileManager extends Runnable
{
	public default StorageLockFileManager start()
	{
		this.setRunning(true);
		return this;
	}
	
	public default StorageLockFileManager stop()
	{
		this.setRunning(false);
		return this;
	}
	
	public boolean isRunning();
	
	public StorageLockFileManager setRunning(boolean running);
	
	
	
	public final class Default implements StorageLockFileManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageLockFileSetup       setup              ;
		private final StorageOperationController operationController;

		// cached values
		private transient boolean           isRunning;
		private transient StorageLockedFile lockFile;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final StorageLockFileSetup       setup              ,
			final StorageOperationController operationController
		)
		{
			super();
			this.setup               = setup              ;
			this.operationController = operationController;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized final boolean isRunning()
		{
			return this.isRunning;
		}

		@Override
		public synchronized final StorageLockFileManager setRunning(final boolean running)
		{
			this.isRunning = running;
			
			return this;
		}
		
		private synchronized boolean checkIsRunning()
		{
			return this.isRunning && this.operationController.checkProcessingEnabled();
		}

		@Override
		public final void run()
		{
			final long updateInterval = this.setup.updateInterval();
			
			try
			{
				// causes the initial write
				this.ensureInitialized();
				
				// wait first after the intial write, then perform the regular update
				while(this.checkIsRunning())
				{
					XThreads.sleep(updateInterval);
					this.updateFile();
				}
			}
			catch(final Exception e)
			{
				this.operationController.registerDisruptingProblem(e);
				throw e;
			}
		}
		
		private void ensureInitialized()
		{
			if(this.lockFile != null)
			{
				return;
			}
			this.initialize();
		}
		
		private void initialize()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME StorageLockFileManager.Default#initialize()
		}
		
		private void updateFile()
		{
			// check again after the wait time.
			if(this.checkIsRunning())
			{
				// abort to avoid un unnecessary write.
				return;
			}
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME StorageLockFileManager.Default#updateFile()
		}
		
	}
	
}
