package net.jadoth.storage.types;

import net.jadoth.storage.types.StorageEntityCache.Implementation;

public final class StorageGcPhaseMonitor
	{
		private boolean isSweepMode;

		/*
		 * Indicates that no new data (store) has been received since the last sweep.
		 * This basically means that no more gc marking or sweeping is necessary, however as stored entities
		 * (both newly created and updated) are forced gray, potentially any number of entities can be
		 * virtually doomed but still be kept alive. Those will only be found in a second mark and sweep since the
		 * last store.
		 * This flag can be seen as "no new data level 1".
		 */
		private boolean gcHotPhaseComplete; // sweep once after startup in any case

		/*
		 * Indicates that not only no new data has been received since the last sweep, but also that a second sweep
		 * has already been executed since then, removing all unreachable entities and effectively establishing
		 * a clean / optimized / stable state.
		 * This flag can be seen as "no new data level 2".
		 * It will shut off all GC activity until the next store resets the flags.
		 */
		private boolean gcColdPhaseComplete;

		private boolean gcComplete;


		// (14.07.2016 TM)NOTE: dummy constructor to prepare for the new EntityCache Implementation
		StorageGcPhaseMonitor(final OidMarkQueue[] oidMarkQueues)
		{
			super();
		}

		final synchronized boolean isSweepMode()
		{
			return this.isSweepMode;
		}

		final synchronized void setCompletion()
		{
			this.gcHotPhaseComplete = this.gcColdPhaseComplete = this.gcComplete = true;
		}

		final synchronized void resetCompletion()
		{
			this.gcHotPhaseComplete = this.gcColdPhaseComplete = this.gcComplete = false;
		}

		final synchronized boolean isPartialComplete()
		{
			return this.gcHotPhaseComplete;
		}

		final synchronized boolean isComplete()
		{
			return this.gcComplete;
		}

		final synchronized void reset(final StorageEntityCache.Implementation[] colleagues)
		{
			this.resetGcPhaseState(colleagues);
			this.setCompletion();

			// truncate gray segments AFTER completion has been resetted so that no other channel marks entities again.
			for(final StorageEntityCache.Implementation e : colleagues)
			{
				e.truncateGraySegments();
			}
		}

		final synchronized void resetGcPhaseState(final StorageEntityCache.Implementation[] colleagues)
		{
			if(!this.isSweepMode)
			{
				return;
			}
			this.isSweepMode = false;
			for(int i = 0; i < colleagues.length; i++)
			{
				colleagues[i].completedSweeping = false;
			}
		}

		final synchronized boolean isMarkPhaseComplete(final StorageEntityCache.Implementation[] colleagues)
		{
			// mode is switched only once by the first channel no notice mark phase completion (last to check in)
			if(this.isSweepMode)
			{
				return true;
			}

			/* note on tricky concurrency:
			 * because a lock on this instance is required to complete the gray chain (see #advanceGrayChain),
			 * checking all channels while having the lock is guaranteed to not let any gray marking slip through.
			 * Even if another channel is about to process its last gray item and mark an already checked channel in
			 * the process, it cannot complete its gray chain until this channel releases the lock.
			 * Hence, this channel will see the other channel as still having a gray item (its last one) and therefore
			 * will return false (meaning mark phase not complete).
			 * If it really was the last gray item of all channels, it will get recognized properly in the next check,
			 * when all channel will have nextGray == null and nothing more gets enqueued.
			 */
			for(int i = 0; i < colleagues.length; i++)
			{
				if(colleagues[i].isMarking())
				{
//					DEBUGStorage.println(i + " not finished mark phase yet.");
					return false;
				}
			}

			for(int i = 0; i < colleagues.length; i++)
			{
				colleagues[i].completedSweeping = false;
//				DEBUGStorage.println(colleagues[i].channelIndex() + " " + colleagues[i].DEBUG_grayCount);
//				colleagues[i].DEBUG_grayCount = 0;

//				synchronized(colleagues[i])
//				{
//					if(colleagues[i].DEBUG_marked < 1500000 && colleagues[i].DEBUG_marked > 0)
//					{
//						DEBUGStorage.println(i + " incomplete marking!");
//						for(int j = 0; j < colleagues.length; j++)
//						{
//							colleagues[j].DEBUG_gcState();
//						}
//						DEBUGStorage.println("x_x");
//					}
//					DEBUGStorage.println(i + " Gray chain processed. Marked " + colleagues[i].DEBUG_marked);
//					colleagues[i].DEBUG_marked = 0;
//				}

			}

			// switch mode (only once, see check above)
			this.isSweepMode = true;

			/*
			 * It is important to set the completion state here (end of mark phase) and not at the end of the sweep
			 * phase.
			 * Rationale:
			 * An entity update (e.g. store, etc.) during a sweep phase sets white entities to initial (light gray).
			 * That mark gets resetted in the next sweep phase.
			 * If the flags were set at the end of the sweep phase, the following might occur:
			 * - store during sweep1 phase
			 * - completion gets resetted
			 * - some entities get marked as initial but not resetted by the sweep as they were already visited
			 * - sweep1 finishes, setting completion1
			 * - sweep2 visits the entities, resets them to white, but does not delete them
			 * - sweep2 finishes, setting completion2
			 * - GC effectively stops until the next store
			 * However some (potentially meanwhile unreachable) entities did not get deleted as sweep2 just
			 * resetted the initial mark but did not delete the entities yet.
			 *
			 * If the completion state setting is located here, an ongoing sweep phase with an intermediate store
			 * won't nullify the store's state resetting.
			 */
			if(this.gcHotPhaseComplete)
			{
//				DEBUGStorage.println("Cold mark phase complete.");
				this.gcColdPhaseComplete = true;
			}
			else
			{
//				DEBUGStorage.println("Hot mark phase complete.");
				this.gcHotPhaseComplete = true;
			}

			return true;
		}

		final synchronized boolean isSweepPhaseComplete(final StorageEntityCache.Implementation[] colleagues)
		{
			// mode is switched only once by the first channel no notice mark phase completion (last to check in)
			if(!this.isSweepMode)
			{
				return true;
			}

			// simple completed-check: sooner or later, every channels completes sweeping without rollback.
			for(int i = 0; i < colleagues.length; i++)
			{
				if(!colleagues[i].completedSweeping)
				{
					return false;
				}
			}

			// switch modes in all channels while under the lock's protection
			for(int i = 0; i < colleagues.length; i++)
			{
				colleagues[i].completedSweeping = false;

				/*
				 * calling this method here is essential because the gray chain must be initialized while the lock
				 * on the gc phase monitor is still hold to guarantee there is at least one gray item
				 * before the next check for completed mark phase.
				 * Calling that complex logic for all channels in one channel thread at this point is
				 * no concurrency problem for the simple reason that this method can only be entered if
				 * all other channels have completed sweeping and wait for the last channel to end the sweep mode,
				 * meaning they aren't doing anything (mutating state) at the moment.
				 */
				colleagues[i].resetAfterSweep();
			}

			// switch mode (only once, see check above)
			this.isSweepMode = false;

//			DEBUGStorage.println("Sweep phase complete");

			/* after a cold mark phase (no new data) has been done, the following sweep will establish a stable
			 * state in which additional marks and sweeps won't cause any change. Hence, the GC can be considered
			 * "complete" (until the next mutation in entities like store or import) and turned off.
			 * This is indicated by the flag set here.
			 */
			if(this.gcColdPhaseComplete)
			{
				this.gcComplete = true;
				DEBUGStorage.println("GC complete");
			}

			return true;
		}

	}