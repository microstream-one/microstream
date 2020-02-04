package one.microstream.reference;

import static one.microstream.X.coalesce;

import java.lang.management.MemoryUsage;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import one.microstream.meta.XDebug;
import one.microstream.reference.Lazy.Check;
import one.microstream.reference.Lazy.Checker;

public interface LazyReferenceManager
{
	public void register(Lazy<?> lazyReference);

	public void cleanUp(long nanoTimeBudget);

	public default void cleanUp()
	{
		this.cleanUp(Long.MAX_VALUE);
	}
	
	public void cleanUp(long nanoTimeBudget, Lazy.Checker checker);

	public default void cleanUp(final Lazy.Checker checker)
	{
		this.cleanUp(Long.MAX_VALUE, checker);
	}

	public void clear();

	public LazyReferenceManager start();

	public LazyReferenceManager stop();
	
	public boolean isRunning();

	public <P extends Consumer<? super Lazy<?>>> P iterate(P procedure);

	

	public static LazyReferenceManager set(final LazyReferenceManager referenceManager)
	{
		return Static.set(referenceManager);
	}
	
	public static boolean isSet()
	{
		return Static.isSet();
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
		
		static synchronized boolean isSet()
		{
			return globalReferenceManager != DUMMY;
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

	public final class Clearer implements Checker
	{
		@Override
		public boolean check(final Lazy<?> lazyReference)
		{
			lazyReference.clear();
			return true;
		}
	}

	
	
	public static LazyReferenceManager New()
	{
		return New(Lazy.Checker());
	}

	public static LazyReferenceManager New(final long millisecondTimeout)
	{
		return New(Lazy.Checker(millisecondTimeout));
	}
	
	public static LazyReferenceManager New(final Check customCheck)
	{
		return New(Lazy.Checker(customCheck));
	}

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
			_longReference.New(milliTimeCheckInterval),
			_longReference.New(nanoTimeBudget)
		);
	}

	public static LazyReferenceManager New(
		final Checker        checker                       ,
		final _longReference milliTimeCheckIntervalProvider,
		final _longReference nanoTimeBudgetProvider
	)
	{
		return New(_booleanReference.True(), checker, milliTimeCheckIntervalProvider, nanoTimeBudgetProvider);
	}
	
	public static LazyReferenceManager New(final _booleanReference parentRunningState)
	{
		return New(
			parentRunningState,
			Lazy.Checker(),
			_longReference.New(Default.DEFAULT_CHECK_INTERVAL_MS),
			_longReference.New(Default.DEFAULT_TIME_BUDGET_NS)
		);
	}
	
	public static LazyReferenceManager New(
		final _booleanReference parentRunningState            ,
		final Checker           checker                       ,
		final _longReference    milliTimeCheckIntervalProvider,
		final _longReference    nanoTimeBudgetProvider
	)
	{
		return new Default(parentRunningState, checker, milliTimeCheckIntervalProvider, nanoTimeBudgetProvider);
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

		private final _booleanReference parentRunningState            ;
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
			final _booleanReference parentRunningState    ,
			final Checker           checker               ,
			final _longReference    checkIntervalProvider ,
			final _longReference    nanoTimeBudgetProvider
		)
		{
			super();
			this.parentRunningState             = parentRunningState    ;
			this.checker                        = checker               ;
			this.millitimeCheckIntervalProvider = checkIntervalProvider ;
			this.nanoTimeBudgetProvider         = nanoTimeBudgetProvider;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////
		
		private long calculateNanoTimeBudgetBound(final long nanoTimeBudget)
		{
			final long timeBudgetBound = System.nanoTime() + nanoTimeBudget;

			// giving a very high or MAX_VALUE (unlimited) time budget will cause negative values
			return timeBudgetBound >= 0
				? timeBudgetBound
				: Long.MAX_VALUE
			;
		}
		
		public void DEBUG_printLoadCount(final String label)
		{
			final int count = this.iterate(new Consumer<Lazy<?>>()
			{
				int count;

				@Override
				public void accept(final Lazy<?> t)
				{
					if(t.peek() != null)
					{
						this.count++;
					}
				}
			}).count;
			
			XDebug.println('\n' + label + " Lazy loaded count = " + count);
		}

		final void internalCleanUp(final long nanoTimeBudget, final Checker checker)
		{
			/* (22.06.2016 TM)FIXME: full clear does not clear fully
			 * productive use of the full clear call clears only like 6 of 300 references on a regular basis.
			 * Sometimes all, but most of the time not.
			 * Even though the application is single threaded as far as lazy reference creation is concerned
			 * (simple "main test" class execution)
			 */
			
			final long timeBudgetBound = this.calculateNanoTimeBudgetBound(nanoTimeBudget);

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

//			this.DEBUG_printLoadCount("Before cycle:");
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

//			this.DEBUG_printLoadCount("After cycle:");
			checker.endCheckCycle();
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
			this.internalCleanUp(nanoTimeBudget, this.checker);
		}
		
		@Override
		public void cleanUp(final long nanoTimeBudget, final Lazy.Checker checker)
		{
			this.internalCleanUp(nanoTimeBudget, checker);
		}
		
		@Override
		public final synchronized boolean isRunning()
		{
			return this.running && this.parentRunningState.get();
		}

		@Override
		public synchronized LazyReferenceManager start()
		{
			// check for already running condition to avoid starting more than more thread
			if(this.parentRunningState.get() && !this.running)
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
			private final _longReference                              checkIntervalProvider;

			LazyReferenceCleanupThread(
				final WeakReference<LazyReferenceManager.Default> parent,
				final _longReference checkIntervalProvider
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
						if(!parent.isRunning())
						{
							break;
						}

						// perform check
						parent.cleanUpBudgeted();

						// very nasty: must clear the reference from the stack in order for the WeakReference to work
						parent = null;

						// extra nasty: must sleep with nulled reference for WeakReference to work, not before.
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
						/*
						 * Thread may not die on any exception, just continue looping
						 * as long as parent exists and running is true
						 */
					}
				}
				
				// either parent has been garbage collected or stopped, so terminate.
				XDebug.println(Thread.currentThread().getName() + " terminating.");
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
		public final void cleanUp(final long nanoTimeBudget, final Checker checker)
		{
			// no-op dummy
		}
		
		@Override
		public final boolean isRunning()
		{
			return false;
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

	
	
	@FunctionalInterface
	public interface CycleEvaluator
	{
		public void evaluateCycle(MemoryUsage memoryUsage, long cycleClearCount, double memoryQuota);
				
	}

}
