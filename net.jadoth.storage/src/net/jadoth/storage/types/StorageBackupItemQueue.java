package net.jadoth.storage.types;

public interface StorageBackupItemQueue extends StorageBackupItemEnqueuer
{
	public void processNextItem(StorageBackupHandler handler) throws InterruptedException;
	
	
	public final class Implementation implements StorageBackupItemQueue
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Item head = new Item(null, 0, 0, null);
		private       Item tail = this.head;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void enqueueCopyingItem(
			final StorageLockedChannelFile sourceFile    ,
			final long                     sourcePosition,
			final long                     length        ,
			final StorageLockedChannelFile targetFile
		)
		{
			this.internalEnqueueItem(sourceFile, sourcePosition, length, targetFile);
		}

		@Override
		public final void enqueueTruncatingItem(final StorageLockedChannelFile file, final long newLength)
		{
			// signalling with a null sourceFile is a hack to avoid the complexity of multiple Item classes
			this.internalEnqueueItem(null, 0, newLength, file);
		}
		
		private void internalEnqueueItem(
			final StorageLockedChannelFile sourceFile    ,
			final long                     sourcePosition,
			final long                     length        ,
			final StorageLockedChannelFile targetFile
		)
		{
			synchronized(this.head)
			{
				this.tail = this.tail.next = new Item(sourceFile, sourcePosition, length, targetFile);
				this.head.notifyAll();
			}
		}

		@Override
		public final void processNextItem(final StorageBackupHandler handler) throws InterruptedException
		{
			synchronized(this.head)
			{
				while(this.head.next == null)
				{
					this.wait();
				}
				
				final Item itemToBeProcessed = this.head.next;
				
				itemToBeProcessed.processBy(handler);
				
				if((this.head.next = itemToBeProcessed.next) == null)
				{
					// queue has been processed completely, reset to initial state of appending directly to the head.
					this.tail = this.head;
				}
			}
		}
		
		static final class Item
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final StorageLockedChannelFile sourceFile    ;
			final long                     sourcePosition;
			final long                     length        ;
			final StorageLockedChannelFile targetFile    ;

			Item next;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Item(
				final StorageLockedChannelFile sourceFile    ,
				final long                     sourcePosition,
				final long                     length        ,
				final StorageLockedChannelFile targetFile
			)
			{
				super();
				this.sourceFile     = sourceFile    ;
				this.sourcePosition = sourcePosition;
				this.length         = length        ;
				this.targetFile     = targetFile    ;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			public void processBy(final StorageBackupHandler handler)
			{
				// deciding on a null sourceFile is a hack to avoid the complexity of multiple Item classes
				if(this.sourceFile != null)
				{
					handler.copyFile(this.sourceFile, this.sourcePosition, this.length, this.targetFile);
				}
				else
				{
					handler.truncateFile(this.targetFile, this.length);
				}
			}
			
		}
		
	}
	
}
