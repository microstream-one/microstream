package one.microstream.storage.types;

@FunctionalInterface
public interface StorageBackupThreadProvider
{
	/**
	 * Provides a newly created, yet unstarted {@link Thread} instance wrapping the passed
	 * {@link StorageBackupHandler} instance.
	 * The thread will be used as an exclusive, permanent backup worker thread until the storage
	 * is shut down.
	 * Interfering with the thread from outside the storage compound has undefined and potentially
	 * unpredictable and erronous behavior.
	 *
	 * @return a {@link Thread} instance to be used as a storage backup worker thread.
	 */
	public Thread provideBackupThread(StorageBackupHandler backupHandler);


	
	public static StorageBackupThreadProvider New()
	{
		return new StorageBackupThreadProvider.Default();
	}

	public final class Default implements StorageBackupThreadProvider
	{
		Default()
		{
			super();
		}
		
		@Override
		public Thread provideBackupThread(final StorageBackupHandler backupHandler)
		{
			return new Thread(
				backupHandler,
				StorageBackupHandler.class.getSimpleName()
			);
		}

	}

}
