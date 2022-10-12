package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */



public interface StorageChannelSynchronizingTask extends StorageChannelTask
{
	public boolean isProcessed();

	public void incrementProcessingProgress();

	public void waitOnProcessing() throws InterruptedException;



	public abstract class AbstractCompletingTask<R>
	extends StorageChannelTask.Abstract<R>
	implements StorageChannelSynchronizingTask
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public AbstractCompletingTask(
			final long                       timestamp   ,
			final int                        channelCount, 
			final StorageOperationController controller
		)
		{
			super(timestamp, channelCount, controller);
		}


		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected void succeed(final StorageChannel channel, final R result)
		{
			// no-op by default. To be overridden only when needed.
		}

		protected void fail(final StorageChannel channel, final R result)
		{
			// no-op by default. To be overridden only when needed.
		}

		protected void postCompletionSuccess(final StorageChannel channel, final R result) throws InterruptedException
		{
			// no-op by default. To be overridden only when needed.
		}

		private void synchronizedComplete(final StorageChannel channel, final R result)
		{
			try
			{
				// handle success or failure
				if(this.hasProblems())
				{
					// any other thread's storing failed, so rollback own storing
					this.fail(channel, result);
				}
				else
				{
					this.succeed(channel, result);
				}
			}
			catch(final Throwable t)
			{
				this.addProblem(channel.channelIndex(), t);
			}
			finally
			{
				// must complete the task (signal calling thread) no matter the result (success or problem)
				this.incrementCompletionProgress();
			}
		}





		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected R internalProcessBy(final StorageChannel channel)
		{
			// no-op by default. To be overridden only when needed.
			return null;
		}

		@Override
		protected final void complete(final StorageChannel channel, final R result) throws InterruptedException
		{
			try
			{
				// wait for all other processing threads to report in before completing (e.g. committing a write)
				this.waitOnProcessing();
			}
			catch(final InterruptedException e)
			{
				/* Thread interrupted. Register problem, pass exception along
				 * but still care for consistency via finally block before leaving
				 */
				this.addProblem(channel.channelIndex(), e);
				throw e; // passing the interruption basically means terminating the channel work loop
			}
			finally
			{
				// actual completion logic after (timely) synchronizing with other threads (or after interruption)
				this.synchronizedComplete(channel, result);

				// post-completion logic that may not be subject to completion try-catch-finally
				if(!this.hasProblems())
				{
					this.postCompletionSuccess(channel, result);
				}
			}
		}


		public static final class Dummy extends AbstractCompletingTask<Void> implements StorageRequestTask
		{

			public Dummy(final int channelCount, StorageOperationController controller)
			{
				super(0, channelCount, controller);
			}

		}

	}

}
