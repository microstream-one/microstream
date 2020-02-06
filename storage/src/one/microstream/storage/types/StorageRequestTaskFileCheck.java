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

		final long    nanoTimeBudgetBound;
		      boolean completed          ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                               timestamp          ,
			final int                                channelCount       ,
			final long                               nanoTimeBudgetBound
		)
		{
			super(timestamp, channelCount);
			this.nanoTimeBudgetBound = nanoTimeBudgetBound;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			this.completed = channel.issuedFileCheck(this.nanoTimeBudgetBound);
			return null;
		}

		@Override
		public final boolean result()
		{
			return this.completed;
		}

	}

}
