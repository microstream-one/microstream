package net.jadoth.storage.types;

import net.jadoth.functional._longProcedure;
import net.jadoth.swizzling.types.Swizzle;


/**
 * Central instance serving as a locking instance (concurrency monitor) for concurrently marking entities.
 * Via the indirection over a pure OID (long primitives) mark queue, the actual marking, sweeping and concurrency
 * management associated with it is strictly thread local, like the rest of the storage implementation is.
 * Without that centralization and indirection, absolute concurrency correctness is hard to achieve and much more
 * coding effort.
 *
 * @author TM
 */
public interface StorageEntityMarkMonitor extends _longProcedure
{
	public void signalPendingStoreUpdate(StorageEntityCache<?> channel);

	public void resetCompletion();

	public void advanceMarking(StorageOidMarkQueue oidMarkQueue, int amount);

	public void clearPendingStoreUpdate(StorageEntityCache<?> channel);

	public boolean isComplete(StorageEntityCache<?> channel);

	public boolean needsSweep(StorageEntityCache<?> channel);

	public void completeSweep(
		StorageEntityCache<?>  channel        ,
		StorageRootOidSelector rootOidSelector,
		long                   channelRootOid
	);

	public boolean isMarkingComplete();

	public StorageReferenceMarker provideReferenceMarker(StorageEntityCache<?> channel);

//	public String DEBUG_state();



	public interface Creator
	{
		public StorageEntityMarkMonitor createEntityMarkMonitor(StorageOidMarkQueue[] oidMarkQueues);



		public final class Implementation implements StorageEntityMarkMonitor.Creator
		{
			@Override
			public StorageEntityMarkMonitor createEntityMarkMonitor(final StorageOidMarkQueue[] oidMarkQueues)
			{
				return new StorageEntityMarkMonitor.Implementation(oidMarkQueues.clone());
			}

		}

	}


	final class Implementation implements StorageEntityMarkMonitor, StorageReferenceMarker
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageOidMarkQueue[] oidMarkQueues          ;
		private final int                   channelCount           ;
		private final int                   channelHash            ;
		private       long                  pendingMarksCount      ;
		private final boolean[]             pendingStoreUpdates    ;
		private       int                   pendingStoreUpdateCount;

		private final boolean[]             needsSweep             ;
		private       int                   sweepingChannelCount   ;

		private final long[]                channelRootOids         ;

		/*
		 * Indicates that no new data (store) has been received since the last sweep.
		 * This basically means that no more gc marking or sweeping is necessary, however as stored entities
		 * (both newly created and updated) are forced gray, potentially any number of entities can be
		 * virtually doomed but still be kept alive. Those will only be found in a second mark and sweep since the
		 * last store.
		 * This flag can be seen as "no new data level 1".
		 */
		private boolean gcHotPhaseComplete = true; // GC is initially completed because there is no data at all

		/*
		 * Indicates that not only no new data has been received since the last sweep, but also that a second sweep
		 * has already been executed since then, removing all unreachable entities and effectively establishing
		 * a clean / optimized / stable state.
		 * This flag can be seen as "no new data level 2".
		 * It will shut off all GC activity until the next store resets the flags.
		 */
		private boolean gcColdPhaseComplete = true; // GC is initially completed because there is no data at all



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final StorageOidMarkQueue[] oidMarkQueues)
		{
			super();
			this.oidMarkQueues       = oidMarkQueues                 ;
			this.channelCount        = oidMarkQueues.length          ;
			this.channelHash         = this.channelCount - 1         ;
			this.pendingStoreUpdates = new boolean[this.channelCount];
			this.needsSweep          = new boolean[this.channelCount];
			this.channelRootOids     = new long   [this.channelCount];
		}

		private synchronized void incrementPendingMarksCount()
		{
			this.pendingMarksCount++;
		}

		@Override
		public final synchronized boolean isMarkingComplete()
		{
			return this.pendingMarksCount == 0 && this.pendingStoreUpdateCount == 0;
		}

		@Override
		public final synchronized void advanceMarking(final StorageOidMarkQueue oidMarkQueue, final int amount)
		{
//			DEBUGStorage.println(System.identityHashCode(oidMarkQueue) + " >-  " + this.pendingMarksCount + " " + oidMarkQueue.size());

			if(this.pendingMarksCount < amount)
			{
				// (07.07.2016 TM)EXCP: proper exception
				throw new RuntimeException(
					"pending marks count (" + this.pendingMarksCount +
					") is smaller than the number to be advanced (" + amount + ").");
			}

			/*
			 * Advance the oidMarkQueue not before the gc phase monitor has been locked and the amount has been validated.
			 * AND while the lock is held. Hence the channel must pass and update its queue instance in here, not outside.
			 */
			oidMarkQueue.advanceTail(amount);
			this.pendingMarksCount -= amount;

//			DEBUGStorage.println(System.identityHashCode(oidMarkQueue) + "  >-  " + this.pendingMarksCount + " " + oidMarkQueue.size());
		}

		@Override
		public final synchronized void signalPendingStoreUpdate(final StorageEntityCache<?> channel)
		{
			// check array to ensure idempotence
			if(!this.pendingStoreUpdates[channel.channelIndex()])
			{
				DEBUGStorage.println(channel.channelIndex() + " signals pending store update.");
				this.pendingStoreUpdates[channel.channelIndex()] = true;
				this.pendingStoreUpdateCount++;
			}
		}

		@Override
		public final synchronized void clearPendingStoreUpdate(final StorageEntityCache<?> channel)
		{
			// check array to ensure idempotence
			if(this.pendingStoreUpdates[channel.channelIndex()])
			{
				DEBUGStorage.println(channel.channelIndex() + " clears pending store update.");
				this.pendingStoreUpdates[channel.channelIndex()] = false;
				this.pendingStoreUpdateCount--;
			}
		}

		private synchronized void advanceGcCompletion()
		{
			if(this.gcColdPhaseComplete)
			{
				DEBUGStorage.println("GC already complete.");
				return;
			}

			if(this.gcHotPhaseComplete)
			{
				DEBUGStorage.println("Completed GC fully.");
				/*
				 * Note for debugging:
				 * For testing repeated GC runs, do NOT just deactivate the cold completion flag here.
				 * It will create a completion state inconcistency and thus a race condition in isComplete(),
				 * occasionally causing one channel to forever wait for itself while all others assumed
				 * completion via the hot phase + sweep count check.
				 * Nasty problem to find.
				 * To let the GC run repeatedly, modify the logic in isComplete() directly (always return false).
				 */
				this.gcColdPhaseComplete = true;
			}
			else
			{
				DEBUGStorage.println("Completed GC Hot Phase");
				this.gcHotPhaseComplete = true;
			}
		}

		private synchronized boolean callToSweepRequired()
		{
			// if there is already a sweep going on, no new sweep may be done
			if(this.sweepingChannelCount > 0)
			{
				return false;
			}

			// if no sweep is in progress, check if the marking is complete
			if(!this.isMarkingComplete())
			{
				return false;
			}

			DEBUGStorage.println("Marking complete.");

			// reset channel root ids board because channels will update it upon ecountering the need to sweep.
			this.resetChannelRootIds();

			// no current sweep and completed marking means a new seep has to be initiated. So do it.
			for(int i = 0; i < this.needsSweep.length; i++)
			{
				this.needsSweep[i] = true;
			}
			this.sweepingChannelCount = this.needsSweep.length;

			return true;
		}

		@Override
		public final synchronized boolean needsSweep(final StorageEntityCache<?> channel)
		{
			/*
			 * If there is a pending sweep to be executed by the passed (= calling) channel, then mark as done
			 * and return that sweep is required, directly causing a sweep in the calling method.
			 *
			 * Otherwise, check if the passed/calling channel/thread is the first to recognize
			 * the current marking is complete (no more pending oids to mark) and issue that a channel-wide sweep is required.
			 * If so, again the current channel marks its own sweep to be done and returns causing a sweep.
			 *
			 * If both checks yield false, no sweep is needed.
			 *
			 * Note that the actual timing of when the sweep is done or before or after other threads already marking again is irrelevant.
			 * What is relevant is the logical order:
			 * - A required sweep is ONLY issued if the marking is safely completed (lock-secured central count == 0 check)
			 * - The sweep check itself is lock-secured and central, a sweep cannot be issues or done twice.
			 * - After the count == 0 case is detected, every channel will exactely sweep once before it can go back to marking
			 * - The mark oid queue (long[]) does not in any way interfere with the sweeping (local Entry instances) or vice versa.
			 */
			return this.needsSweep[channel.channelIndex()] || this.callToSweepRequired();
		}

		@Override
		public final synchronized void completeSweep(
			final StorageEntityCache<?>  channel        ,
			final StorageRootOidSelector rootOidSelector,
			final long                   channelRootOid
		)
		{
			// register the channel's current valid root Oid after the performed sweep (potentially 0).
			this.channelRootOids[channel.channelIndex()] = channelRootOid;

			// mark this channel as having completed the sweep
			this.needsSweep[channel.channelIndex()] = false;

			// decrement sweep channel count and execute completion logic if required.
			if(--this.sweepingChannelCount == 0)
			{
				this.advanceGcCompletion();
				this.determineAndEnqueueRootOid(rootOidSelector);
			}
		}

		final synchronized void resetChannelRootIds()
		{
			for(int i = 0; i < this.channelRootOids.length; i++)
			{
				this.channelRootOids[i] = Swizzle.nullId();
			}
		}

		final synchronized void determineAndEnqueueRootOid(final StorageRootOidSelector rootOidSelector)
		{
			/*
			 * note that no lock on the selector instance is required because every channel thread
			 * brings his own exclusive instance and only uses it "in here" by itself.
			 */
			rootOidSelector.reset();
			for(int i = 0; i < this.channelRootOids.length; i++)
			{
				rootOidSelector.accept(this.channelRootOids[i]);
			}

			// at least one channel MUST have a non-null root oid, otherwise the whole database would be wiped.
			final long currentMaxRootOid = rootOidSelector.yield();
			if(currentMaxRootOid == Swizzle.nullId())
			{
				// (27.07.2016 TM)FIXME: what about the firct GC call in a new/empty DB?

				// (15.07.2016 TM)EXCP: proper exception
				throw new RuntimeException("No root oid could have been found.");
			}

//			DEBUGStorage.println(Thread.currentThread().getName() + " enqueuing root OID " + currentMaxRootOid);

			/*
			 * this initializes the next marking.
			 * From here on, pendingMarksCount can only be 0 again if marking is complete.
			 */
			this.accept(currentMaxRootOid);
		}

		@Override
		public final void accept(final long oid)
		{
			// do not enqueue null oids, not even get the lock
			if(oid == Swizzle.nullId())
			{
				return;
			}

			// no need to keep the lock longer than necessary or nested with the queue lock.
			this.incrementPendingMarksCount();
			this.oidMarkQueues[(int)(oid & this.channelHash)].enqueue(oid);
		}

		@Override
		public final boolean tryFlush()
		{
			// nothing to flush in a single-oid-enqueing implementation.
			return false;
		}

		@Override
		public final StorageReferenceMarker provideReferenceMarker(final StorageEntityCache<?> channel)
		{
			// (22.07.2016 TM)TODO: make constant dynamic
			return new CachingReferenceMarker(this, this.channelCount, 500);
		}

		final void enqueueBulk(final long[][] oidsPerChannel, final int[] sizes)
		{
			long totalSize = 0;
			for(final int size : sizes)
			{
				totalSize += size;
			}

			synchronized(this)
			{
				this.pendingMarksCount += totalSize;
			}

			final StorageOidMarkQueue[] oidMarkQueues = this.oidMarkQueues;

			// lock for every queue is only acquired once and all oids are enqueued efficiently
			for(int i = 0; i < oidsPerChannel.length; i++)
			{
				oidMarkQueues[i].enqueueBulk(oidsPerChannel[i], sizes[i]);
			}
		}

		@Override
		public final synchronized void resetCompletion()
		{
			this.gcHotPhaseComplete = this.gcColdPhaseComplete = false;
		}

		@Override
		public final synchronized boolean isComplete(final StorageEntityCache<?> channel)
		{
//			// only for testing
//			return false;

			/*
			 * GC is effectively complete if either:
			 * - the cold phase is complete (meaning nothing will/can change until the next store)
			 * - the hot phase (first sweep) is complete and the cold phase has only sweeps pending from other channels
			 * ! NOT if hot phase is completed and sweepingChannelCount is 0, because that applies to marking, too.
			 */
			return this.gcColdPhaseComplete
				|| this.gcHotPhaseComplete && this.sweepingChannelCount > 0 && !this.needsSweep[channel.channelIndex()]
			;
		}

		static final class CachingReferenceMarker implements StorageReferenceMarker
		{
			private final StorageEntityMarkMonitor.Implementation markMonitor        ;
			private final long[][]                                oidsPerChannel     ;
			private final int[]                                   oidsPerChannelSizes;
			private final int                                     channelHash        ;
			private final int                                     bufferLength       ;

			CachingReferenceMarker(
				final StorageEntityMarkMonitor.Implementation markMonitor ,
				final int                                     channelCount,
				final int                                     bufferLength
			)
			{
				super();
				this.markMonitor         = markMonitor             ;
				this.bufferLength        = bufferLength            ;
				this.channelHash         = channelCount - 1        ;
				this.oidsPerChannel      = new long[channelCount][];
				this.oidsPerChannelSizes = new int[channelCount]   ;

				for(int i = 0; i < channelCount; i++)
				{
					this.oidsPerChannel[i] = new long[bufferLength];
				}
			}

			@Override
			public final void accept(final long oid)
			{
				// do not enqueue null oids
				if(oid == Swizzle.nullId())
				{
					return;
				}

				final int i = (int)(oid & this.channelHash);

				this.oidsPerChannel[i][this.oidsPerChannelSizes[i]] = oid;
				if((this.oidsPerChannelSizes[i] = this.oidsPerChannelSizes[i] + 1) == this.bufferLength)
				{
					this.flush();
				}
			}

			final void flush()
			{
				this.markMonitor.enqueueBulk(this.oidsPerChannel, this.oidsPerChannelSizes);

				for(int i = 0; i < this.oidsPerChannelSizes.length; i++)
				{
					this.oidsPerChannelSizes[i] = 0;
				}
			}

			@Override
			public final boolean tryFlush()
			{
				for(int i = 0; i < this.oidsPerChannelSizes.length; i++)
				{
					if(this.oidsPerChannelSizes[i] != 0)
					{
						this.flush();
						return true;
					}
				}

				return false;
			}
		}

//		@Override
//		public synchronized String DEBUG_state()
//		{
//			// (19.07.2016 TM)NOTE: ultra hacky hardcoded multi-lock for 4 channels
//			synchronized(this.oidMarkQueues[0])
//			{
//				synchronized(this.oidMarkQueues[1])
//				{
//					synchronized(this.oidMarkQueues[2])
//					{
//						synchronized(this.oidMarkQueues[3])
//						{
//							final VarString vs = VarString.New("GC state");
//
//							vs
//							.lf().padLeft(Long.toString(this.pendingMarksCount), 10, ' ').add(" pending marks count")
//							;
////							for(int i = 0; i < this.oidMarkQueues.length; i++)
////							{
////								vs.lf().padLeft(Long.toString(this.oidMarkQueues[i].size()), 10, ' ').blank().add("in channel #"+i);
////							}
//
//							vs
//							.lf()
//							.lf().add("Hot  complete\t" + this.gcHotPhaseComplete)
//							.lf().add("Cold complete\t" + this.gcColdPhaseComplete)
//							.lf().add("Needs sweep (" + this.sweepingChannelCount+"):")
//							;
//							for(int i = 0; i < this.needsSweep.length; i++)
//							{
//								vs.lf().blank().add(i + ": " + this.needsSweep[i]);
//							}
//
//							vs
//							.lf().padLeft(Long.toString(this.pendingStoreUpdateCount), 10, ' ').blank().add("pending store updates")
//							;
//							for(int i = 0; i < this.pendingStoreUpdates.length; i++)
//							{
//								vs.lf().blank().add(i + ": " + this.pendingStoreUpdates[i]);
//							}
//
//							return vs.toString();
//						}
//					}
//				}
//			}
//		}

	}

}
