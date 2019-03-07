package one.microstream.util.pooling;

import java.util.function.Consumer;
import java.util.function.Supplier;

import one.microstream.exceptions.ExceptionCreator;

public interface Pool<E> extends Supplier<E>
{
	public interface Configuration<E>
	{
		public int getMaximumCount();

		public long getPoolCheckInterval();

		public Supplier<E> getSupplier();

		public Consumer<? super E> getDispatcher();

		public Consumer<? super E> getReturner();

		public Consumer<? super E> getCloser();

		public Pool.WaitTimeoutProvider getWaitTimeoutProvider();

		public Pool.WaitIntervalProvider getWaitIntervalProvider();

		public Controller getController();

		public ExceptionCreator<? extends RuntimeException> getExceptionCreator();



		public class Implementation<E> implements Configuration<E>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields  //
			/////////////////////

			final int                                          maximumCount        ;
			final long                                         poolCheckInterval   ;
			final Supplier<E>                                  supplier            ;
			final Consumer<? super E>                         dispatcher          ;
			final Consumer<? super E>                         returner            ;
			final Consumer<? super E>                         closer              ;
			final Pool.WaitTimeoutProvider                     waitTimeoutProvider ;
			final Pool.WaitIntervalProvider                    waitIntervalProvider;
			final Controller                                   controller          ;
			final ExceptionCreator<? extends RuntimeException> exceptionCreator    ;



			///////////////////////////////////////////////////////////////////////////
			// constructors     //
			/////////////////////

			public Implementation(
				final int                                          maximumCount        ,
				final long                                         poolCheckInterval   ,
				final Supplier<E>                                  supplier            ,
				final Consumer<? super E>                          dispatcher          ,
				final Consumer<? super E>                          returner            ,
				final Consumer<? super E>                          closer              ,
				final Pool.WaitTimeoutProvider                     waitTimeoutProvider ,
				final Pool.WaitIntervalProvider                    waitIntervalProvider,
				final Controller                                   controller          ,
				final ExceptionCreator<? extends RuntimeException> exceptionCreator
			)
			{
				super();
				this.maximumCount         = maximumCount        ;
				this.poolCheckInterval    = poolCheckInterval   ;
				this.supplier             = supplier            ;
				this.dispatcher           = dispatcher          ;
				this.returner             = returner            ;
				this.closer               = closer              ;
				this.waitTimeoutProvider  = waitTimeoutProvider ;
				this.waitIntervalProvider = waitIntervalProvider;
				this.controller           = controller          ;
				this.exceptionCreator     = exceptionCreator    ;
			}



			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public int getMaximumCount()
			{
				return this.maximumCount;
			}

			@Override
			public long getPoolCheckInterval()
			{
				return this.poolCheckInterval;
			}

			@Override
			public Supplier<E> getSupplier()
			{
				return this.supplier;
			}

			@Override
			public Consumer<? super E> getDispatcher()
			{
				return this.dispatcher;
			}

			@Override
			public Consumer<? super E> getReturner()
			{
				return this.returner;
			}

			@Override
			public Consumer<? super E> getCloser()
			{
				return this.closer;
			}

			@Override
			public WaitTimeoutProvider getWaitTimeoutProvider()
			{
				return this.waitTimeoutProvider;
			}

			@Override
			public WaitIntervalProvider getWaitIntervalProvider()
			{
				return this.waitIntervalProvider;
			}

			@Override
			public Controller getController()
			{
				return this.controller;
			}

			@Override
			public ExceptionCreator<? extends RuntimeException> getExceptionCreator()
			{
				return this.exceptionCreator;
			}

		}

	}

	@Override
	public E get();

	public void takeBack(E element);

	public int getMaximumCount();

	public int getTotalCount();

	public int getFreeCount();

	public int getUsedCount();


	@FunctionalInterface
	public interface Controller
	{
		public int calculateCloseCount(
			int  maxConnectionCount  ,
			int  totalConnectionCount,
			int  freeConnectionCount ,
			long lastGetTime         ,
			long lastReturnTime      ,
			long lastCloseTime
		);
	}

	@FunctionalInterface
	public interface WaitTimeoutProvider
	{
		public long provideWaitTimeout();
	}

	@FunctionalInterface
	public interface WaitIntervalProvider
	{
		public long provideWaitInterval();
	}

}
