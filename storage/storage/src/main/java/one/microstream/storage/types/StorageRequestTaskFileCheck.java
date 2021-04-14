package one.microstream.storage.types;


public interface StorageRequestTaskFileCheck extends StorageRequestTask
{
	public boolean result();



	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskFileCheck, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long    nanoTimeBudget;
		      boolean completed     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long timestamp     ,
			final int  channelCount  ,
			final long nanoTimeBudget
		)
		{
			super(timestamp, channelCount);
			this.nanoTimeBudget = nanoTimeBudget;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			this.completed = channel.issuedFileCleanupCheck(this.nanoTimeBudget);
			return null;
		}

		@Override
		public final boolean result()
		{
			return this.completed;
		}

	}

}
