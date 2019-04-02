package one.microstream.util.pooling;

import static one.microstream.X.notNull;
import static one.microstream.collections.XUtilsCollection.rngProcess;
import static one.microstream.math.XMath.positive;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import one.microstream.X;
import one.microstream.collections.HashEnum;
import one.microstream.concurrency.Threaded;
import one.microstream.concurrency.ThreadedInstantiating;
import one.microstream.exceptions.ExceptionCreator;
import one.microstream.functional.Instantiator;
import one.microstream.typing.Immutable;
import one.microstream.typing.XTypes;

public class ThreadedPool<E> implements Pool<E>
{
	public static final class DiscardSignal extends RuntimeException
	{
		// emtpy
	}

	public static final DiscardSignal DISCARD = new DiscardSignal();



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	static <E> Instantiator<HashEnum<E>> poolCreator()
	{
		// must be static to avoid creation of an implicit reference to outer instance which would prevent GC.
		return HashEnum::New;
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final Configuration<? super E>                     configuration       ;
	final HashEnum<E>                                  pool                ;
	final Threaded<HashEnum<E>>                        used                ;
	final Supplier<E>                                  supplier            ;
	final Consumer<? super E>                         returner            ;
	final Consumer<? super E>                         dispatcher          ;
	final Consumer<? super E>                         closer              ;
	final Pool.WaitTimeoutProvider                     waitTimeoutProvider ;
	final Pool.WaitIntervalProvider                    waitIntervalProvider;
	final Controller                                   controller          ;
	final ExceptionCreator<? extends RuntimeException> exceptionCreator    ;

	volatile int  maxCount      ;
	volatile int  totalCount    ;
	volatile long lastGetTime   ;
	volatile long lastReturnTime;
	volatile long lastCloseTime ;

	private final Consumer<E> repooler = this::repool;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ThreadedPool(final Configuration<E> configuration)
	{
		super();
		this.totalCount           = 0;
		this.configuration        = configuration;
		this.maxCount             = positive(configuration.getMaximumCount());
		this.returner             = notNull(configuration.getReturner());
		this.dispatcher           = notNull(configuration.getDispatcher());
		this.supplier             = notNull(configuration.getSupplier());
		this.closer               = notNull(configuration.getCloser());
		this.waitTimeoutProvider  = notNull(configuration.getWaitTimeoutProvider());
		this.waitIntervalProvider = notNull(configuration.getWaitIntervalProvider());
		this.controller           = notNull(configuration.getController());
		this.exceptionCreator     = notNull(configuration.getExceptionCreator());

		this.pool = HashEnum.New();
		this.used = new ThreadedInstantiating<>(ThreadedPool.<E>poolCreator())
			.setCleanUpOperation(new PoolCleaner<>(this))
		;

		this.lastGetTime = this.lastReturnTime = this.lastCloseTime = System.currentTimeMillis();
		new PoolManager<>(this, configuration.getPoolCheckInterval()).start();
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	@Override
	public int getMaximumCount()
	{
		return this.maxCount;
	}

	@Override
	public int getTotalCount()
	{
		synchronized(this.pool)
		{
			return XTypes.to_int(this.pool.size()) + XTypes.to_int(this.used.size());
		}
	}

	@Override
	public int getFreeCount()
	{
		synchronized(this.pool)
		{
			return XTypes.to_int(this.pool.size());
		}
	}

	@Override
	public int getUsedCount()
	{
		synchronized(this.pool)
		{
			// (20.07.2012 TM)FIXME: Shouldn't that accumulate all sizes of the thread local enums?
			return XTypes.to_int(this.used.size());
		}
	}


	void returnOrphans(final HashEnum<E> orphans)
	{
		orphans.iterate(this.repooler);
	}

	void repool(final E element)
	{
		try
		{
			this.returner.accept(element); // execute returner procedure on returned element. May be a no-op.
		}
		catch(final DiscardSignal s)
		{
			synchronized(this.pool)
			{
				this.totalCount--;          // decrement for discarded bad element
				this.pool.notifyAll();      // notify waiting threads that a new slot for a element is available
			}
			return; // do NOT throw an exception here as the returner just signaled to discard the returned element
		}
		catch(final RuntimeException e)
		{
			// sadly this redundancy cannot be consolidated due to language constructs
			synchronized(this.pool)
			{
				this.totalCount--;          // decrement for element discarded due to exception
				this.pool.notifyAll();      // notify waiting threads that a new slot for a element is available
			}
			throw e; // all other cases: throw caught exeption after count update
		}

		// normal case: return element to pool
		synchronized(this.pool)
		{
			this.pool.add(element);         // return still valid element to the pool
			this.pool.notifyAll();          // notify waiting threads that a free element or new slot is available
		}
	}



	@Override
	public E get()
	{
		E element;

		/* lock the pool, try to obtain a free or create a new element.
		 * If maximum element count is reached, wait for a free element.
		 * Note the extremely short lock time in all cases (especially compared
		 * to the relatively long execution time of anything one can do with the obtained element)
		 */
		synchronized(this.pool)
		{
			if((element = this.pool.pinch()) == null)
			{
				final long waitBound;
				final long waitTimeout = this.waitTimeoutProvider.provideWaitTimeout();
				waitBound = waitTimeout != 0 ? System.currentTimeMillis() + waitTimeout : Long.MAX_VALUE;

				while((element = this.pool.pinch()) == null)
				{
					if(System.currentTimeMillis() >= waitBound)
					{
						return null;
					}
					if(this.totalCount < this.maxCount)
					{
						// still room for new element, create one
						this.totalCount++; // increment count in anticipation of created element
						break; // break loop and thus exit synchronized block. Thread-local rest can be done unsynched
					}
					try
					{
						// wait for either a free element or a free slot to create a new one
						this.pool.wait(this.waitIntervalProvider.provideWaitInterval());
					}
					catch(final InterruptedException e)
					{
						/* well, thread has been interrupted while waiting for an element.
						 * Return without an element because whoever interrupted it obviously does not
						 * want it to obtain an element anymore.
						 * This is probably more consistent to the request to stop waiting for a element
						 * than passing along the checked Exception all the time.
						 * I surely won't propagate an InterruptedException in a method like this.
						 */
						if((element = this.pool.pinch()) == null)
						{
							return null;
						}
					}
				}
			}
		}

		// element is only null at this point if a count to create a new one has been reserved
		try
		{
			if(element == null)
			{
				// try to get a new element for which the count has already been reserved
				try
				{
					element = this.supplier.get();
				}
				catch(final Exception t)
				{
					// element retrieval failed. Roll back count increment and pass exception along.
					synchronized(this.pool)
					{
						this.totalCount--;
					}
					throw this.exceptionCreator.createException("Pool increase failed.", t);
				}
			}
			this.used.get().add(element);
			this.dispatcher.accept(element);
		}
		catch(final Exception t)
		{
			this.used.get().remove(element);
			synchronized(this.pool)
			{
				this.pool.add(element);
			}
			throw this.exceptionCreator.createException("Dispatch failed.", t);
		}

		// at this point, element is guaranteed to be not null and can be added to the thread local list of used cons.
		return element;
	}

	@Override
	public void takeBack(final E element)
	{
		if(!this.used.get().removeOne(element))
		{
			return; // element has never been handled by this pool or already returned
		}

		this.lastReturnTime = System.currentTimeMillis();
		this.repool(element);
	}

	void internalCheckPoolSize()
	{
		final int poolSize = X.checkArrayRange(this.pool.size());
		if(poolSize == 0)
		{
			// avoid calling the controller or getting a lock if pool size is 0 anyway
			return;
		}

		final int decrementAmount = this.controller.calculateCloseCount(
			this.maxCount,
			this.totalCount,
			XTypes.to_int(this.pool.size()),
			this.lastGetTime,
			this.lastReturnTime,
			this.lastCloseTime
		);
		if(decrementAmount <= 0)
		{
			// avoid acquiring a lock if there is no need to
			return;
		}
//		XDebug.debugln("DEBUG\n" +
//			"max:     " + this.maxCount + "\n" +
//			"total:   " + this.totalCount + "\n" +
//			"pool:    " + this.pool.size() + "\n" +
//			"lastGet: " + this.lastGetTime + "\n" +
//			"lastRet: " + this.lastReturnTime + "\n" +
//			"lastCls: " + this.lastCloseTime + "\n" +
//			"decremC: " + decrementAmount
//		);
		synchronized(this.pool)
		{
			// query effective pool size again after lock is acquired, just to be sure
			final int effectivePoolSize = X.checkArrayRange(this.pool.size());

			final int actualDecrement; // check pool size after a acquiring the lock
			if((actualDecrement = Math.min(effectivePoolSize, decrementAmount)) == 0)
			{
				return; // avoid calling processing stuff if there is no need to
			}
			this.totalCount -= actualDecrement;
			rngProcess(this.pool, poolSize - 1, -actualDecrement, this.closer);
		}
	}






	/*
	 * Must be a class instead of SAM instance in order to prevent self-sustaining referencing of the pool
	 * by it's cleaner.
	 *
	 * ... and that name is too funny :DD
	 */
	static class PoolCleaner<E> extends WeakReference<ThreadedPool<E>> implements Consumer<HashEnum<E>>
	{
		public PoolCleaner(final ThreadedPool<E> pool)
		{
			super(pool);
		}

		@Override
		public void accept(final HashEnum<E> e) throws RuntimeException
		{
			final ThreadedPool<E> pool;
			if((pool = this.get()) == null)
			{
				return; // parent pool has been collected somewhere in between or so.
			}
			pool.returnOrphans(e);
		}

	}

	static class PoolManager<E> extends Thread implements Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		private static final long MIN_CHECK_INTERVAL = 100; // anything below 100 ms loop time is not reasonable
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final WeakReference<ThreadedPool<E>> pool;
		private final long                           poolCheckInterval;

		// backup references to pool's element collections and closer
		private final Consumer<? super E>            poolCloser;
		private final HashEnum<E>                    poolPool;
		private final Threaded<HashEnum<E>>          poolUsed;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public PoolManager(final ThreadedPool<E> pool, final long poolCheckInterval)
		{
			super();
			if(poolCheckInterval < MIN_CHECK_INTERVAL)
			{
				throw new IllegalArgumentException();
			}
			this.poolCheckInterval = poolCheckInterval;
			this.pool = new WeakReference<>(pool);

			this.poolCloser = pool.closer;
			this.poolPool   = pool.pool;
			this.poolUsed   = pool.used;
		}

		@Override
		public void run()
		{
			ThreadedPool<E> pool;
			while((pool = this.pool.get()) != null)
			{
				// automatic thread termination if pool is garbage collected
				try
				{
					Thread.sleep(this.poolCheckInterval);
				}
				catch(final InterruptedException e)
				{
					// ignore interruption, the pool manager has to go on as long as the pool itself exists.
				}
				pool.internalCheckPoolSize();
			}

			this.poolPool.iterate(this.poolCloser);
			/*
			 * This is tricky: If ConnectionPool instance has been collected because it is not referenced
			 * any more by anyone, the responsibility to close still used elements has been implicitely
			 * taken over by the threads who borrowed the elements but don't reference the pool any more.
			 * Hence, clean up the thread local now once to close orphaned elements, but leave all remaining
			 * elements to the live threads.
			 * If thread implementations shall not have to take over element management responsibility, it is
			 * advised not to abandon the element pool reference prematurely in the first place.
			 */
			this.poolUsed.consolidate();
		}

	}

}
