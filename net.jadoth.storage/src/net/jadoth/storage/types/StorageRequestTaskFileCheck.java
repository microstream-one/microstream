package net.jadoth.storage.types;


public interface StorageRequestTaskFileCheck extends StorageRequestTask
{
	public boolean result();



	public final class Implementation
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskFileCheck, StorageChannelTaskSaveEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final StorageDataFileDissolvingEvaluator fileDissolver      ;
		final long                               nanoTimeBudgetBound;
		      boolean                            completed          ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
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
		// override methods //
		/////////////////////

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
