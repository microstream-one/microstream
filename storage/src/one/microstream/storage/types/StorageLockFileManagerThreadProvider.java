package one.microstream.storage.types;

@FunctionalInterface
public interface StorageLockFileManagerThreadProvider
{
	/**
	 * Provides a newly created, yet unstarted {@link Thread} instance wrapping the passed
	 * {@link StorageLockFileManager} instance.
	 * The thread will be used as an exclusive, permanent lock file validator and updater worker thread
	 * until the storage is shut down.
	 * Interfering with the thread from outside the storage compound has undefined and potentially
	 * unpredictable and erronous behavior.
	 *
	 * @return a {@link Thread} instance to be used as a storage lock file managing worker thread.
	 */
	public Thread provideLockFileManagerThread(StorageLockFileManager lockFileManager);
	

	
	public static StorageLockFileManagerThreadProvider New()
	{
		return new StorageLockFileManagerThreadProvider.Default();
	}

	public final class Default implements StorageLockFileManagerThreadProvider
	{
		Default()
		{
			super();
		}
		
		@Override
		public Thread provideLockFileManagerThread(final StorageLockFileManager lockFileManager)
		{
			return new Thread(
				lockFileManager,
				StorageLockFileManager.class.getSimpleName()
			);
		}

	}

}
