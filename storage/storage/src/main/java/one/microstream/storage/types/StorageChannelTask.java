package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
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

import one.microstream.storage.exceptions.StorageException;
import one.microstream.util.logging.Logging;

public interface StorageChannelTask extends StorageTask
{
	public void incrementCompletionProgress();

	public void addProblem(int hashIndex, Throwable problem);



	// (26.11.2014 TM)TODO: consolidate task naming

	public abstract class Abstract<R>
	extends StorageTask.Abstract
	implements StorageChannelTask
	{
		private final static Logger logger = Logging.getLogger(StorageChannelTask.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private int remainingForCompletion;
		private int remainingForProcessing;

		private final AtomicBoolean hasProblems = new AtomicBoolean();
		private final Throwable[]   problems   ; // unshared instance conveniently abused as a second lock
		
		/* (07.03.2022 TM)NOTE:
		 * Retrofitted to fix #285
		 * While it seems more reasonable at first to check for disruptions in a passed context instance,
		 * the static helper StorageRequestAcceptor#waitOnTask makes this approach a little tricky.
		 * Also, since the change from the channel-based architecture to the cell-based architecture
		 * will make all this cross-channel problem checking unnecessary in the future, this solution here
		 * is acceptable for the time being.
		 */
		protected final StorageOperationController controller ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Abstract(
			final long                       timestamp   ,
			final int                        channelCount,
			final StorageOperationController controller
		)
		{
			super(timestamp);
			
			// (20.11.2019 TM)NOTE: inlined assignments caused an "Unsafe" error on an ARM machine.
			this.remainingForProcessing = channelCount               ;
			this.remainingForCompletion = channelCount               ;
			this.controller             = notNull(controller)        ;
			this.problems               = new Throwable[channelCount];
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void checkForProblems()
		{
			if(this.controller.hasDisruptions())
			{
				throw new StorageException("Aborting after: ", this.controller.disruptions().first());
			}
						
			if(!this.hasProblems.get())
			{
				return;
			}
									
			// (30.05.2013 TM)FIXME: check why this is never reached when task fails?
			// (15.06.2013 TM)NOTE: should be fixed by double check in waitOnCompletion()
			// (09.12.2019 TM)NOTE: still needs to be investigated.
			for(int i = 0; i < this.problems.length; i++)
			{
				if(this.problems[i] != null)
				{
					throw new StorageException("Problem in channel #" + i, this.problems[i]);
				}
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected abstract R internalProcessBy(StorageChannel channel);

		protected abstract void complete(StorageChannel channel,  R value) throws InterruptedException;

		protected void finishProcessing()
		{
			// must notify other threads about progress even in case of error
			this.incrementProcessingProgress();
		}

		/* ultimate completion that has to be done in any case (resource closing etc.),
		 * no matter what problems occurred before.
		 */
		protected void cleanUp(final StorageChannel channel)
		{
			// no-op in general implementation
		}

		protected final int channelCount()
		{
			// a bit of a hack, but rarely used, so it's better off that way
			return this.problems.length;
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized void incrementCompletionProgress()
		{
			// may never get negative or something is seriously broken
			this.remainingForCompletion--; // suffices as this method gets called by every manager thread exactly once.
			this.notifyAll();
		}

		@Override
		public final synchronized boolean isComplete()
		{
			return this.remainingForCompletion == 0;
		}

		@Override
		public final synchronized void waitOnCompletion() throws InterruptedException
		{
			while(this.remainingForCompletion > 0)
			{
				this.checkForProblems(); // check for problems already while waiting
				this.wait(100);
			}
			this.checkForProblems(); // check for problems after every channel reported completion
		}

		@Override
		public final boolean hasProblems()
		{
			return this.hasProblems.get();
		}

		@Override
		public final Throwable[] problems()
		{
			return this.problems;
		}

		@Override
		public final Throwable problemForChannel(final StorageChannel channel)
		{
			return this.problems[channel.channelIndex()];
		}

		@Override
		public final void addProblem(final int hashIndex, final Throwable problem)
		{
			logger.error("Error occurred in storage channel#{}", hashIndex, problem);
			
			if(this.problems[hashIndex] == null)
			{
				this.problems[hashIndex] = problem;
				this.hasProblems.set(true);
			}
			else
			{
				this.problems[hashIndex].addSuppressed(problem);
			}
		}

		public final boolean isProcessed()
		{
			synchronized(this.problems)
			{
				return this.remainingForProcessing == 0;
			}
		}

		public final void waitOnProcessing() throws InterruptedException
		{
			synchronized(this.problems)
			{
				while(this.remainingForProcessing > 0)
				{
					this.problems.wait();
				}
			}
		}

		public final void incrementProcessingProgress()
		{
			synchronized(this.problems)
			{
				// may never get negative or something is seriously broken
				// suffices as this method gets called by every manager thread exactly once.
				this.remainingForProcessing--;
				this.problems.notifyAll();
			}
		}

		@Override
		public final void processBy(final StorageChannel storageChannel) throws InterruptedException
		{
			// separate outermost try-finally guarantees calling of clean up logic in any case
			try
			{
				final R result;
				try
				{
					result = this.internalProcessBy(storageChannel);
				}
				catch(final Throwable e)
				{
					// a problem occurring while processing gets reported and the task gets cleanly aborted.
					this.addProblem(storageChannel.channelIndex(), e);
					this.incrementCompletionProgress();
					return;
				}
				finally
				{
					// processing is finishing in any case (e.g. notifying other thread about the task's progress)
					this.finishProcessing();
				}

				// task gets completed (must be done after finishing the processing)
				this.complete(storageChannel, result);
			}
			finally
			{
				this.cleanUp(storageChannel);
			}

		}

	}

}
