package one.microstream.functional;

/*-
 * #%L
 * microstream-base
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

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;
import static one.microstream.concurrency.XThreads.start;
import static one.microstream.math.XMath.positive;

import java.util.function.Consumer;

import one.microstream.collections.BulkList;
import one.microstream.typing.XTypes;


public interface ParallelProcedure<E> extends Consumer<E>
{
	/**
	 * Applies a wrapped logic by delegating its execution to another thread.
	 *
	 * @param element the element to have the logic applied to.
	 */
	@Override
	public void accept(E element);



	public interface ThreadCountProvider
	{
		public int maxThreadCount();

		public class Constant implements ThreadCountProvider
		{
			private final int maxThreadCount;

			public Constant(final int maxThreadCount)
			{
				super();
				this.maxThreadCount = positive(maxThreadCount);
			}

			@Override
			public int maxThreadCount()
			{
				return this.maxThreadCount;
			}
		}
	}

	public interface ThreadTimeoutProvider
	{
		public int threadTimeout();

		public class Constant implements ThreadTimeoutProvider
		{
			private final int threadTimeout;

			public Constant(final int threadTimeout)
			{
				super();
				this.threadTimeout = positive(threadTimeout);
			}

			@Override
			public int threadTimeout()
			{
				return this.threadTimeout;
			}
		}
	}



	/**
	 * Type providing the actual logic {@link Consumer} to be applied to the elements encountered by a
	 * {@link ParallelProcedure}. Providing can mean anything from creating a new lightweight instance on each call
	 * of {@link #provideLogic()} to lazy creation caching or pooling heavyweight instances.
	 */
	public interface LogicProvider<S, P extends Consumer<? super S>>
	{
		/**
		 * Provides the actual logic {@link Consumer} to be applied to the elements encountered by a
		 * {@link ParallelProcedure}.
		 * <p>
		 * This method always gets called by the actual worker thread that will
		 * use the provided logic, so that a call of {@link Thread#currentThread()} inside the method
		 * will always return the logic executing thread. The intention is to give complex provider implementations
		 * a chance to get to know the executing thread before the logic is executed. It is however stronly
		 * discouraged to try and control the worker thread activity from outside as it can disturb or disrupt
		 * the actual thread management. E.g. any explicit interruption can cause the worker thread to be
		 * abolished and killed and the logic instance to be disposed.
		 *
		 * @return the logic instance to be used by a worker thread.
		 */
		public P provideLogic();

		/**
		 * Signals the provider instance that the passed logic instance is no longer used and provides the cause
		 * for its disposal (e.g. an {@link InterruptedException} because the executing worker has been abolished)
		 * <p>
		 * An implementation of this method can be anything from a no-op to complex resource clean up.
		 *
		 * @param logic the no longer used logic that shall be disposed.
		 * @param cause the cause for the disposal.
		 */
		public void disposeLogic(P logic, Throwable cause);



		public final class SingletonLogic<S, P extends Consumer<? super S>> implements LogicProvider<S, P>
		{
			private final P logic;

			public SingletonLogic(final P logic)
			{
				super();
				this.logic = logic;
			}

			@Override
			public P provideLogic()
			{
				return this.logic;
			}

			@Override
			public void disposeLogic(final P logic, final Throwable cause)
			{
				// no-op.
			}

		}

	}



	/**
	 * Sample implementation with a user defined number of maximum worker threads and a user defined timeout (in ms)
	 * after which one worker thread will be abolished.
	 * <p>
	 * Worker threads will be created or abolished in line with the number of elements to be handled, the specified
	 * maximum thread count and the specified thread timeout.<br>
	 * Thread abolishment is controlled via calls of {@link Thread#interrupt()} from the thread management logic.
	 * Also, any other {@link Throwable} thrown by the logic {@link Consumer} will cause the throwing worker thread
	 * to be abolished as well and the logic instance to be disposed.
	 * <p>
	 * Locking is done via the instance itself. There are opinions that say it is preferable to do locking via a
	 * dedicated internal unshared locking object to prevent "lock stealing" from outside. However, obtaining
	 * the particular lock in an outside context can also be desired or even needed. E.g.: synchronize on the
	 * procedure instance itself in the calling context, apply the procedure to all elements in a collection and then
	 * release the lock (leave the synchronized block) to start all worker threads as soon all the collection's elements
	 * have been collected. It is probably due to requirements like this that lock hiding strategies get extended
	 * by means to publicly present their hidden lock instance. But then that defeats the purpose of lock hiding
	 * altogether and yields the direct and simpler syntactical way of using this-synchronized internal methods the
	 * superior one. In the end, synchronizing/locking on the instance itself is fine. It just may not be messed up.
	 * As with anything else in writing code.
	 */
	public final class Default<E> implements ParallelProcedure<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final int                   DEFAULT_THREAD_TIMEOUT          = 1000;
		private static final ThreadTimeoutProvider DEFAULT_THREAD_TIMEOUT_PROVIDER = () -> DEFAULT_THREAD_TIMEOUT;



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final LogicProvider<? super E, ? extends Consumer<? super E>> logicProvider;

		private final BulkList<WorkerThread>  threads              ;
		private final ThreadCountProvider     threadCountProvider  ;
		private final ThreadTimeoutProvider   threadTimeoutProvider;

		private Entry<E> head, tail;
		private long lastTouched = System.currentTimeMillis();



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final LogicProvider<? super E, ? extends Consumer<? super E>> logicProvider,
			final int threadCount
		)
		{
			this(logicProvider, new ThreadCountProvider.Constant(threadCount), DEFAULT_THREAD_TIMEOUT_PROVIDER);
		}

		public Default(
			final LogicProvider<? super E, ? extends Consumer<? super E>> logicProvider,
			final int threadCount,
			final int threadTimeout
		)
		{
			this(
				logicProvider,
				new ThreadCountProvider.Constant(threadCount),
				new ThreadTimeoutProvider.Constant(threadTimeout)
			);
		}

		/**
		 * Alias for {@code Default(logicProvider, maxThreadCount, 1000)}.
		 *
		 * @param logicProvider the instance that provides the logic instances to be used by the worker threads.
		 * @param threadCountProvider the maximum number of concurrent threads to be created by this instance.
		 */
		public Default(
			final LogicProvider<? super E, ? extends Consumer<? super E>> logicProvider      ,
			final ThreadCountProvider                                     threadCountProvider
		)
		{
			this(logicProvider, threadCountProvider, null);
		}

		/**
		 * Validates the passed parameters but does neither create threads nor call any of the logic provider's methods.
		 * <p>
		 * Valid arguments are:
		 * <ul>
		 * <li>non-null logic provider instance</li>
		 * <li>positive max thread count (greater than 0)</li>
		 * <li>positive thread timeout (greater than 0) in milliseconds</li>
		 * </ul>
		 * Note that a low thread timeout can cause high overhead of frequent thread creation and destruction.
		 * A value of at least 100 or higher is advised.<br>
		 * Also note that a thread count of 1 defeats the purpose of parallelism and that a very high thread count
		 * (depending on the system, like 10 to 100) will cause unnecessary thread management overhead in most cases.
		 *
		 * @param logicProvider the instance that provides the logic instances to be used by the worker threads.
		 * @param threadCountProvider the maximum number of concurrent threads to be created by this instance.
		 * @param threadTimeout the thread abolishment timeout in milliseconds.
		 */
		public Default(
			final LogicProvider<? super E, ? extends Consumer<? super E>> logicProvider      ,
			final ThreadCountProvider                                     threadCountProvider,
			final ThreadTimeoutProvider                                   threadTimeout
		)
		{
			super();
			this.logicProvider         =  notNull(logicProvider);
			this.threadCountProvider   =  notNull(threadCountProvider);
			this.threadTimeoutProvider = coalesce(threadTimeout, DEFAULT_THREAD_TIMEOUT_PROVIDER);
			this.threads               = new BulkList<>();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void touch()
		{
			this.lastTouched = System.currentTimeMillis();
		}

		private void checkWorkerCreation()
		{
			/* intentionally no check for current queue size because if items come in faster than
			 * the timeout, the max thread number will probably be needed soon, anyway.
			 * The intention is to build up max threads quickly and remove them slowly over time.
			 */
			if(XTypes.to_int(this.threads.size()) < this.threadCountProvider.maxThreadCount())
			{
				this.threads.add(start(this.createWorkerThread()));
			}
		}

		private WorkerThread createWorkerThread()
		{
			return new WorkerThread(XTypes.to_int(this.threads.size()));
		}

		private boolean isTimedOut()
		{
			return System.currentTimeMillis() - this.lastTouched > this.threadTimeoutProvider.threadTimeout();
		}

		private boolean isOversized()
		{
			return this.threadCountProvider.maxThreadCount() < XTypes.to_int(this.threads.size());
		}

		private void checkThreadTimeout()
		{
			if((this.isTimedOut() || this.isOversized()) && XTypes.to_int(this.threads.size()) > 0)
			{
				this.touch();                    // touch here to make threads shutdown one by one over time
				this.threads.last().interrupt(); // intentionally no pick() because thread gets removed explicitly
			}
		}

		public int currentThreadCount()
		{
			synchronized(this.threads)
			{
				// don't disturb worker threads via this, but synchronize thread cache
				return XTypes.to_int(this.threads.size());
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// enqueing //
		/////////////

		@Override
		public final void accept(final E element)
		{
			this.enqueueEntry(new Entry<>(element)); // instantiate before synchronization and syntactically only once
		}

		private synchronized void enqueueEntry(final Entry<E> newEntry)
		{
			this.tail = this.head == null ? (this.head = newEntry) : (this.tail.next = newEntry);
			this.touch();
			this.checkWorkerCreation();
			this.notifyAll();
		}

		private static final class Entry<E>
		{
			final E  element;
			Entry<E> next;

			Entry(final E element)
			{
				super();
				this.element = element;
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// retrieval & threading //
		//////////////////////////

		private final class WorkerThread extends Thread
		{
			final int number;

			WorkerThread(final int number)
			{
				super();
				this.number = number;
			}

			@Override
			public void run()
			{
				Default.this.runWorker(this);
			}
		}

		// (19.03.2014 TM)TODO: fix unrelated two S type parameters in LogicProvider
		@SuppressWarnings("unchecked")
		final <S, E1 extends S, P extends Consumer<? super S>> void runWorker(final WorkerThread worker)
		{
			Default.delegateRun(
				(Default<E1>)this,
				(Default<E1>.WorkerThread)worker,
				(LogicProvider<S, P>)this.logicProvider
			);
		}

		static <S, E extends S, P extends Consumer<? super S>> void delegateRun(
			final Default<E>              instance,
			final Default<E>.WorkerThread thread  ,
			final LogicProvider<S, P>            provider
		)
		{
			// outer try layer required in case providing the logic fails
			try
			{
				// get actual logic just here to have the actual worker thread invoke the providing method
				final P logic = provider.provideLogic();
				thread.setName(buildThreadName(logic.getClass(), thread.number));
				try
				{
					while(true)
					{
						// run until thread is interrupted (abolished) or logic encounters any kind of problem
						logic.accept(instance.get());
					}
				}
				catch(final Throwable t)
				{
					provider.disposeLogic(logic, t); // only way to make compiler recognize logic's type here
				}
			}
			finally
			{
				instance.removeThread(thread);
			}
		}

		static String buildThreadName(final Class<?> logicClass, final int threadNumber)
		{
			String logicClassName = logicClass.getName();
			for(int i = logicClassName.length(); i-- > 0;)
			{
				if(logicClassName.charAt(i) == '.')
				{
					logicClassName = logicClassName.substring(i + 1);
					break;
				}
			}
			return "Worker-" + threadNumber + '-' + logicClassName;
		}

		final synchronized E get() throws InterruptedException
		{
			while(this.head == null)
			{
				this.wait(this.threadTimeoutProvider.threadTimeout()); // wait timout has to be thread timeout
				this.checkThreadTimeout(); // if nothing to do, check for timeout and possibly abolish one thread
			}

			final E element = this.head.element;
			this.head = this.head.next;
			return element;
		}

		final synchronized void removeThread(final WorkerThread thread)
		{
			this.threads.removeOne(thread); // gets called for last thread in the list most of the time
		}

	}

}
