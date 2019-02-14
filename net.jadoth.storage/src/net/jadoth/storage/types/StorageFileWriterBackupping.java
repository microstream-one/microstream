package net.jadoth.storage.types;

import java.nio.ByteBuffer;


public final class StorageFileWriterBackupping implements StorageFileWriter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageFileWriter         delegate    ;
	private final StorageBackupItemEnqueuer itemEnqueuer;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	StorageFileWriterBackupping(
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
	public final long writeStore(final StorageDataFile<?> targetFile, final ByteBuffer[] byteBuffers)
	{
		final long oldFileLength = targetFile.length();
		final long byteCount = this.delegate.writeStore(targetFile, byteBuffers);
		
		// every item increases the user count, even if its the same file multiple times.
		targetFile.incrementUserCount();
		
		// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
		this.itemEnqueuer.enqueueBackupItem(targetFile, oldFileLength, byteCount, null);
		
		return byteCount;
	}
	
	@Override
	public final long writeTransfer(
		final StorageDataFile<?> sourceFile  ,
		final StorageDataFile<?> targetfile  ,
		final long               sourceOffset,
		final long               length
	)
	{
		final long byteCount = this.delegate.writeTransfer(sourceFile, targetfile, sourceOffset, length);
		
		// every item increases the user count, even if its the same file multiple times.
		sourceFile.incrementUserCount();
		targetfile.incrementUserCount();
		
		// backup item is enqueued and will be processed by the backup thread, which then decrements the user count.
		this.itemEnqueuer.enqueueBackupItem(sourceFile, sourceOffset, byteCount, targetfile);
		
		return byteCount;
	}
	
	// (14.02.2019 TM)FIXME: JET-55: Transaction File Writes.
	
	
	
	public static final class Provider implements StorageFileWriter.Provider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final StorageBackupItemEnqueuer  backupItemEnqueuer;
		final StorageFileWriter.Provider wrappedProvider   ;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Provider(
			final StorageBackupItemEnqueuer  backupItemEnqueuer,
			final StorageFileWriter.Provider wrappedProvider
		)
		{
			super();
			this.backupItemEnqueuer = backupItemEnqueuer;
			this.wrappedProvider    = wrappedProvider   ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public StorageFileWriter provideWriter(final int channelIndex)
		{
			final StorageFileWriter delegateWriter = this.wrappedProvider.provideWriter(channelIndex);
			
			return new StorageFileWriterBackupping(delegateWriter, this.backupItemEnqueuer);
		}

	}
	
}
