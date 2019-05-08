package one.microstream.network.types;

import one.microstream.exceptions.MissingFoundationPartException;


public interface NetworkFactoryServerSessionless extends NetworkFactoryServer
{
	@Override
	public NetworkConnectionServer createServer();



	public class Default extends NetworkFactoryServer.Abstract
	implements NetworkFactoryServerSessionless
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private volatile NetworkConnectionProcessor connectionProcessor;



		///////////////////////////////////////////////////////////////////////////
		// setters //
		////////////

		protected final synchronized void internalSetConnectionProcessor(
			final NetworkConnectionProcessor connectionProcessor
		)
		{
			this.connectionProcessor = connectionProcessor;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public NetworkFactoryServerSessionless.Default setConnectionProcessorProvider(
			final NetworkConnectionProcessor.Provider<?> connectionProcessorProvider
		)
		{
			this.internalSetConnectionProcessorProvider(connectionProcessorProvider);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Default setConnectionSocket(
			final NetworkConnectionSocket connectionSocket
		)
		{
			this.internalSetConnectionSocket(connectionSocket);
			return this;
		}

		public NetworkFactoryServerSessionless.Default setConnectionProcessor(
			final NetworkConnectionProcessor connectionProcessor
		)
		{
			this.internalSetConnectionProcessor(connectionProcessor);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Default setConnectionListenerMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionListeners(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Default setConnectionProcessorMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionProcessors(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Default setConnectionListenerCheckInterval(
			final int interval
		)
		{
			this.internalSetIntervalCheckConnectionListenerCount(interval);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Default setConnectionProcessorThreadTimeout(
			final int timeout
		)
		{
			this.internalSetTimeoutConnectionProcessorThreadIdle(timeout);
			return this;
		}

		protected synchronized NetworkConnectionProcessor getConnectionProcessor()
		{
			if(this.connectionProcessor == null)
			{
				this.connectionProcessor = this.dispatch(this.provideConnectionProcessor());
			}
			return this.connectionProcessor;
		}

		protected synchronized NetworkConnectionProcessor provideConnectionProcessor()
		{
			throw new MissingFoundationPartException(NetworkConnectionProcessor.class);
		}

		@Override
		protected synchronized NetworkConnectionProcessor.Provider<?> provideConnectionProcessorProvider()
		{
			return new NetworkConnectionProcessor.Provider.Trivial(
				this.getConnectionProcessor()
			);
		}

		@Override
		public synchronized NetworkConnectionServer createServer()
		{
			final NetworkConnectionServer newServer = new NetworkConnectionServer.Default(
				this.createConnectionServerSetup()
			);
			return newServer;
		}

	}

}
