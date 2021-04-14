package one.microstream.storage.types;

import one.microstream.storage.exceptions.StorageException;

public interface StorageTask
{
	public void setNext(StorageTask saveChunkEntry);

	public StorageTask awaitNext(long ms) throws InterruptedException;

	public StorageTask next();

	public void processBy(StorageChannel storageChannel) throws InterruptedException;

	public boolean isComplete();

	public void waitOnCompletion() throws InterruptedException;

	public boolean hasProblems();

	public Throwable[] problems();

	public Throwable problemForChannel(StorageChannel channel);

	public long timestamp();



	public abstract class Abstract implements StorageTask
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private volatile StorageTask next;

		private final long timestamp;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Abstract(final long timestamp)
		{
			super();
			this.timestamp = timestamp;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized StorageTask awaitNext(final long ms) throws InterruptedException
		{
			final long targetTime = System.currentTimeMillis() + ms;

			long waitTime;
			// if no immediate next task is available, wait for it a little, but then switch back to do housekeeping
			while(this.next == null && (waitTime = targetTime - System.currentTimeMillis()) > 0)
			{
//				XDebug.debugln(
//					Thread.currentThread().getName() + " waiting for next task " + waitTime
//					+ " @" + System.currentTimeMillis()
//				);
				this.wait(waitTime);
			}
//			XDebug.debugln(Thread.currentThread().getName() + " done waiting");
			return this.next;
		}

		@Override
		public final StorageTask next()
		{
			return this.next;
		}

		@Override
		public final void setNext(final StorageTask next)
		{
			if(this.next != null)
			{
				throw new StorageException("next task already assigned: " + this + " -> " + this.next);
			}
			this.next = next;
		}

		@Override
		public final long timestamp()
		{
			return this.timestamp;
		}

	}

	public final class DummyTask extends StorageTask.Abstract
	{
		DummyTask()
		{
			super(0);
		}

		@Override
		public final boolean isComplete()
		{
			return true;
		}

		@Override
		public final boolean hasProblems()
		{
			return false;
		}

		@Override
		public final Throwable[] problems()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final Throwable problemForChannel(final StorageChannel channel)
		{
			return this.problems()[channel.channelIndex()];
		}

		@Override
		public final void waitOnCompletion() throws InterruptedException
		{
			// no-op, i.e. instantly complete
		}

		@Override
		public void processBy(final StorageChannel storageChannel) throws InterruptedException
		{
			// no-op
		}

	}

}
