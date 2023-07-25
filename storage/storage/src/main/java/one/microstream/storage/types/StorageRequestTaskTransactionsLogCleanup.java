package one.microstream.storage.types;

public interface StorageRequestTaskTransactionsLogCleanup extends StorageRequestTask
{
	public boolean result();



	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskTransactionsLogCleanup, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

	    boolean completed;

	    
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                       timestamp     ,
			final int                        channelCount  ,
			final StorageOperationController controller
		)
		{
			super(timestamp, channelCount, controller);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			this.completed = channel.issuedTransactionsLogCleanup();
			return null;
		}

		@Override
		public final boolean result()
		{
			return this.completed;
		}

	}

}
