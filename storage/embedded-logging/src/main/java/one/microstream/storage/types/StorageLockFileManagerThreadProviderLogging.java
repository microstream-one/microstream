package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageLockFileManagerThreadProviderLogging
	extends StorageLockFileManagerThreadProvider, StorageLoggingWrapper<StorageLockFileManagerThreadProvider>
{

	static StorageLockFileManagerThreadProviderLogging New(final StorageLockFileManagerThreadProvider wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
	public static class Default
		extends StorageLoggingWrapper.Abstract<StorageLockFileManagerThreadProvider>
		implements StorageLockFileManagerThreadProviderLogging
	{
		protected Default(final StorageLockFileManagerThreadProvider wrapped)
		{
			super(wrapped);
		}

		@Override
		public Thread provideLockFileManagerThread(final StorageLockFileManager lockFileManager)
		{
			return this.wrapped().provideLockFileManagerThread(lockFileManager);
		}

		@Override
		public Thread provideLockFileManagerThread(final StorageLockFileManager lockFileManager,
			final StorageThreadNameProvider threadNameProvider)
		{
			this.logger().storageLockFileManagerThreadProvider_beforeProvideLockFileManagerThread(lockFileManager, threadNameProvider);
			
			final Thread thread = this.wrapped().provideLockFileManagerThread(lockFileManager, threadNameProvider);
			
			this.logger().storageLockFileManager_afterProvideLockFileManagerThread(lockFileManager, thread);
			
			return thread;
		}
	}

}
