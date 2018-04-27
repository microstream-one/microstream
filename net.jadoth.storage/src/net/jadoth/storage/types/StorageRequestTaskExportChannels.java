package net.jadoth.storage.types;


public interface StorageRequestTaskExportChannels extends StorageRequestTask
{
	public final class Implementation
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskExportChannels, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageIoHandler fileHandler;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final long               timestamp   ,
			final int                channelCount,
			final StorageIoHandler fileHandler
		)
		{
			// every channel has to store at least a chunk header, so progress count is always equal to channel count
			super(timestamp, channelCount);
			this.fileHandler = fileHandler;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			channel.exportData(this.fileHandler);
			return null;
		}

	}

}
