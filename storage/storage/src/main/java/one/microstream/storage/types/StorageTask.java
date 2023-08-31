package one.microstream.storage.types;

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
				this.wait(waitTime);
			}
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
