package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageBackupThreadProviderLogging
	extends StorageBackupThreadProvider, StorageLoggingWrapper<StorageBackupThreadProvider>
{

	static StorageBackupThreadProviderLogging New(final StorageBackupThreadProvider wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
	public static class Default
		extends StorageLoggingWrapper.Abstract<StorageBackupThreadProvider>
		implements StorageBackupThreadProviderLogging
	{
		protected Default(final StorageBackupThreadProvider wrapped)
		{
			super(wrapped);
		}

		@Override
		public Thread provideBackupThread(final StorageBackupHandler backupHandler)
		{
			return this.wrapped().provideBackupThread(backupHandler);
		}

		@Override
		public Thread provideBackupThread(final StorageBackupHandler backupHandler,
			final StorageThreadNameProvider threadNameProvider)
		{
			this.logger().storageBackupThreadProvider_beforeProvideBackupThread(backupHandler, threadNameProvider);
			
			final Thread thread = this.wrapped().provideBackupThread(backupHandler, threadNameProvider);
			
			this.logger().storageBackupThreadProvider_afterProvideBackupThread(backupHandler, thread);
			
			return thread;
		}
		
	}

}
