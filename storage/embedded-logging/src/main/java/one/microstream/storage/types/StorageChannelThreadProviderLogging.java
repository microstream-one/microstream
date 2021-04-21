package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageChannelThreadProviderLogging
	extends StorageChannelThreadProvider, StorageLoggingWrapper<StorageChannelThreadProvider>
{
	public static StorageChannelThreadProviderLogging New(final StorageChannelThreadProvider wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
	public static class Default
		extends StorageLoggingWrapper.Abstract<StorageChannelThreadProvider>
		implements StorageChannelThreadProviderLogging
	{
		protected Default(final StorageChannelThreadProvider wrapped)
		{
			super(wrapped);
		}

		@Override
		public Thread provideChannelThread(final StorageChannel storageChannel)
		{
			return this.wrapped().provideChannelThread(storageChannel);
		}

		@Override
		public Thread provideChannelThread(final StorageChannel storageChannel, final StorageThreadNameProvider threadNameProvider)
		{
			this.logger().storageThreadProvider_beforeProvideChannelThread(storageChannel, threadNameProvider);
			
			final Thread thread = this.wrapped().provideChannelThread(storageChannel, threadNameProvider);
			
			this.logger().storageThreadProvider_afterProvideChannelThread(storageChannel, thread);
			
			return thread;
		}
		
	}
}
