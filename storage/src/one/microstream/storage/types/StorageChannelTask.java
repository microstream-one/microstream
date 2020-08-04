package one.microstream.storage.types;

import one.microstream.storage.exceptions.StorageException;

public interface StorageChannelTask extends StorageTask
{
	public void incrementCompletionProgress();

	public void addProblem(int hashIndex, Throwable problem);



	// (26.11.2014 TM)TODO: consolidate task naming

	public abstract class Abstract<R>
	extends StorageTask.Abstract
	implements StorageChannelTask
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private          int         remainingForCompletion;
		private          int         remainingForProcessing;

		private volatile boolean     hasProblems;
		private final    Throwable[] problems   ; // unshared instance conveniently abused as a second lock



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Abstract(final long timestamp, final int channelCount)
		{
			super(timestamp);
			
			// (20.11.2019 TM)NOTE: inlined assignments caused an "Unsafe" error on an ARM machine. No kidding.
			this.remainingForProcessing = channelCount;
			this.remainingForCompletion = channelCount;
			this.problems = new Throwable[channelCount];
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void checkForProblems()
		{
			if(!this.hasProblems)
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
					// (09.09.2014 TM)EXCP: proper exception
					throw new StorageException("Problem in channel " + i, this.problems[i]);
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
		 * no matter what problems occured before.
		 */
		protected void cleanUp(final StorageChannel channel)
		{
			// no-op in general implementation
		}

		protected final int channelCount()
		{
			// a little bit of a hack, but rarely used, so it's better off that way
			return this.problems.length;
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized void incrementCompletionProgress()
		{
			// may never get negative or something is seriously broken
			this.remainingForCompletion--; // suffices as this method gets called by every manager thread exactely once.
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
				this.wait();
			}
			this.checkForProblems(); // check for problems after every channel reported completion
		}

		@Override
		public final boolean hasProblems()
		{
			return this.hasProblems;
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
			if(this.problems[hashIndex] == null)
			{
				this.problems[hashIndex] = problem;
				this.hasProblems = true;
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
				// suffices as this method gets called by every manager thread exactely once.
				this.remainingForProcessing--;
				this.problems.notifyAll();
			}
		}

		@Override
		public final void processBy(final StorageChannel storageChannel) throws InterruptedException
		{
			// separate outmost try-finally guarantees calling of clean up logic in any case
			try
			{
				final R result;
				try
				{
					result = this.internalProcessBy(storageChannel);
				}
				catch(final Throwable e)
				{
					// a problem occuring while processing gets reported and the task gets cleanly aborted.
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
