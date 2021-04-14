package one.microstream.storage.types;


public interface StorageRequestTaskExportChannels extends StorageRequestTask
{
	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskExportChannels, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageLiveFileProvider fileProvider;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                timestamp   ,
			final int                 channelCount,
			final StorageLiveFileProvider fileProvider
		)
		{
			// every channel has to store at least a chunk header, so progress count is always equal to channel count
			super(timestamp, channelCount);
			this.fileProvider = fileProvider;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			channel.exportData(this.fileProvider);
			return null;
		}

	}

}
