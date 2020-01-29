package one.microstream.reference;

import static one.microstream.X.coalesce;
import static one.microstream.X.mayNull;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public interface LazyReferenceManager
{
	public void register(Lazy<?> lazyReference);

	public void cleanUp(long nanoTimeBudget);

	public default void cleanUp()
	{
		this.cleanUp(Long.MAX_VALUE);
	}

	public void clear();

	public LazyReferenceManager start();

	public LazyReferenceManager stop();

	public <P extends Consumer<? super Lazy<?>>> P iterate(P procedure);


	// (28.01.2020 TM)FIXME: priv#89: start LRM with EmbeddedStorageManager
	// (28.01.2020 TM)FIXME: priv#89 / priv#207: connect LRM life cycle with ERM life cycle.

	public static LazyReferenceManager set(final LazyReferenceManager referenceManager)
	{
		return Static.set(referenceManager);
	}

	public static LazyReferenceManager get()
	{
		return Static.get();
	}


	public final class Static
	{
		static final LazyReferenceManager DUMMY = new LazyReferenceManager.Dummy();

		static LazyReferenceManager globalReferenceManager = DUMMY;

		static synchronized LazyReferenceManager set(final LazyReferenceManager referenceManager)
		{
			final LazyReferenceManager old = globalReferenceManager;
			globalReferenceManager = coalesce(referenceManager, DUMMY);
			return old;
		}

		static synchronized LazyReferenceManager get()
		{
			return globalReferenceManager;
		}

		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 * 
		 * @throws UnsupportedOperationException
		 */
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}

	}
	
	public static boolean isValidTimeout(final long millisecondTimeout)
	{
		return millisecondTimeout > 0;
	}
	
	public static boolean isValidMemoryQuota(final double memoryQuota)
	{
		return memoryQuota >= 0.0 && memoryQuota <= 1.0;
	}
	
	public static long validateTimeout(final long millisecondTimeout)
	{
		if(isValidTimeout(millisecondTimeout))
		{
			return millisecondTimeout;
		}
		
		// (28.01.2020 TM)EXCP: proper exception
		throw new RuntimeException("Timeout must be greater than 0.");
	}
	
	public static double validateMemoryQuota(final double memoryQuota)
	{
		if(isValidMemoryQuota(memoryQuota))
		{
			return memoryQuota;
		}
		
		// (28.01.2020 TM)EXCP: proper exception
		throw new RuntimeException("Memory quora must be in the range [0.0; 1.0].");
	}

	// (28.01.2020 TM)FIXME: priv#89: custom predicate (touched & MemoryUsage)
	
	public static LazyReferenceManager.Checker Checker(
		final long   millisecondTimeout,
		final double memoryQuota,
		final Check  customCheck
	)
	{
		// note: at least timeout is validated to always be a usable value. The other two can be null/0.
		return new Checker.Default(
			mayNull(customCheck),
			validateTimeout(millisecondTimeout),
			validateMemoryQuota(memoryQuota)
		);
	}
	
	public static LazyReferenceManager.Checker Checker(
		final long millisecondTimeout
	)
	{
		return Checker(millisecondTimeout, Checker.Defaults.defaultMemoryQuota(), null);
	}
	
	public static LazyReferenceManager.Checker CheckerTimeout(
		final long millisecondTimeout
	)
	{
		return Checker(millisecondTimeout, 0.0, null);
	}
	
	public static LazyReferenceManager.Checker CheckerMemory(
		final double memoryQuota
	)
	{
		// kind of dumb, but well ...
		return Checker(Long.MAX_VALUE, memoryQuota, null);
	}
	
	public static LazyReferenceManager.Checker Checker(
		final long   millisecondTimeout,
		final double memoryQuota
	)
	{
		return Checker(millisecondTimeout, memoryQuota, null);
	}
	
	public static LazyReferenceManager.Checker Checker(
		final Check customCheck
	)
	{
		return Checker(
			Checker.Defaults.defaultTimeout(),
			Checker.Defaults.defaultMemoryQuota(),
			customCheck
		);
	}
	
	public static LazyReferenceManager.Checker Checker()
	{
		return Checker(null);
	}
		

	

	public interface Checker
	{
		public default void beginCheckCycle()
		{
			// no-op by default
		}

		/**
		 * 
		 * @param lazyReference the lazy reference to check against
		 * @return if additional checks should be prevented
		 */
		public boolean check(Lazy<?> lazyReference);

		public default void endCheckCycle()
		{
			// no-op by default
		}
		
		
		public interface Defaults
		{
			public static long defaultTimeout()
			{
				// about 15 minutes
				return 1_000_000;
			}
			
			public static double defaultMemoryQuota()
			{
				// all avaiable memory is used
				return 1.0;
			}
		}
				
		/**
		 * This implementation uses two dimensions to evaluate if a lazy reference will be cleared:<br>
		 * - time: a ref's "age" in terms of {@link Lazy#lastTouched()} compared to {@link System#currentTimeMillis()}<br>
		 * - memory: the amount of used memory compared to the permitted quota of total available memory.
		 * <p>
		 * Either dimension can be deactivated by setting its configuration value to 0.<br>
		 * If both are non-zero, a arithmetically combined check will make clearing of a certain reference
		 * more like the older it gets as free memory shrinks.<br>
		 * So, as free memory gets lower, older/passive references are cleared sooner, newer/active ones later.
		 * 
		 * @author TM
		 */
		public final class Default implements LazyReferenceManager.Checker, Lazy.ClearingEvaluator
		{
			///////////////////////////////////////////////////////////////////////////
			// constants //
			//////////////
			
			public static double memoryQuotaNoCheck()
			{
				return 0.0;
			}
			
			public static long graceTimeMinimum()
			{
				return 1000;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			// configuration values (final) //

			
			/**
			 * An optional custom checking logic that overrides the generic logic.<br>
			 * May be <code>null</code>.<br>
			 * If it returns <code>null</code>, the generic logic is used.
			 */
			private final Check customCheck;
			
			/**
			 * The timeout in milliseconds after which a reference is cleared regardless of memory consumption.<br>
			 * May be 0 to be deactivated.<br>
			 * Negative values are invalid and must be checked before the constructor is called.
			 */
			private final long timeoutMs;
			
			private final long graceTimeMs;
			
			/**
			 * The quota of total available memory (= min(committed, max)) the check has to comply with.<br>
			 * May be 0.0 to be deactivated.<br>
			 * 1.0 means all of the available memory can be used.<br>
			 * Anything outside of [0.0; 1.0] is invalid and must be checked before the constructor is called.
			 * 
			 */
			private final double memoryQuota;
			
			// cycle working variables //
			
			private MemoryUsage cycleMemoryUsage;
			
			/**
			 * The {@link System#currentTimeMillis()}-compliant timestamp value below which a reference
			 * is considered to be timed out and will be cleared regardless of memory consumption.
			 */
			private long cycleTimeoutThresholdMs;
			
			private long cycleGraceTimeThresholdMs;
			
			/**
			 * The maximum number of bytes that may be used before
			 */
			private long cycleMemoryLimit;
			private long cycleMemoryUsed;
			private boolean cycleMemoryExceeded;
			private long cycleMemoryBasedClearCount;
			
			// derive working variables for fast integer arithmetic //
			
			private long sh10MemoryLimit;
			private long sh10MemoryUsed;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Default(final Check customCheck, final long timeoutMs, final double memoryQuota)
			{
				super();
				this.customCheck = customCheck;
				this.timeoutMs   = timeoutMs  ;
				this.memoryQuota = memoryQuota;
				this.graceTimeMs = deriveGraceTime(timeoutMs);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			private static long deriveGraceTime(final long timeoutMs)
			{
				return Math.min(graceTimeMinimum(), timeoutMs / 2);
			}
			
			private static long shift10(final long value)
			{
				// equals *1024, which is roughly *1000, but significantly faster and the precise factor doesn't matter.
				return value << 10;
			}
			
			private boolean isMemoryCheckEnabled()
			{
				return this.memoryQuota != memoryQuotaNoCheck();
			}

			@Override
			public final void beginCheckCycle()
			{
				// timeout is guaranteed to be > 0.
				this.cycleTimeoutThresholdMs = System.currentTimeMillis() - this.timeoutMs;
				
				// to query system time only exactely once.
				this.cycleGraceTimeThresholdMs = this.cycleTimeoutThresholdMs + this.timeoutMs - this.graceTimeMs;

				// querying a MemoryUsage instance takes about 500 ns to query, so it is only done occasionally.
				this.updateMemoryUsage();
				this.cycleMemoryBasedClearCount = 0;
			}
			
			private void updateMemoryUsage()
			{
				this.cycleMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
				this.cycleMemoryLimit = this.calculateMemoryLimit(this.cycleMemoryUsage);
				this.cycleMemoryUsed  = this.cycleMemoryUsage.getUsed();
				this.cycleMemoryExceeded = this.isMemoryCheckEnabled() && this.cycleMemoryUsed >= this.cycleMemoryLimit;
				
				// derived values for fast integer arithmetic for every check
				this.sh10MemoryLimit = shift10(this.cycleMemoryLimit);
				this.sh10MemoryUsed  = shift10(this.cycleMemoryUsed);
			}
			
			private void registerMemoryBasedClearing()
			{
				if((++this.cycleMemoryBasedClearCount & 127L) == 0)
				{
					this.updateMemoryUsage();
				}
			}
			
			private boolean memoryBasedClear(final boolean decision)
			{
				if(decision)
				{
					this.registerMemoryBasedClearing();
					return true;
				}
				
				return false;
			}
			
			private long calculateMemoryLimit(final MemoryUsage memoryUsage)
			{
				if(!this.isMemoryCheckEnabled())
				{
					return Long.MAX_VALUE;
				}
				
				// max might return -1 and is also capped by committed memory, so both must be considered.
				return (long)(Math.min(memoryUsage.getCommitted(), memoryUsage.getMax()) * this.memoryQuota);
			}

			@Override
			public final boolean check(final Lazy<?> lazyReference)
			{
				return lazyReference.clear(this);
			}
						
			private Boolean performCustomCheck(final Lazy<?> lazyReference)
			{
				return this.customCheck.test(lazyReference, this.cycleMemoryUsage, this.timeoutMs);
			}
			
			@Override
			public final boolean clear(final Lazy<?> lazyReference)
			{
				final Boolean check;
				if(this.customCheck != null && (check = this.performCustomCheck(lazyReference)) != null)
				{
					return check.booleanValue();
				}
				// custom check is not present or was indecisive and defers to the generic logic.
				
				// simple time-based checks: never clear inside grace time, always clear beyond timeout.
				final long lastTouched = lazyReference.lastTouched();
				if(lastTouched >= this.cycleGraceTimeThresholdMs)
				{
					return false;
				}
				if(lastTouched < this.cycleTimeoutThresholdMs)
				{
					return true;
				}
				
				// no simple case, so a more sophisticated check combining age and memory is required
				return this.checkByMemoryWithAgePenalty(lastTouched);
			}
						
			private boolean checkByMemoryWithAgePenalty(final long lastTouched)
			{
				// if memory check is disabled, return right away
				if(!this.isMemoryCheckEnabled())
				{
					return false;
				}
				
				final long age = lastTouched - this.cycleTimeoutThresholdMs;
				final long sh10Weight = shift10(age) / this.timeoutMs;

				// used memory times weightSh10 is a kind of "age penalty" towards the actually used memory
				final boolean clearingDecision =
					this.sh10MemoryUsed + this.cycleMemoryUsed * sh10Weight >= this.sh10MemoryLimit
				;
				
				return this.memoryBasedClear(clearingDecision);
			}

		}

		
	}

	
	public interface Check
	{
		public Boolean test(Lazy<?> lazyReference, MemoryUsage memoryUsage, long millisecondTimeout);
	}
	
	public static LazyReferenceManager New(final long millisecondTimeout)
	{
		return New(Checker(millisecondTimeout));
	}
	
	public static LazyReferenceManager New()
	{
		return New(Checker());
	}
	
	public static LazyReferenceManager New(final Check customCheck)
	{
		return New(Checker(customCheck));
	}
	
	// (29.01.2020 TM)FIXME: priv#89: more convenience methods

	public static LazyReferenceManager New(final Checker checker)
	{
		return New(checker, Default.DEFAULT_CHECK_INTERVAL_MS, Default.DEFAULT_TIME_BUDGET_NS);
	}

	public static LazyReferenceManager New(
		final Checker checker               ,
		final long    milliTimeCheckInterval,
		final long    nanoTimeBudget
	)
	{
		return New(
			checker,
			_longReference.Constant(milliTimeCheckInterval),
			_longReference.Constant(nanoTimeBudget)
		);
	}

	public static LazyReferenceManager New(
		final Checker        checker                       ,
		final _longReference milliTimeCheckIntervalProvider,
		final _longReference nanoTimeBudgetProvider
	)
	{
		return new Default(checker, milliTimeCheckIntervalProvider, nanoTimeBudgetProvider);
	}

	public final class Clearer implements Checker
	{
		@Override
		public boolean check(final Lazy<?> lazyReference)
		{
			lazyReference.clear();
			return true;
		}
	}


	public final class Default implements LazyReferenceManager
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final Clearer CLEARER                   = new Clearer();

		// defaults mean to check every second with a budget of 1 MS (0.1% thread activity)
		        static final long    DEFAULT_CHECK_INTERVAL_MS = 1_000        ;
		        static final long    DEFAULT_TIME_BUDGET_NS    =     1_000_000;



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final    Checker        checker                       ;
		private final    _longReference millitimeCheckIntervalProvider;
		private final    _longReference nanoTimeBudgetProvider        ;
		private final    Entry          head   = new Entry(null)      ;
		private          Entry          tail   = this.head            ;
		private          Entry          cursor = this.head            ; // current "last" entry for checking
		        volatile boolean        running                       ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Checker        checker               ,
			final _longReference checkIntervalProvider ,
			final _longReference nanoTimeBudgetProvider
		)
		{
			super();
			this.checker                        = checker               ;
			this.millitimeCheckIntervalProvider = checkIntervalProvider ;
			this.nanoTimeBudgetProvider         = nanoTimeBudgetProvider;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final void internalCleanUp(final long timeBudgetBound, final Checker checker)
		{
			/* (22.06.2016 TM)FIXME: full clear does not clear fully
			 * productive use of the full clear call clears only like 6 of 300 references on a regular basis.
			 * Sometimes all, but most of the time not.
			 * Even though the application is single threaded as far as lazy reference creation is concerned (simple "main test" class execution)
			 */

			final Entry currentTail;

			/*
			 * This local synchronized block is crucial to prevent deadlocks!
			 * The reference manager thread may never keep a lock on the manager instance and then require
			 * a lock on a lazy reference in order to complete its cleanup cycle.
			 * Consider the following szenario:
			 * - application thread locks lazy instance #1 to load its content.
			 * - manager thread locks the manager instance for the whole check cycle, starts checking.
			 * - loading of the LI#1 content causes LI#2 to be created and registered at the manager
			 * - manager thread wants to check LI#1 for timeout.
			 * - So app.thread holds LI#1 lock, requires mgr.lock, mgr.thread holds mgr.lock, requires LI#1 lock
			 * => deadlock (happened in productive use, although very rare)
			 * The solution to prevent this is:
			 * The lock on the manager instance is only held for a very short time without requiring any additional lock
			 * internally to leave the lock again, only to consistently query the current tail entry.
			 * The rest of the algorithm does not interfere with any other thread, so it can be done without lock.
			 *
			 * Of course it is important that no other method calling this method keeps the mgr.lock for the whole
			 * check cycle, otherwise the deadlock can still occur (i.e. no synchronized method!).
			 * Happened in productive use after fixing this method :(.
			 */
			synchronized(this)
			{
				currentTail = this.tail;
			}

			Entry last, e = (last = this.cursor).nextLazyManagerEntry;

			// special case check initially, there is only the head with no next entry
			if(e == null)
			{
				return;
			}

			checker.beginCheckCycle();

			cleanUp:
			do // do at least one check, no matter what
			{
				// keep strong reference to avoid intermediate garbage collection
				final Lazy<?> ref = e.get();

				// check for orphan entry
				if(ref != null)
				{
					// leave checking logic completely to checker (also for lock atomicity reasons)
					checker.check(ref);
				}
				else if(e != currentTail)
				{
					// remove orphan entry (never remove current tail entry for list consistency reasons)
					e = last.nextLazyManagerEntry = e.nextLazyManagerEntry;
					continue;
				}

				if(e == currentTail)
				{
					/*
					 * if the iteration reached the current tail entry, the cursor gets reset (outside the loop)
					 * and the iteration gets aborted. Rationale behind that:
					 * It might be that there have been added new entry to the chain while the iteration was
					 * executed. However these entries are newly created, hence will hardly timeout right away.
					 * The oldest entries near the head are much more likely for that, so it is efficient to restart.
					 */
					last = this.head;
					break cleanUp;
				}

				e = (last = e).nextLazyManagerEntry;
			}
			while(System.nanoTime() < timeBudgetBound);

			// remember last checked entry for next cleanup run. Cursor field is strictly only used by one thread.
			this.cursor = last;

			checker.endCheckCycle();
		}

		final void internalCleanUp(final long timeBudgetBound)
		{
			this.internalCleanUp(timeBudgetBound, this.checker);
		}

		final void cleanUpBudgeted()
		{
			// this method may NOT (and needs not to) be synchronized (see comment in internalCleanUp).

			// perform actual cleanup for the dynamically specified nano time budget
			this.cleanUp(this.nanoTimeBudgetProvider.get());
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized void register(final Lazy<?> lazyReference)
		{
//			XDebug.debugln(this + " registering " + lazyReference.peek());
			// uniqueness of references is guaranteed by calling this method only exactely once per reference instance
			this.tail = this.tail.nextLazyManagerEntry = new Entry(lazyReference);
		}

		@Override
		public void clear()
		{
			this.internalCleanUp(Long.MAX_VALUE, CLEARER);
		}

		@Override
		public void cleanUp(final long nanoTimeBudget)
		{
			// this method may NOT (and needs not to) be synchronized (see comment in internalCleanUp).

			// giving a very high or MAX_VALUE (unlimited) time budget will cause negative values
			final long timeBudgetBound = System.nanoTime() + nanoTimeBudget;

			this.internalCleanUp(timeBudgetBound < 0 ? Long.MAX_VALUE : timeBudgetBound);
		}

		@Override
		public synchronized LazyReferenceManager start()
		{
			// check for already running condition to avoid starting more than more thread
			if(!this.running)
			{
				this.running = true;
				new LazyReferenceCleanupThread(new WeakReference<>(this), this.millitimeCheckIntervalProvider).start();
			}
			return this;
		}

		@Override
		public synchronized LazyReferenceManager stop()
		{
			this.running = false;
			return this;
		}

		@Override
		public synchronized <P extends Consumer<? super Lazy<?>>> P iterate(final P procedure)
		{
			for(Entry e = this.head; (e = e.nextLazyManagerEntry) != null;)
			{
				final Lazy<?> ref = e.get();
				if(ref != null)
				{
					procedure.accept(ref);
				}
			}
			return procedure;
		}


		static final class LazyReferenceCleanupThread extends Thread
		{
			// lazy reference for automatic thread termination
			private final WeakReference<LazyReferenceManager.Default> parent               ;
			private final _longReference                                     checkIntervalProvider;

			LazyReferenceCleanupThread(
				final WeakReference<LazyReferenceManager.Default> parent,
				final _longReference                      checkIntervalProvider
			)
			{
				super(LazyReferenceManager.class.getSimpleName() + '@' + System.identityHashCode(parent));
				this.parent                = parent               ;
				this.checkIntervalProvider = checkIntervalProvider;
			}


			@Override
			public void run()
			{
				LazyReferenceManager.Default parent;
				while((parent = this.parent.get()) != null)
				{
					// sleep for a dynamically specified milli time until the next check
					try
					{
						// check for running state. Must be the first action in case of swallowed exception
						if(!parent.running)
						{
							break;
						}

						// perform check
						parent.cleanUpBudgeted();

						// very nasty: must clear the reference from the stack in order for the weak reference to work
						parent = null;

						// extra nasty: must sleep with nulled reference for WR to work, not before.
						try
						{
							Thread.sleep(this.checkIntervalProvider.get());
						}
						catch(final InterruptedException e)
						{
							// sleep interrupted, proceed with check immediately
						}
					}
					catch(final Exception e)
					{
						/* thread may not die on any exception, just continue looping as long as parent exists
						 * and running is true
						 */
					}
				}
				// either parent has been garbage collected or stopped, so terminate.
//				XDebug.debugln(Thread.currentThread().getName() + " terminating.");
			}
		}


		static final class Entry extends WeakReference<Lazy<?>>
		{
			Entry nextLazyManagerEntry; // explicit naming to avoid ambiguity with WeakReference's field

			public Entry(final Lazy<?> referent)
			{
				super(referent);
			}

		}

	}


	public final class Dummy implements LazyReferenceManager
	{
		Dummy()
		{
			super();
		}

		@Override
		public final void register(final Lazy<?> lazyReference)
		{
			// no-op dummy
		}

		@Override
		public final void clear()
		{
			// no-op dummy
		}

		@Override
		public final void cleanUp(final long nanoTimeBudget)
		{
			// no-op dummy
		}

		@Override
		public final Dummy start()
		{
			return this;
		}

		@Override
		public final Dummy stop()
		{
			return this;
		}

		@Override
		public <P extends Consumer<? super Lazy<?>>> P iterate(final P procedure)
		{
			return procedure;
		}

	}

}
