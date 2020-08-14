package one.microstream.storage.types;


public interface StorageRequestTaskCacheCheck extends StorageRequestTask
{
	public boolean result();



	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskCacheCheck, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final StorageEntityCacheEvaluator entityEvaluator;
		final long                        nanoTimeBudget ;
		      boolean                     completed      ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                        timestamp      ,
			final int                         channelCount   ,
			final long                        nanoTimeBudget ,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			super(timestamp, channelCount);
			this.entityEvaluator = entityEvaluator; // may be null
			this.nanoTimeBudget  = nanoTimeBudget;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			this.completed = channel.issuedEntityCacheCheck(this.nanoTimeBudget, this.entityEvaluator);
			return null;
		}

		@Override
		public final boolean result()
		{
			return this.completed;
		}

	}

}
