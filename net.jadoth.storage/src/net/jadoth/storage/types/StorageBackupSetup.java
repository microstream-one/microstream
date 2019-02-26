package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

public interface StorageBackupSetup
{
	public StorageFileProvider backupFileProvider();
	
	public StorageFileWriter.Provider setupWriterProvider(
		StorageFileWriter.Provider writerProvider
	);
	
	public StorageBackupHandler setupHandler(
		StorageChannelController channelController
	);
	
	
	
	public static StorageBackupSetup New(
		final StorageFileProvider backupFileProvider
	)
	{
		return new StorageBackupSetup.Implementation(
			notNull(backupFileProvider),
			StorageBackupItemQueue.New()
		);
	}
	
	public final class Implementation implements StorageBackupSetup
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageFileProvider    backupFileProvider;
		private final StorageBackupItemQueue itemQueue         ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Implementation(
			final StorageFileProvider    backupFileProvider,
			final StorageBackupItemQueue itemQueue
		)
		{
			super();
			this.backupFileProvider = backupFileProvider;
			this.itemQueue          = itemQueue         ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final StorageFileProvider backupFileProvider()
		{
			return this.backupFileProvider;
		}
		
		@Override
		public StorageFileWriter.Provider setupWriterProvider(
			final StorageFileWriter.Provider writerProvider
		)
		{
			return StorageFileWriterBackupping.Provider(this.itemQueue, writerProvider);
		}
		
		@Override
		public StorageBackupHandler setupHandler(
			final StorageChannelController channelController
		)
		{
			final int channelCount = channelController.channelCountProvider().get();
			return StorageBackupHandler.New(this, channelCount, this.itemQueue, channelController);
		}
		
	}
	
}
