package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageChannelTaskShutdown extends StorageChannelTask
{
	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageChannelTaskShutdown
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageOperationController operationController;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final long                       timestamp          ,
			final int                        channelCount       ,
			final StorageOperationController operationController
		)
		{
			super(timestamp, channelCount);
			this.operationController = notNull(operationController);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			// may not deactivate here as some channel threads would die before all others notice the progress
			return null;
		}

		@Override
		protected final void succeed(final StorageChannel channel, final Void result)
		{
			/* (01.07.2015 TM)FIXME: Shutdown lets "remainingForCompletion" remain at full channel count,
			 * thus letting the calling thread (main) wait forever
			 * thus preventing the program from terminating.
			 */
			/* (07.07.2016 TM)FIXME: Shutdown must properly handle completion notification
			 * so that the issuing shutdown method waits for the shutdown to actually complete.
			 */

			// may not be done before to give every channel a safe way to notice the processing progress
			this.operationController.deactivate();

			// can / may never throw an exception
			channel.reset();
		}

		@Override
		protected final void fail(final StorageChannel channel, final Void result)
		{
			// nothing to do here
		}

	}

}
