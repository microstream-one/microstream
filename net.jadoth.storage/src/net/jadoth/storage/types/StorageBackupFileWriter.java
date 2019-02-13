package net.jadoth.storage.types;

import java.nio.ByteBuffer;

public interface StorageBackupFileWriter extends StorageFileWriter
{
	public final class Implementation implements StorageBackupFileWriter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageFileWriter         delegate    ;
		private final StorageBackupItemEnqueuer itemEnqueuer;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final StorageFileWriter         delegate    ,
			final StorageBackupItemEnqueuer itemEnqueuer
		)
		{
			super();
			this.delegate     = delegate    ;
			this.itemEnqueuer = itemEnqueuer;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long write(final StorageLockedFile file, final ByteBuffer[] byteBuffers)
		{
			final long oldFileLength = file.length();
			final long byteCount = this.delegate.write(file, byteBuffers);
			
			// every item increases the user count, even if its the same file multiple times.
			file.incrementUserCount();
			
			// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
			this.itemEnqueuer.enqueueBackupItem(file, oldFileLength, byteCount);
			
			return byteCount;
		}
		
	}
	
}
