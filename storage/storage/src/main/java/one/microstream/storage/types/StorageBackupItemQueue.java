package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

public interface StorageBackupItemQueue extends StorageBackupItemEnqueuer, StorageFileUser
{
	public boolean processNextItem(StorageBackupHandler handler, long timeoutMs) throws InterruptedException;
	
	public boolean isEmpty();
		
	public static StorageBackupItemQueue New()
	{
		return new StorageBackupItemQueue.Default();
	}
	
	public final class Default implements StorageBackupItemQueue
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Item head = new Item(null);
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
		public final boolean isEmpty()
		{
			return this.head.next == null;
		}
		
		@Override
		public final void enqueueCopyingItem(
			final StorageLiveChannelFile<?> sourceFile    ,
			final long               sourcePosition,
			final long               length
		)
		{
			this.internalEnqueueItem(new CopyItem(sourceFile, sourcePosition, length));
		}

		@Override
		public final void enqueueTruncatingItem(
			final StorageLiveChannelFile<?> file     ,
			final long               newLength
		)
		{
			// Signaling with a null sourceFile is a hack to avoid the complexity of multiple Item classes
			this.internalEnqueueItem(new TruncationItem(file, newLength));
		}
		
		@Override
		public void enqueueDeletionItem(
			final StorageLiveChannelFile<?> file
		)
		{
			// Signaling with a negative length is a hack to avoid the complexity of multiple Item classes
			this.internalEnqueueItem(new DeletionItem(file));
		}
		
		private void internalEnqueueItem(
			final Item item
		)
		{
			item.sourceFile.registerUsage(this);
			
			// no try-catch with unregisterUsage required since the following code is too simple to fail.
			synchronized(this.head)
			{
				this.tail = this.tail.next = item;
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
					if(!handler.isRunning())
					{
						return true;
					}
					
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
		
		static class Item
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final StorageLiveChannelFile<?> sourceFile;
			Item next;
			
			public Item(final StorageLiveChannelFile<?> sourceFile)
			{
				super();
				this.sourceFile = sourceFile;
			}
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			public void processBy(final StorageBackupHandler handler)
			{
				//no-op
				return;
			}
			
		}
		
		static final class CopyItem extends Item
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final long sourcePosition;
			final long length        ;
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			CopyItem(
				final StorageLiveChannelFile<?> sourceFile    ,
				final long                      sourcePosition,
				final long                      length
			)
			{
				super(sourceFile);
				this.sourcePosition = sourcePosition;
				this.length         = length        ;
			}

			@Override
			public void processBy(final StorageBackupHandler handler)
			{
				handler.copyFilePart(this.sourceFile, this.sourcePosition, this.length);
			}
		}
		
		static final class TruncationItem extends Item
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final long length;
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			TruncationItem(
				final StorageLiveChannelFile<?> sourceFile,
				final long                      length
			)
			{
				super(sourceFile);
				this.length = length;
			}

			@Override
			public void processBy(final StorageBackupHandler handler)
			{
				handler.truncateFile(this.sourceFile, this.length);
			}
		}
		
		static final class DeletionItem extends Item
		{
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			DeletionItem(
				final StorageLiveChannelFile<?> sourceFile
			)
			{
				super(sourceFile);
			}

			@Override
			public void processBy(final StorageBackupHandler handler)
			{
				handler.deleteFile(this.sourceFile);
			}
		}
		
	}
	
}
