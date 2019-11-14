package one.microstream.storage.types;

@FunctionalInterface
public interface StorageChannelThreadProvider extends StorageThreadProviding
{
	/**
	 * Provides a newly created, yet unstarted {@link Thread} instance wrapping the passed
	 * {@link StorageChannel} instance.
	 * The thread will be used as an exclusive, permanent storage channel worker thread until the storage
	 * is shut down.
	 * Interfering with the thread from outside the storage compound has undefined and potentially
	 * unpredictable and erronous behavior.
	 *
	 * @return a {@link Thread} instance to be used as a storage channel worker thread.
	 */
	public default Thread provideChannelThread(final StorageChannel storageChannel)
	{
		return this.provideChannelThread(storageChannel, StorageThreadNameProvider.NoOp());
	}
	
	public Thread provideChannelThread(
		StorageChannel            storageChannel    ,
		StorageThreadNameProvider threadNameProvider
	);



	public final class Default implements StorageChannelThreadProvider
	{
		@Override
		public Thread provideChannelThread(
			final StorageChannel            storageChannel    ,
			final StorageThreadNameProvider threadNameProvider
		)
		{
			final String threadName = StorageChannel.class.getSimpleName() + "-" + storageChannel.channelIndex();
			
			return new Thread(
				storageChannel,
				threadNameProvider.provideThreadName(this, threadName)
			);
		}

	}

}
