package one.microstream.reference;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import one.microstream.memory.MemoryStatistics;
import one.microstream.meta.XDebug;
import one.microstream.reference.Lazy.Check;
import one.microstream.reference.Lazy.Checker;
import one.microstream.time.XTime;

public interface LazyReferenceManager
{
	public void register(Lazy<?> lazyReference);
	
	public LazyReferenceManager registerAll(LazyReferenceManager other);

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
	
	public LazyReferenceManager addController(LazyReferenceManager.Controller controller);
	
	public boolean removeController(LazyReferenceManager.Controller controller);
	
	public boolean isRunning();

	public <P extends Consumer<? super Lazy<?>>> P iterate(P iterator);

	public <P extends Consumer<? super LazyReferenceManager.Controller>> P iterateControllers(P iterator);

	

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
		static LazyReferenceManager globalReferenceManager = LazyReferenceManager.New();

		static synchronized LazyReferenceManager set(final LazyReferenceManager referenceManager)
		{
			final LazyReferenceManager old = globalReferenceManager;
			referenceManager.registerAll(old);
			globalReferenceManager = referenceManager;
			
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
		 * @throws UnsupportedOperationException when called
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
		final Checker           checker                       ,
		final _longReference    milliTimeCheckIntervalProvider,
		final _longReference    nanoTimeBudgetProvider
	)
	{
		return new Default(checker, milliTimeCheckIntervalProvider, nanoTimeBudgetProvider);
	}

	public final class Default implements LazyReferenceManager
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final Clearer CLEARER = new Clearer();

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

        private boolean         running        ;
		private ControllerEntry headController ;
		private long            controllerCount;

		
		
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
		
		private synchronized boolean mayRun()
		{
			if(this.headController == null)
			{
				// if no external controller is or was present, the LRM controls itself on its own.
				return this.controllerCount == 0;
			}
			
			// check for orphaned head controller and consolidate to next non-orphaned one (or null!)
			final LazyReferenceManager.Controller ac;
			if((ac = this.headController.get()) == null)
			{
				this.headController = this.headController.consolidateSelf();
				
				// mus call recursively in case null is returned or GC cleared a weak reference in the mean time.
				return this.mayRun();
			}
			
			return ac.mayRun()
				? true
				: this.headController.checkChain()
			;
		}
		
		// NOT threadsafe! Must be secured by accessing outer LRM methods
		static final class ControllerEntry extends WeakReference<LazyReferenceManager.Controller>
		{
			ControllerEntry next;
			
			ControllerEntry(final LazyReferenceManager.Controller controller)
			{
				super(controller);
			}
			
			final boolean checkChain()
			{
				return this.next != null && this.next.isEnabled(this);
			}

			final boolean isEnabled(final ControllerEntry last)
			{
				final LazyReferenceManager.Controller controller;
				if((controller = this.get()) == null)
				{
					// if this chain entry is an orphan AND the last one, the default value is returned
					if((last.next = this.next) == null)
					{
						return false;
					}
					
					// if it is not the last one, the next entry is asked/checked
					return this.next.isEnabled(last);
				}
				
				// if at least one existing controllers returns true, then true it is.
				if(controller.mayRun())
				{
					return true;
				}

				// check next controller if present. Otherwise, this controler's false counts
				return this.next != null && this.next.isEnabled(this);
			}
			
			static final ControllerEntry consolidate(final ControllerEntry root)
			{
				ControllerEntry current = root;
				while(current != null && current.get() == null)
				{
					current = current.next;
				}
				if(current != null)
				{
					current.consolidateTail();
				}
				
				return current;
			}
			
			final ControllerEntry consolidateSelf()
			{
				return consolidate(this);
			}
			
			final ControllerEntry consolidateTail()
			{
				this.next = consolidate(this.next);
				
				return this;
			}
			
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
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
			/* (22.06.2016 TM)NOTE: full clear does not clear fully
			 * productive use of the full clear call clears only like 6 of 300 references on a regular basis.
			 * Sometimes all, but most of the time not.
			 * Even though the application is single threaded as far as lazy reference creation is concerned
			 * (simple "main test" class execution)
			 * 
			 * (06.02.2020 TM)NOTE: since then, the LRM has been massively overhauled.
			 * However, the basic logic of entry iteration and the "Clearer" checker remained unchanged.
			 * So the age old note might still be relevant.
			 */
			
			final long timeBudgetBound = XTime.calculateNanoTimeBudgetBound(nanoTimeBudget);

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
		public synchronized LazyReferenceManager registerAll(final LazyReferenceManager other)
		{
			if(other == this)
			{
				throw new IllegalArgumentException(
					"Other " + LazyReferenceManager.class.getSimpleName() + " may not be this."
				);
			}
			
			other.iterate(lr ->
				this.register(lr)
			);
			
			other.iterateControllers(ac ->
				this.addController(ac)
			);
			
			return this;
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
			return this.running && this.mayRun();
		}

		@Override
		public synchronized LazyReferenceManager start()
		{
			// check for already running condition to avoid starting more than more thread
			if(!this.running && this.mayRun())
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
		public final synchronized LazyReferenceManager addController(
			final LazyReferenceManager.Controller controller
		)
		{
			if(controller == null)
			{
				return this;
			}
			
			// either set as head instance or scroll to the end and add as tail instance.
			if(this.headController == null)
			{
				this.headController = new ControllerEntry(controller);
				this.controllerCount++;
				
				return this;
			}
			
			ControllerEntry current = this.headController;
			while(current.next != null)
			{
				// no need for orphan removal logic here as the checking logic already does that on every check
				if(current.get() == controller)
				{
					return this;
				}
				current = current.next;
			}
			current.next = new ControllerEntry(controller);
			this.controllerCount++;
			
			return this;
		}

		@Override
		public final synchronized boolean removeController(
			final LazyReferenceManager.Controller controller
		)
		{
			// head entry special case
			if(this.headController != null && this.headController.get() == controller)
			{
				// adding logic ensures there can be at the most one entry, so one match suffices to end the loop.
				this.headController = this.headController.next;
				this.controllerCount--;
				this.stopIfNoControllers();
				return true;
			}
			
			if(this.headController == null)
			{
				// no (more) controllers present, hence passed controller not found.
				return false;
			}
			
			// normal case loop starting with a non-null, non-matching head entry
			ControllerEntry last = this.headController;
			for(ControllerEntry e; (e = last.next) != null; last = e)
			{
				// no need for orphan removal logic here as the checking logic already does that on every check
				if(e.get() == controller)
				{
					// remove chain element by replacing the reference to it by that to its next.
					last.next = e.next;
					this.controllerCount--;
					
					// adding logic ensures there can be at the most one entry, so one match suffices to end the loop.
					this.stopIfNoControllers();
					return true;
				}
			}
			
			// passed controller not found
			return false;
		}

		/**
		 * When there are no controllers attached to this object, stop the LazyReferenceManager so that no
		 * daemon threads running anymore and program can exit normally.
		 */
		private void stopIfNoControllers()
		{
			if (this.controllerCount == 0)
			{
				this.stop();
			}
		}

		@Override
		public <P extends Consumer<? super LazyReferenceManager.Controller>> P iterateControllers(
			final P iterator
		)
		{
			for(ControllerEntry acc = this.headController; acc != null; acc = acc.next)
			{
				final LazyReferenceManager.Controller ac;
				if((ac = acc.get()) != null)
				{
					iterator.accept(ac);
				}
			}
			return iterator;
		}

		@Override
		public synchronized <P extends Consumer<? super Lazy<?>>> P iterate(final P iterator)
		{
			for(Entry e = this.head; (e = e.nextLazyManagerEntry) != null;)
			{
				final Lazy<?> ref = e.get();
				if(ref != null)
				{
					iterator.accept(ref);
				}
			}
			return iterator;
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
//				XDebug.println(Thread.currentThread().getName() + " terminating.");
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

	
	@FunctionalInterface
	public interface Controller
	{
		public boolean mayRun();
	}
	
	
	@FunctionalInterface
	public interface CycleEvaluator
	{
		public void evaluateCycle(MemoryStatistics memoryStatistics, long cycleClearCount, double memoryQuota);
				
	}

}
