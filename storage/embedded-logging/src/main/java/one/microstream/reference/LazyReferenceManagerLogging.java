package one.microstream.reference;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.reference.Lazy.Checker;
import one.microstream.storage.types.StorageLoggingWrapper;

public interface LazyReferenceManagerLogging extends LazyReferenceManager, StorageLoggingWrapper<LazyReferenceManager>
{
	public static void New()
	{
		final LazyReferenceManager clone = LazyReferenceManager.New(
				LazyCheckerLogging.New(
					LazyReferenceManager.get().getChecker()
				)
			);
		
		LazyReferenceManager.set(LazyReferenceManagerLogging.New(clone));
	}
	
	public static LazyReferenceManagerLogging New(
		final LazyReferenceManager wrapped
	)
	{
		return new Default(notNull(wrapped));
	}
	
	public static class Default
		extends StorageLoggingWrapper.Abstract<LazyReferenceManager>
		implements LazyReferenceManagerLogging
	{
		
		protected Default(
			final LazyReferenceManager wrapped
		)
		{
			super(wrapped);
		}

		@Override
		public void register(final Lazy<?> lazyReference)
		{
			this.logger().lazyReferenceManager_beforerRegister(lazyReference);
			
			this.wrapped().register(lazyReference);
		}

		@Override
		public LazyReferenceManager registerAll(final LazyReferenceManager other)
		{
			return this.wrapped().registerAll(other);
		}

		@Override
		public void cleanUp(final long nanoTimeBudget)
		{
			this.wrapped().cleanUp(nanoTimeBudget);
		}

		@Override
		public void cleanUp()
		{
			this.wrapped().cleanUp();
		}

		@Override
		public void cleanUp(final long nanoTimeBudget, final Checker checker)
		{
			this.wrapped().cleanUp(nanoTimeBudget, checker);
		}

		@Override
		public void cleanUp(final Checker checker)
		{
			this.wrapped().cleanUp(checker);
		}

		@Override
		public void clear()
		{
			this.wrapped().clear();
		}

		@Override
		public LazyReferenceManager start()
		{
			this.logger().lazyReferenceManager_beforeStart(this);
			
			final LazyReferenceManager lazyReferenceManager = this.wrapped().start();
			
			this.logger().lazyReferenceManager_afterStart(this);
			
			return lazyReferenceManager;
		}

		@Override
		public LazyReferenceManager stop()
		{
			this.logger().lazyReferenceManager_beforeStop(this);
			
			final LazyReferenceManager lazyReferenceManager = this.wrapped().stop();
			
			this.logger().lazyReferenceManager_afterStop(this);
			
			return lazyReferenceManager;
		}

		@Override
		public LazyReferenceManager addController(final Controller controller)
		{
			return this.wrapped().addController(controller);
		}

		@Override
		public boolean removeController(final Controller controller)
		{
			return this.wrapped().removeController(controller);
		}

		@Override
		public boolean isRunning()
		{
			return this.wrapped().isRunning();
		}

		@Override
		public <P extends Consumer<? super Lazy<?>>> P iterate(final P iterator)
		{
			return this.wrapped().iterate(iterator);
		}

		@Override
		public <P extends Consumer<? super Controller>> P iterateControllers(final P iterator)
		{
			return this.wrapped().iterateControllers(iterator);
		}
		
		@Override
		public Checker getChecker()
		{
			return this.wrapped().getChecker();
		}
		
	}

}
