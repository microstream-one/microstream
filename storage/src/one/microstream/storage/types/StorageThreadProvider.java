package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageThreadProvider extends StorageChannelThreadProvider, StorageBackupThreadProvider
{
	public static StorageThreadProvider New(
		final StorageChannelThreadProvider channelThreadProvider,
		final StorageBackupThreadProvider  backupThreadProvider
	)
	{
		return new StorageThreadProvider.Wrapper(
			notNull(channelThreadProvider),
			notNull(backupThreadProvider)
		);
	}

	public final class Wrapper implements StorageThreadProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageChannelThreadProvider channelThreadProvider;
		private final StorageBackupThreadProvider  backupThreadProvider ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Wrapper(
			final StorageChannelThreadProvider channelThreadProvider,
			final StorageBackupThreadProvider  backupThreadProvider
		)
		{
			super();
			this.channelThreadProvider = channelThreadProvider;
			this.backupThreadProvider = backupThreadProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Thread provideStorageThread(final StorageChannel storageChannel)
		{
			return this.channelThreadProvider.provideStorageThread(storageChannel);
		}

		@Override
		public final Thread provideBackupThread(final StorageBackupHandler backupHandler)
		{
			return this.backupThreadProvider.provideBackupThread(backupHandler);
		}

	}

}
