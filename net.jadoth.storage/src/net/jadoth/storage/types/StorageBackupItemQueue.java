package net.jadoth.storage.types;

public interface StorageBackupItemQueue extends StorageBackupItemEnqueuer
{
	public void processNextItem(StorageBackupHandler handler) throws InterruptedException;
	
	
	
	public static StorageBackupItemQueue New()
	{
		return new StorageBackupItemQueue.Implementation();
	}
	
	public final class Implementation implements StorageBackupItemQueue
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Item head = new Item(null, 0, 0, null);
		private       Item tail = this.head;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void enqueueCopyingItem(
			final StorageInventoryFile sourceFile    ,
			final long                 sourcePosition,
			final long                 length        ,
			final StorageInventoryFile targetFile
		)
		{
			this.internalEnqueueItem(sourceFile, sourcePosition, length, targetFile);
		}

		@Override
		public final void enqueueTruncatingItem(
			final StorageInventoryFile file     ,
			final long                 newLength
		)
		{
			// signalling with a null sourceFile is a hack to avoid the complexity of multiple Item classes
			this.internalEnqueueItem(null, 0, newLength, file);
		}
		
		@Override
		public void enqueueDeletionItem(final StorageInventoryFile file)
		{
			// signalling with a negative length is a hack to avoid the complexity of multiple Item classes
			this.internalEnqueueItem(null, 0, -1, file);
		}
		
		private void internalEnqueueItem(
			final StorageInventoryFile sourceFile    ,
			final long                 sourcePosition,
			final long                 length        ,
			final StorageInventoryFile targetFile
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
					this.head.wait();
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
			
			final StorageInventoryFile sourceFile    ;
			final long                 sourcePosition;
			final long                 length        ;
			final StorageInventoryFile targetFile    ;

			Item next;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Item(
				final StorageInventoryFile sourceFile    ,
				final long                 sourcePosition,
				final long                 length        ,
				final StorageInventoryFile targetFile
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
					handler.copyFilePart(this.sourceFile, this.sourcePosition, this.length, this.targetFile);
				}
				else if(this.length < 0)
				{
					handler.deleteFile(this.targetFile);
				}
				else
				{
					handler.truncateFile(this.targetFile, this.length);
				}
			}
			
		}
		
	}
	
}
