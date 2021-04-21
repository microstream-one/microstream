package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.storage.exceptions.StorageException;

public interface StorageSystemLogging extends StorageSystem, StorageLoggingWrapper<StorageSystem>
{
	public static StorageSystemLogging New(
		final StorageSystem wrapped
	)
	{
		return new Default(notNull(wrapped));
	}
	
	public static class Default extends StorageLoggingWrapper.Abstract<StorageSystem> implements StorageSystemLogging
	{
		protected Default(final StorageSystem delegate)
		{
			super(delegate);
		}

		@Override
		public boolean isActive()
		{
			return this.wrapped().isActive();
		}

		@Override
		public StorageRequestAcceptor createRequestAcceptor()
		{
			return this.wrapped().createRequestAcceptor();
		}

		@Override
		public StorageTypeDictionary typeDictionary()
		{
			return this.wrapped().typeDictionary();
		}

		@Override
		public StorageOperationController operationController()
		{
			return this.wrapped().operationController();
		}

		@Override
		public StorageChannelCountProvider channelCountProvider()
		{
			return this.wrapped().channelCountProvider();
		}

		@Override
		public boolean isAcceptingTasks()
		{
			return this.wrapped().isAcceptingTasks();
		}

		@Override
		public boolean isRunning()
		{
			return this.wrapped().isRunning();
		}

		@Override
		public boolean isStartingUp()
		{
			return this.wrapped().isStartingUp();
		}

		@Override
		public boolean isShuttingDown()
		{
			return this.wrapped().isShuttingDown();
		}

		@Override
		public StorageConfiguration configuration()
		{
			return this.wrapped().configuration();
		}

		@Override
		public boolean isShutdown()
		{
			return this.wrapped().isShutdown();
		}

		@Override
		public StorageSystem start()
		{
			return this.wrapped().start();
		}

		@Override
		public void checkAcceptingTasks()
		{
			this.wrapped().checkAcceptingTasks();
		}

		@Override
		public StorageIdAnalysis initializationIdAnalysis()
		{
			return this.wrapped().initializationIdAnalysis();
		}

		@Override
		public long initializationTime()
		{
			return this.wrapped().initializationTime();
		}

		@Override
		public boolean shutdown()
		{
			return this.wrapped().shutdown();
		}

		@Override
		public long operationModeTime()
		{
			return this.wrapped().operationModeTime();
		}

		@Override
		public StorageObjectIdRangeEvaluator objectIdRangeEvaluator()
		{
			return this.wrapped().objectIdRangeEvaluator();
		}

		@Override
		public long initializationDuration()
		{
			return this.wrapped().initializationDuration();
		}

		@Override
		public void close() throws StorageException
		{
			this.wrapped().close();
		}
	}
}
