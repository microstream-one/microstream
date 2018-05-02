package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;

public interface StorageChannelTaskTruncateData extends StorageRequestTask
{
	public final class Implementation
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageChannelTaskTruncateData
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageChannelController channelController;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final long                     timestamp        ,
			final int                      channelCount     ,
			final StorageChannelController channelController
		)
		{
			super(timestamp, channelCount);
			this.channelController = notNull(channelController);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			// truncate all data
			channel.truncateData();
			return null;
		}

		@Override
		protected final void succeed(final StorageChannel channel, final Void result)
		{
			// nothing to do on success. All channels are wiped clean and back in their initial state.
		}

		@Override
		protected final void fail(final StorageChannel channel, final Void result)
		{
			// if anything goes wrong, the storage must be shut down as it is in an undefined state.
			this.channelController.deactivate();

			// can / may never throw an exception
			channel.clear();
		}

	}

}
