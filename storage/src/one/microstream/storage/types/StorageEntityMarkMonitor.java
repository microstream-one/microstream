package one.microstream.storage.types;

import java.util.function.Supplier;

import one.microstream.chars.VarString;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;


/**
 * Central instance serving as a locking instance (concurrency monitor) for concurrently marking entities.
 * Via the indirection over a pure OID (long primitives) mark queue, the actual marking, sweeping and concurrency
 * management associated with it is strictly thread local, like the rest of the storage implementation is.
 * Without that centralization and indirection, absolute concurrency correctness is hard to achieve and much more
 * coding effort.
 *
 * @author TM
 */
public interface StorageEntityMarkMonitor extends PersistenceObjectIdAcceptor
{
	public void signalPendingStoreUpdate(StorageEntityCache<?> channel);

	public void resetCompletion();

	public void advanceMarking(StorageobjectIdMarkQueue objectIdMarkQueue, int amount);

	public void clearPendingStoreUpdate(StorageEntityCache<?> channel);

	public boolean isComplete(StorageEntityCache<?> channel);

	public boolean needsSweep(StorageEntityCache<?> channel);

	public boolean isPendingSweep(StorageEntityCache<?> channel);

	public void completeSweep(
		StorageEntityCache<?>  channel             ,
		StorageRootOidSelector rootObjectIdSelector,
		long                   channelRootObjectId
	);

	public boolean isMarkingComplete();

	public StorageReferenceMarker provideReferenceMarker(StorageEntityCache<?> channel);

	public void enqueue(StorageobjectIdMarkQueue objectIdMarkQueue, long objectId);

//	public String DEBUG_state();



	public interface Creator
	{
		public StorageEntityMarkMonitor createEntityMarkMonitor(StorageobjectIdMarkQueue[] oidMarkQueues);



		public final class Default implements StorageEntityMarkMonitor.Creator
		{
			@Override
			public StorageEntityMarkMonitor createEntityMarkMonitor(final StorageobjectIdMarkQueue[] objectIdMarkQueues)
			{
				return new StorageEntityMarkMonitor.Default(objectIdMarkQueues.clone());
			}

		}

	}


	final class Default implements StorageEntityMarkMonitor, StorageReferenceMarker
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageobjectIdMarkQueue[] oidMarkQueues          ;
		private final int                   channelCount           ;
		private final int                   channelHash            ;
		private       long                  pendingMarksCount      ;
		private final boolean[]             pendingStoreUpdates    ;
		private       int                   pendingStoreUpdateCount;

		private final boolean[]             needsSweep             ;
		private       int                   sweepingChannelCount   ;

		private final long[]                channelRootOids         ;

		private long sweepGeneration     ;
		private long lastSweepStart      ;
		private long lastSweepEnd        ;
		private long gcHotGeneration     ;
		private long gcColdGeneration    ;
		private long lastGcHotCompletion ;
		private long lastGcColdCompletion;

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

		Default(final StorageobjectIdMarkQueue[] oidMarkQueues)
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
		public final synchronized void advanceMarking(final StorageobjectIdMarkQueue oidMarkQueue, final int amount)
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
			 * Advance the oidMarkQueue not before the mark monitor has been locked and the amount has been validated.
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
//				DEBUGStorage.println(channel.channelIndex() + " signals pending store update.");
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
//				DEBUGStorage.println(channel.channelIndex() + " clears pending store update.");
				this.pendingStoreUpdates[channel.channelIndex()] = false;
				this.pendingStoreUpdateCount--;
			}
		}

		private synchronized void advanceGcCompletion()
		{
			if(this.gcColdPhaseComplete)
			{
				DebugStorage.println("GC already complete.");
				return;
			}

			if(this.gcHotPhaseComplete)
			{
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
				this.lastGcColdCompletion = System.currentTimeMillis();
				this.gcColdGeneration++;
				DebugStorage.println("Completed GC #" + this.gcColdGeneration + " @ " + this.lastGcColdCompletion);
			}
			else
			{
				this.gcHotPhaseComplete = true;
				this.lastGcHotCompletion = System.currentTimeMillis();
				this.gcHotGeneration++;
				DebugStorage.println("Completed GC Hot Phase #" + this.gcHotGeneration + " @ " + this.lastGcHotCompletion);
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

			this.lastSweepStart = System.currentTimeMillis();

//			DEBUGStorage.println("Marking complete.");

			// reset channel root ids board because channels will update it upon ecountering the need to sweep.
			this.resetChannelRootIds();

			/*
			 * This is the (lock-secured) only time where it is guaranteed that all mark queues are empty.
			 * So reset them to free memory occupied by the last mark.
			 */
			this.resetMarkQueues();

			// no current sweep and completed marking means a new sweep has to be initiated.
			this.initiateSweep();

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
			return this.isPendingSweep(channel) || this.callToSweepRequired();
		}

		@Override
		public final synchronized boolean isPendingSweep(final StorageEntityCache<?> channel)
		{
			return this.needsSweep[channel.channelIndex()];
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
			
			DebugStorage.println(channel.channelIndex() + " completed sweeping.");

			// decrement sweep channel count and execute completion logic if required.
			if(--this.sweepingChannelCount == 0)
			{
				this.lastSweepEnd = System.currentTimeMillis();
				this.sweepGeneration++;
				this.advanceGcCompletion();
				this.determineAndEnqueueRootOid(rootOidSelector);
			}
		}

		final synchronized void resetChannelRootIds()
		{
			for(int i = 0; i < this.channelRootOids.length; i++)
			{
				this.channelRootOids[i] = Persistence.nullId();
			}
		}

		final synchronized void resetMarkQueues()
		{
			for(int i = 0; i < this.oidMarkQueues.length; i++)
			{
				if(this.oidMarkQueues[i].hasElements())
				{
					throw new RuntimeException(); // (01.08.2016 TM)EXCP: proper exception
				}
				this.oidMarkQueues[i].reset();
			}
		}

		final synchronized void initiateSweep()
		{
			for(int i = 0; i < this.needsSweep.length; i++)
			{
				this.needsSweep[i] = true;
			}
			this.sweepingChannelCount = this.needsSweep.length;
		}

		final synchronized void determineAndEnqueueRootOid(final StorageRootOidSelector rootObjectIdSelector)
		{
			/*
			 * note that no lock on the selector instance is required because every channel thread
			 * brings his own exclusive instance and only uses it "in here" by itself.
			 */
			rootObjectIdSelector.resetGlobal();
			for(int i = 0; i < this.channelRootOids.length; i++)
			{
				rootObjectIdSelector.acceptGlobal(this.channelRootOids[i]);
			}

			// at least one channel MUST have a non-null root oid, otherwise the whole database would be wiped.
			final long currentMaxRootObjectId = rootObjectIdSelector.yieldGlobal();

			if(currentMaxRootObjectId == Persistence.nullId())
			{
				/*
				 * no error here. Strictly seen, an empty or cleared database is valid.
				 * Should the need for an error arise, StorageRootOidSelector#yieldGlobal
				 * is the right place to do it in a customized way.
				 */
				return;
			}

//			DEBUGStorage.println(Thread.currentThread().getName() + " enqueuing root OID " + currentMaxRootOid);

			/*
			 * this initializes the next marking.
			 * From here on, pendingMarksCount can only be 0 again if marking is complete.
			 */
			this.acceptObjectId(currentMaxRootObjectId);
		}

		@Override
		public final void acceptObjectId(final long objectId)
		{
			// do not enqueue null oids, not even get the lock
			if(objectId == Persistence.nullId())
			{
				return;
			}

			this.enqueue(this.oidMarkQueues[(int)(objectId & this.channelHash)], objectId);
		}

		@Override
		public final void enqueue(final StorageobjectIdMarkQueue objectIdMarkQueue, final long objectId)
		{
			this.incrementPendingMarksCount();
			// no need to keep the lock longer than necessary or nested with the queue lock.
			objectIdMarkQueue.enqueue(objectId);
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
			// (14.07.2016 TM)TODO: make marking configuration dynamic
			return new CachingReferenceMarker(this, this.channelCount, 10000);
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

			final StorageobjectIdMarkQueue[] oidMarkQueues = this.oidMarkQueues;

			// lock for every queue is only acquired once and all oids are enqueued efficiently
			for(int i = 0; i < oidsPerChannel.length; i++)
			{
				if(sizes[i] == 0)
				{
					// avoid unnecessary locking and execution overhead
					continue;
				}
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
			private final StorageEntityMarkMonitor.Default markMonitor        ;
			private final long[][]                         oidsPerChannel     ;
			private final int[]                            oidsPerChannelSizes;
			private final int                              channelHash        ;
			private final int                              bufferLength       ;

			CachingReferenceMarker(
				final StorageEntityMarkMonitor.Default markMonitor ,
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
			public final void acceptObjectId(final long objectId)
			{
				// do not enqueue null oids
				if(objectId == Persistence.nullId())
				{
					return;
				}

				final int i = (int)(objectId & this.channelHash);

				this.oidsPerChannel[i][this.oidsPerChannelSizes[i]] = objectId;
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


		private synchronized <T> T lockAllMarkQueues(final int currentIndex, final Supplier<T> logic)
		{
			// funny: dynamic locking via trivial recursion
			if(currentIndex >= 0)
			{
				synchronized(this.oidMarkQueues[currentIndex])
				{
					return this.lockAllMarkQueues(currentIndex - 1, logic);
				}
			}

			return logic.get();
		}


		public synchronized String DEBUG_state()
		{
			return this.lockAllMarkQueues(this.channelHash, () ->
			{
				final VarString vs = VarString.New("GC state");

				vs
				.lf().padLeft(Long.toString(this.pendingMarksCount), 10, ' ').add(" pending marks count")
				;
				for(int i = 0; i < this.oidMarkQueues.length; i++)
				{
					vs.lf().padLeft(Long.toString(this.oidMarkQueues[i].size()), 10, ' ').add(" in channel #" + i);
				}

				vs
				.lf()
				.lf().add("Hot  complete: ").add(this.gcHotPhaseComplete)
				.lf().add("Cold complete: ").add(this.gcColdPhaseComplete)
				.lf()
				.lf().add("sweepGeneration     : ").add(this.sweepGeneration     )
				.lf().add("lastSweepEnd        : ").add(this.lastSweepEnd        )
				.lf().add("lastSweepStart      : ").add(this.lastSweepStart      )
				.lf().add("gcHotGeneration     : ").add(this.gcHotGeneration     )
				.lf().add("gcColdGeneration    : ").add(this.gcColdGeneration    )
				.lf().add("lastGcColdCompletion: ").add(this.lastGcColdCompletion)
				.lf().add("lastGcHotCompletion : ").add(this.lastGcHotCompletion )
				.lf()
				.lf().add("Needs sweep (").add(this.sweepingChannelCount).add("):")
				;
				for(int i = 0; i < this.needsSweep.length; i++)
				{
					vs.lf().blank().add(i).add(": ").add(this.needsSweep[i]);
				}

				vs
				.lf().padLeft(Long.toString(this.pendingStoreUpdateCount), 10, ' ').blank().add("pending store updates")
				;
				for(int i = 0; i < this.pendingStoreUpdates.length; i++)
				{
					vs.lf().blank().add(i).add(": ").add(this.pendingStoreUpdates[i]);
				}

				return vs.toString();
			});
		}

	}

}
