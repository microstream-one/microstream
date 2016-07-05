package net.jadoth.storage.types;


/**
 * Type to listen for all writes to the storage by type. Can be used to implement an on the fly backup handler.
 *
 * @author TM
 */
public interface StorageWriteListener
{
	public void registerStore(StorageDataFile<?> dataFile, long offset, long length);

	public void registerTransfer(StorageDataFile<?> dataFile, long offset, long length);

	public void registerDelete(StorageDataFile<?> dataFile);

	public void registerCreate(StorageDataFile<?> dataFile);

	public void registerTruncate(int channelIndex);

	/**
	 * Starts any active threads required for performing the backup task.
	 * Returning from this method guarantees that all required threads are live and running.
	 *
	 * @return this.
	 */
	public StorageWriteListener start();

	/**
	 * Causes the bachup handler to stop accepting new backup items and terminate all active threads eventually.
	 * Returning from this methode does NOT guarantee that all live threads are terminated.
	 * See {@link #forceStop()} for that.
	 *
	 * @return this.
	 */
	public StorageWriteListener stop();

	/**
	 * Same as {@link #stop()}, except the backup handler is urged to stop as soon as possible instead of
	 * processing remaining backup items.
	 * Returning from this methode guarantees that all live threads are terminated.
	 *
	 * @return this.
	 */
	public StorageWriteListener forceStop();



	public interface Provider
	{
		public StorageWriteListener provideWriteListener(int channelCount);



		public final class Implementation implements Provider
		{

			@Override
			public StorageWriteListener provideWriteListener(final int channelCount)
			{
				return new StorageWriteListener.Dummy();
			}

		}

	}


	public final class Dummy implements StorageWriteListener
	{

		@Override
		public void registerStore(final StorageDataFile<?> dataFile, final long offset, final long length)
		{
			// no-op
		}

		@Override
		public void registerTransfer(final StorageDataFile<?> dataFile, final long offset, final long length)
		{
			// no-op
		}

		@Override
		public void registerDelete(final StorageDataFile<?> dataFile)
		{
			// no-op
		}

		@Override
		public void registerCreate(final StorageDataFile<?> dataFile)
		{
			// no-op
		}

		@Override
		public void registerTruncate(final int channelIndex)
		{
			// no-op
		}

		@Override
		public StorageWriteListener start()
		{
			// no-op
			return this;
		}

		@Override
		public StorageWriteListener stop()
		{
			// no-op
			return this;
		}

		@Override
		public StorageWriteListener forceStop()
		{
			// no-op
			return this;
		}

	}

}
