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

import java.util.function.Supplier;

import org.slf4j.Logger;

import one.microstream.chars.VarString;
import one.microstream.math.XMath;
import one.microstream.persistence.types.PersistenceLiveStorerRegistry;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.reference.Referencing;
import one.microstream.reference.Swizzling;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.util.logging.Logging;


/**
 * Central instance serving as a locking instance (concurrency monitor) for concurrently marking entities.
 * Via the indirection over a pure OID (long primitives) mark queue, the actual marking, sweeping and concurrency
 * management associated with it is strictly thread local, like the rest of the storage implementation is.
 * Without that centralization and indirection, absolute concurrency correctness is hard to achieve and much more
 * coding effort.
 *
 * 
 */
public interface StorageEntityMarkMonitor extends PersistenceObjectIdAcceptor
{
	public void signalPendingStoreUpdate(StorageEntityCache<?> channel);

	public void resetCompletion();

	public void advanceMarking(StorageObjectIdMarkQueue objectIdMarkQueue, int amount);

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

	public void enqueue(StorageObjectIdMarkQueue objectIdMarkQueue, long objectId);

//	public String DEBUG_state();
	
	/**
	 * Reset to a clean initial state, ready to be used.
	 */
	public void reset();


	public static StorageEntityMarkMonitor.Creator Creator()
	{
		return Creator(StorageEntityMarkMonitor.Creator.Defaults.defaultReferenceCacheLength());
	}
	
	public static StorageEntityMarkMonitor.Creator Creator(final int referenceCacheLength)
	{
		return new StorageEntityMarkMonitor.Creator.Default(
			XMath.positive(referenceCacheLength)
		);
	}
	
	public interface ObjectIds
	{
		public long[] objectIds();
		
		public int size();
	}

	public interface Creator
	{
		public StorageEntityMarkMonitor createEntityMarkMonitor(
			StorageObjectIdMarkQueue[]                 oidMarkQueues    ,
			StorageEventLogger                         eventLogger      ,
			Referencing<PersistenceLiveStorerRegistry> refStorerRegistry
		);
		
		
		
		public interface Defaults
		{
			public static int defaultReferenceCacheLength()
			{
				/*
				 * Since every channel allocates a reference cache array for every other channel,
				 * the total amount of reference caches is channelCount^2.
				 * This means that the reference cache length should not be to big, otherwise the
				 * occupied memory increases dramatically with the number of channel.
				 * E.g:
				 * Length 10_000:
				 * 32 channel occupy 32*32*10_000*8 = 80 MB just for reference caches
				 * 64 channel occupy 64*64*10_000*8 = 300 MB just for reference caches
				 * 
				 * Since this is just a cache to prevent inter-thread-communication for single objectIds,
				 * it doesn't have to be very big in the first place. It just defines how big the batch
				 * will be that is communicatated between channels. 100 should be fine. Numbers up to 1000 are
				 * coneivable. Everything beyong that should be moreless overkill or even crazy.
				 */
				return 100;
			}
		}



		public final class Default implements StorageEntityMarkMonitor.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final int referenceCacheLength;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(final int referenceCacheLength)
			{
				super();
				this.referenceCacheLength = referenceCacheLength;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public StorageEntityMarkMonitor createEntityMarkMonitor(
				final StorageObjectIdMarkQueue[]                 objectIdMarkQueues,
				final StorageEventLogger                         eventLogger       ,
				final Referencing<PersistenceLiveStorerRegistry> refStorerRegistry
			)
			{
				return new StorageEntityMarkMonitor.Default(
					objectIdMarkQueues.clone(),
					eventLogger,
					refStorerRegistry,
					this.referenceCacheLength
				);
			}

		}

	}


	final class Default implements StorageEntityMarkMonitor, StorageReferenceMarker
	{
		private final static Logger logger = Logging.getLogger(Default.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// state 1.0: immutable or stateless (as far as this implementation is concerned)

		private final Referencing<PersistenceLiveStorerRegistry> refStorerRegistry;
		
		private final StorageEventLogger eventLogger         ;
		private final int                channelCount        ;
		private final int                channelHash         ;
		private final int                referenceCacheLength;
		
		
		// state 2.0: final references to mutable instances, i.e. content must be cleared on reset
		
		private final StorageObjectIdMarkQueue[] oidMarkQueues   ;
		private final long[]                     channelRootOids ;
		private final StorageReferenceMarker[]   referenceMarkers;

		
		// state 3.0: mutable fields. Must be cleared on reset.
		
		private       long      pendingMarksCount      ;
		private final boolean[] pendingStoreUpdates    ;
		private       int       pendingStoreUpdateCount;
		
		private final boolean[] needsSweep             ;
		private       int       sweepingChannelCount   ;
		
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
		private boolean gcHotPhaseComplete;
		
		/*
		 * Indicates that not only no new data has been received since the last sweep, but also that a second sweep
		 * has already been executed since then, removing all unreachable entities and effectively establishing
		 * a clean / optimized / stable state.
		 * This flag can be seen as "no new data level 2".
		 * It will shut off all GC activity until the next store resets the flags.
		 */
		private boolean gcColdPhaseComplete;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageObjectIdMarkQueue[]                 oidMarkQueues       ,
			final StorageEventLogger                         eventLogger         ,
			final Referencing<PersistenceLiveStorerRegistry> refStorerRegistry   ,
			final int                                        referenceCacheLength
		)
		{
			super();
			this.eventLogger          = eventLogger                   ;
			this.refStorerRegistry    = refStorerRegistry             ;
			this.oidMarkQueues        = oidMarkQueues                 ;
			this.referenceCacheLength = referenceCacheLength          ;
			this.channelCount         = oidMarkQueues.length          ;
			this.channelHash          = this.channelCount - 1         ;
			this.pendingStoreUpdates  = new boolean[this.channelCount];
			this.needsSweep           = new boolean[this.channelCount];
			this.channelRootOids      = new long   [this.channelCount];
			
			this.referenceMarkers = new StorageReferenceMarker[this.channelCount];
			
			// mostly redundant for instance initialization, but consistency is important.
			this.initialize();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private void initializeMarkQueues()
		{
			// this differs from resetMarkQueues() in that here are no consistency checks
			for(int i = 0; i < this.oidMarkQueues.length; i++)
			{
				this.oidMarkQueues[i].reset();
			}
		}
		
		private void initializeChannelRootIds()
		{
			for(int i = 0; i < this.channelRootOids.length; i++)
			{
				this.channelRootOids[i] = Swizzling.nullId();
			}
		}
		
		private void initializePendingStoreUpdates()
		{
			for(int i = 0; i < this.pendingStoreUpdates.length; i++)
			{
				this.pendingStoreUpdates[i] = false;
			}
			
			this.pendingMarksCount = 0;
			this.pendingStoreUpdateCount = 0;
		}
		
		private void initializeSweepingState()
		{
			for(int i = 0; i < this.needsSweep.length; i++)
			{
				this.needsSweep[i] = false;
			}
			
			this.sweepingChannelCount = 0;
		}
		
		private void initializeCompletionState()
		{
			// GC is initially completed because there is no data at all. Initialization and stores will flip them.
			this.gcHotPhaseComplete  = true;
			this.gcColdPhaseComplete = true;
		}
		
		private void initializeGenerationalState()
		{
			this.sweepGeneration      = 0;
			this.lastSweepStart       = 0;
			this.lastSweepEnd         = 0;
			this.gcHotGeneration      = 0;
			this.gcColdGeneration     = 0;
			this.lastGcHotCompletion  = 0;
			this.lastGcColdCompletion = 0;
		}
		
		private final void initialize()
		{
			// this first block basically just sets everything to 0.
			this.initializeMarkQueues();
			this.initializeChannelRootIds();
			this.initializePendingStoreUpdates();
			this.initializeSweepingState();
			this.initializeGenerationalState();
			
			// referenceMarkers may NOT be cleared! They are initialized once with a linking instance that must be kept!
			
			// sets completion state to true, not false!
			this.initializeCompletionState();
		}
		
		@Override
		public final synchronized void reset()
		{
			/* Note:
			 * The methods for "resetting" mark queues and completion state refer to
			 * the operating state and are not applicable here.
			 * The actual resetting is not different from (re)initializing everything.
			 */
			this.initialize();
			
			// this is the only actually exclusive resetting method
			this.synchResetReferenceMarkers();
		}
		
		private void synchResetReferenceMarkers()
		{
			for(int i = 0; i < this.referenceMarkers.length; i++)
			{
				if(this.referenceMarkers[i] == null)
				{
					continue;
				}
				this.referenceMarkers[i].reset();
			}
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
		public final synchronized void advanceMarking(final StorageObjectIdMarkQueue oidMarkQueue, final int amount)
		{
//			DEBUGStorage.println(System.identityHashCode(oidMarkQueue) + " >-  " + this.pendingMarksCount + " " + oidMarkQueue.size());

			if(this.pendingMarksCount < amount)
			{
				throw new StorageException(
					"pending marks count (" + this.pendingMarksCount +
					") is smaller than the number to be advanced (" + amount + ")."
				);
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
				logger.debug("GC not needed");
				this.eventLogger.logGarbageCollectorNotNeeded();
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
				logger.debug("Storage GC completed #{} @ {}", this.gcColdGeneration, this.lastGcColdCompletion);
				this.eventLogger.logGarbageCollectorCompleted(this.gcColdGeneration, this.lastGcColdCompletion);
			}
			else
			{
				this.gcHotPhaseComplete = true;
				this.lastGcHotCompletion = System.currentTimeMillis();
				this.gcHotGeneration++;
				logger.debug("Storage GC completed hot phase #{} @ {}", this.gcHotGeneration, this.lastGcHotCompletion);
				this.eventLogger.logGarbageCollectorCompletedHotPhase(this.gcHotGeneration, this.lastGcHotCompletion);
				
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
			
			logger.debug("StorageChannel#{} completed sweeping", channel.channelIndex());
			this.eventLogger.logGarbageCollectorSweepingComplete(channel);

			// decrement sweep channel count and execute completion logic if required.
			if(--this.sweepingChannelCount == 0)
			{
				this.lastSweepEnd = System.currentTimeMillis();
				this.incrementSweepGeneration();
				this.advanceGcCompletion();
				this.determineAndEnqueueRootOid(rootOidSelector);
			}
		}
		
		private void incrementSweepGeneration()
		{
			final PersistenceLiveStorerRegistry storerRegistry = this.refStorerRegistry.get();
			if(storerRegistry != null)
			{
				// storerRegistry might be null if there is no connected application, yet.
				storerRegistry.clearGroupAndAdvance(this.sweepGeneration, this.sweepGeneration + 1);
			}

			this.sweepGeneration++;
		}

		final synchronized void resetChannelRootIds()
		{
			// no difference to reinitializing
			this.initializeChannelRootIds();
		}

		final synchronized void resetMarkQueues()
		{
			for(int i = 0; i < this.oidMarkQueues.length; i++)
			{
				if(this.oidMarkQueues[i].hasElements())
				{
					throw new StorageException("ObjectId mark queue for channel " + i + " still has elements.");
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

			if(currentMaxRootObjectId == Swizzling.nullId())
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
			if(objectId == Swizzling.nullId())
			{
				return;
			}

			this.enqueue(this.oidMarkQueues[(int)(objectId & this.channelHash)], objectId);
		}

		@Override
		public final void enqueue(final StorageObjectIdMarkQueue objectIdMarkQueue, final long objectId)
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
			if(this.referenceMarkers[channel.channelIndex()] != null)
			{
				throw new StorageException(
					StorageReferenceMarker.class.getSimpleName()
					+ " for channel #" + channel.channelIndex()
					+ " already exists."
				);
			}
			
			return this.referenceMarkers[channel.channelIndex()] =
				new CachingReferenceMarker(this, this.channelCount, this.referenceCacheLength)
			;
		}

		final void enqueueBulk(final ObjectIds[] oidsPerChannel)
		{
			long totalSize = 0;
			
			/* (24.02.2020 TM)FIXME: priv#72: how is this size-adding loop concurrency-safe? Research and comment!
			 * The size might get concurrently modified by other channel threads while the loop runs.
			 */
			for(final ObjectIds e : oidsPerChannel)
			{
				totalSize += e.size();
			}

			synchronized(this)
			{
				this.pendingMarksCount += totalSize;
			}

			final StorageObjectIdMarkQueue[] oidMarkQueues = this.oidMarkQueues;

			// lock for every queue is only acquired once and all oids are enqueued efficiently
			for(int i = 0; i < oidsPerChannel.length; i++)
			{
				if(oidsPerChannel[i].size() == 0)
				{
					// avoid unnecessary locking and execution overhead
					continue;
				}
				oidMarkQueues[i].enqueueBulk(oidsPerChannel[i].objectIds(), oidsPerChannel[i].size());
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
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			// state 1.0: immutable or stateless (as far as this implementation is concerned)
			
			private final StorageEntityMarkMonitor.Default markMonitor   ;
			private final int                              channelHash   ;
			private final int                              bufferLength  ;
			

			// state 2.0: final references to mutable instances, i.e. content must be cleared on reset
			
			private final ChannelItem[] oidsPerChannel;
			
					
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			CachingReferenceMarker(
				final StorageEntityMarkMonitor.Default markMonitor ,
				final int                              channelCount,
				final int                              bufferLength
			)
			{
				super();
				this.markMonitor  = markMonitor     ;
				this.bufferLength = bufferLength    ;
				this.channelHash  = channelCount - 1;
				
				this.oidsPerChannel = new ChannelItem[channelCount];
				for(int i = 0; i < channelCount; i++)
				{
					this.oidsPerChannel[i] = new ChannelItem(bufferLength);
				}
			}
			
			static final class ChannelItem implements ObjectIds
			{
				final long[] oids;
				      int    size;
				
				ChannelItem(final int capacity)
				{
					super();
					this.oids = new long[capacity];
				}
				
				/**
				 * Add the passed oid and returns the resulting size.
				 */
				final int add(final long oid)
				{
					this.oids[this.size] = oid;
					
					return ++this.size;
				}
				
				final boolean isEmpty()
				{
					return this.size == 0;
				}
				
				final void reset()
				{
					// this is sufficient. Old oid data in the array is irrelevant.
					this.size = 0;
				}

				@Override
				public final long[] objectIds()
				{
					return this.oids;
				}

				@Override
				public final int size()
				{
					return this.size;
				}
				
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public final void acceptObjectId(final long objectId)
			{
				// do not enqueue null oids
				if(objectId == Swizzling.nullId())
				{
					return;
				}

				final int i = (int)(objectId & this.channelHash);

				if(this.oidsPerChannel[i].add(objectId) == this.bufferLength)
				{
					this.flush();
				}
			}

			// (24.02.2020 TM)FIXME: how are the calls to this method concurrency-safe?
			final void flush()
			{
				this.markMonitor.enqueueBulk(this.oidsPerChannel);
				this.resetOidsPerChannel();
			}
			
			final void resetOidsPerChannel()
			{
				for(int i = 0; i < this.oidsPerChannel.length; i++)
				{
					this.oidsPerChannel[i].reset();
				}
			}

			@Override
			public final boolean tryFlush()
			{
				for(int i = 0; i < this.oidsPerChannel.length; i++)
				{
					if(!this.oidsPerChannel[i].isEmpty())
					{
						this.flush();
						return true;
					}
				}

				return false;
			}
			
			@Override
			public final void reset()
			{
				this.resetOidsPerChannel();
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
