package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageThreadProvider
extends StorageChannelThreadProvider, StorageBackupThreadProvider, StorageLockFileManagerThreadProvider
{
	public static StorageThreadProvider New(
		final StorageChannelThreadProvider         channelThreadProvider        ,
		final StorageBackupThreadProvider          backupThreadProvider         ,
		final StorageLockFileManagerThreadProvider lockFileManagerThreadProvider
	)
	{
		return new StorageThreadProvider.Wrapper(
			notNull(channelThreadProvider)        ,
			notNull(backupThreadProvider)         ,
			notNull(lockFileManagerThreadProvider)
		);
	}

	public final class Wrapper implements StorageThreadProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageChannelThreadProvider         channelThreadProvider        ;
		private final StorageBackupThreadProvider          backupThreadProvider         ;
		private final StorageLockFileManagerThreadProvider lockFileManagerThreadProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Wrapper(
			final StorageChannelThreadProvider         channelThreadProvider        ,
			final StorageBackupThreadProvider          backupThreadProvider         ,
			final StorageLockFileManagerThreadProvider lockFileManagerThreadProvider
		)
		{
			super();
			this.channelThreadProvider         = channelThreadProvider        ;
			this.backupThreadProvider          = backupThreadProvider         ;
			this.lockFileManagerThreadProvider = lockFileManagerThreadProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Thread provideChannelThread(final StorageChannel storageChannel)
		{
			return this.channelThreadProvider.provideChannelThread(storageChannel);
		}

		@Override
		public final Thread provideBackupThread(final StorageBackupHandler backupHandler)
		{
			return this.backupThreadProvider.provideBackupThread(backupHandler);
		}
		
		@Override
		public final Thread provideLockFileManagerThread(final StorageLockFileManager lockFileManager)
		{
			return this.lockFileManagerThreadProvider.provideLockFileManagerThread(lockFileManager);
		}

	}

}
