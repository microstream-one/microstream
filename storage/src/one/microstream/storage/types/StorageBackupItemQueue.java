package one.microstream.storage.types;

public interface StorageBackupItemQueue extends StorageBackupItemEnqueuer, StorageFileUser
{
	public boolean processNextItem(StorageBackupHandler handler, long timeoutMs) throws InterruptedException;

	
	
	public static StorageBackupItemQueue New()
	{
		return new StorageBackupItemQueue.Default();
	}
	
	public final class Default implements StorageBackupItemQueue
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Item head = new Item(null, 0, 0);
		private       Item tail = this.head;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void enqueueCopyingItem(
			final StorageLiveChannelFile<?> sourceFile    ,
			final long               sourcePosition,
			final long               length
		)
		{
			this.internalEnqueueItem(sourceFile, sourcePosition, length);
		}

		@Override
		public final void enqueueTruncatingItem(
			final StorageLiveChannelFile<?> file     ,
			final long               newLength
		)
		{
			// signalling with a null sourceFile is a hack to avoid the complexity of multiple Item classes
			this.internalEnqueueItem(file, newLength, -1);
		}
		
		@Override
		public void enqueueDeletionItem(
			final StorageLiveChannelFile<?> file
		)
		{
			// signalling with a negative length is a hack to avoid the complexity of multiple Item classes
			this.internalEnqueueItem(file, 0, -1);
		}
		
		private void internalEnqueueItem(
			final StorageLiveChannelFile<?> sourceFile    ,
			final long                      sourcePosition,
			final long                      length
		)
		{
			sourceFile.registerUsage(this);
			
			// no try-catch with unregisterUsage required since the following code is too simple to fail.
			synchronized(this.head)
			{
				this.tail = this.tail.next = new Item(sourceFile, sourcePosition, length);
				this.head.notifyAll();
			}
		}

		@Override
		public final boolean processNextItem(
			final StorageBackupHandler handler  ,
			final long                 timeoutMs
		)
			throws InterruptedException
		{
			final long timeBudgetBound = System.currentTimeMillis() + timeoutMs;
			final long waitInterval    = timeoutMs / 16;
			
			synchronized(this.head)
			{
				while(this.head.next == null)
				{
					if(System.currentTimeMillis() >= timeBudgetBound)
					{
						return false;
					}
					
					this.head.wait(waitInterval);
				}
				
				final Item itemToBeProcessed = this.head.next;
				
				itemToBeProcessed.processBy(handler);
				
				// the backup thread can be the last active part of an already shutdown storage, so it has to clean up.
				itemToBeProcessed.sourceFile.unregisterUsageClosing(this, null);
				
				if((this.head.next = itemToBeProcessed.next) == null)
				{
					// queue has been processed completely, reset to initial state of appending directly to the head.
					this.tail = this.head;
				}
				
				return true;
			}
		}
		
		static final class Item
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final StorageLiveChannelFile<?> sourceFile    ;
			final long                      sourcePosition;
			final long                      length        ;

			Item next;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Item(
				final StorageLiveChannelFile<?> sourceFile    ,
				final long                      sourcePosition,
				final long                      length
			)
			{
				super();
				this.sourceFile     = sourceFile    ;
				this.sourcePosition = sourcePosition;
				this.length         = length        ;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			public void processBy(final StorageBackupHandler handler)
			{
				// negative length used as a hack ("reduce file") to avoid the complexity of multiple Item classes
				if(this.length < 0)
				{
					if(this.sourcePosition == 0)
					{
						// reduce to 0 means deleting the file.
						handler.deleteFile(this.sourceFile);
					}
					else
					{
						// reduce to a non-zero position means truncation.
						handler.truncateFile(this.sourceFile, this.sourcePosition);
					}
				}
				else
				{
					handler.copyFilePart(this.sourceFile, this.sourcePosition, this.length);
				}
			}
			
		}
		
	}
	
}
