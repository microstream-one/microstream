package one.microstream.storage.types;


public interface StorageRequestTaskGarbageCollection extends StorageRequestTask
{
	public boolean result();



	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Boolean>
	implements StorageRequestTaskGarbageCollection, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageTask actualTask    ;
		private final long        nanoTimeBudget;
		private       boolean     completed     ;




		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long        timestamp     ,
			final int         channelCount  ,
			final long        nanoTimeBudget,
			final StorageTask actualTask
		)
		{
			super(timestamp, channelCount);
			this.actualTask     = actualTask    ;
			this.nanoTimeBudget = nanoTimeBudget;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Boolean internalProcessBy(final StorageChannel channel)
		{
			// returns true if nothing to do or completed sweep (=done), false otherwise (in marking phase)
			return channel.issuedGarbageCollection(this.nanoTimeBudget);
		}

		private synchronized void setActualTask() // must be synchronized to set exactely only once for all channels
		{
			// (16.09.2014 TM)NOTE: changed from "!=" this.actualTask to " == ". Ought to be typo.
			if(this.next() != null && this.next() == this.actualTask)
			{
				return; // already set by another channel
			}
			this.setNext(this.actualTask);
		}

		@Override
		protected final void succeed(final StorageChannel channel, final Boolean completedSweep)
		{
			this.completed = true;
			if(this.actualTask != null)
			{
				this.setActualTask();
			}
		}

		@Override
		protected final void fail(final StorageChannel channel, final Boolean result)
		{
			// nothing to do here
		}

		@Override
		public final boolean result()
		{
			return this.completed;
		}

	}

}
