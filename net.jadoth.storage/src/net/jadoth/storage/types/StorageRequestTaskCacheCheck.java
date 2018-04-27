package net.jadoth.storage.types;


public interface StorageRequestTaskCacheCheck extends StorageRequestTask
{
	public boolean result();



	public final class Implementation
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskCacheCheck, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final StorageEntityCacheEvaluator entityEvaluator    ;
		final long                        nanoTimeBudgetBound;
		      boolean                     completed          ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final long                        timestamp          ,
			final int                         channelCount       ,
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			super(timestamp, channelCount);
			this.entityEvaluator     = entityEvaluator; // may be null
			this.nanoTimeBudgetBound = nanoTimeBudgetBound;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			this.completed = channel.issuedCacheCheck(this.nanoTimeBudgetBound, this.entityEvaluator);
			return null;
		}

		@Override
		public final boolean result()
		{
			return this.completed;
		}

	}

}
