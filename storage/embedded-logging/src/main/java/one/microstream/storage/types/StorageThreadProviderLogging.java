package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageThreadProviderLogging
 extends StorageThreadProvider, StorageLoggingWrapper<StorageThreadProvider>
{

	static StorageThreadProviderLogging New(final StorageThreadProvider wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
	public static class Default
		extends StorageLoggingWrapper.Abstract<StorageThreadProvider>
		implements StorageThreadProviderLogging
	{
		protected Default(final StorageThreadProvider wrapped)
		{
			super(wrapped);
		}
		
		@Override
		public Thread provideBackupThread(final StorageBackupHandler backupHandler)
		{
			return this.wrapped().provideBackupThread(backupHandler);
		}

		@Override
		public Thread provideChannelThread(final StorageChannel storageChannel)
		{
			return this.wrapped().provideChannelThread(storageChannel);
		}

		@Override
		public Thread provideLockFileManagerThread(final StorageLockFileManager lockFileManager)
		{
			return this.wrapped().provideLockFileManagerThread(lockFileManager);
		}

		@Override
		public Thread provideBackupThread(final StorageBackupHandler backupHandler,
			final StorageThreadNameProvider threadNameProvider)
		{
			return this.wrapped().provideBackupThread(backupHandler, threadNameProvider);
		}

		@Override
		public Thread provideChannelThread(final StorageChannel storageChannel, final StorageThreadNameProvider threadNameProvider)
		{
			return this.wrapped().provideChannelThread(storageChannel, threadNameProvider);
		}

		@Override
		public Thread provideLockFileManagerThread(final StorageLockFileManager lockFileManager,
			final StorageThreadNameProvider threadNameProvider)
		{
			return this.wrapped().provideLockFileManagerThread(lockFileManager, threadNameProvider);
		}
		
		
	}

}
