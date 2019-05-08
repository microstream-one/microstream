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

		final StorageDataFileDissolvingEvaluator fileDissolver      ;
		final long                               nanoTimeBudgetBound;
		      boolean                            completed          ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                               timestamp          ,
			final int                                channelCount       ,
			final long                               nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolver
		)
		{
			super(timestamp, channelCount);
			this.fileDissolver       = fileDissolver      ; // may be null
			this.nanoTimeBudgetBound = nanoTimeBudgetBound;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			this.completed = channel.issuedFileCheck(this.nanoTimeBudgetBound, this.fileDissolver);
			return null;
		}

		@Override
		public final boolean result()
		{
			return this.completed;
		}

	}

}
