package one.microstream.storage.types;

public interface StorageChannelTaskShutdown extends StorageChannelTask
{
	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageChannelTaskShutdown
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final long                       timestamp          ,
			final int                        channelCount       ,
			final StorageOperationController operationController
		)
		{
			super(timestamp, channelCount, operationController);
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
