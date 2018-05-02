package net.jadoth.network.types;

import net.jadoth.util.MissingAssemblyPartException;


public interface NetworkFactoryServerSessionless extends NetworkFactoryServer
{
	@Override
	public NetworkConnectionServer createServer();



	public class Implementation extends NetworkFactoryServer.AbstractImplementation
	implements NetworkFactoryServerSessionless
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private volatile NetworkConnectionProcessor connectionProcessor;



		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

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
		public NetworkFactoryServerSessionless.Implementation setConnectionProcessorProvider(
			final NetworkConnectionProcessor.Provider<?> connectionProcessorProvider
		)
		{
			this.internalSetConnectionProcessorProvider(connectionProcessorProvider);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Implementation setConnectionSocket(
			final NetworkConnectionSocket connectionSocket
		)
		{
			this.internalSetConnectionSocket(connectionSocket);
			return this;
		}

		public NetworkFactoryServerSessionless.Implementation setConnectionProcessor(
			final NetworkConnectionProcessor connectionProcessor
		)
		{
			this.internalSetConnectionProcessor(connectionProcessor);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Implementation setConnectionListenerMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionListeners(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Implementation setConnectionProcessorMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionProcessors(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Implementation setConnectionListenerCheckInterval(
			final int interval
		)
		{
			this.internalSetIntervalCheckConnectionListenerCount(interval);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionless.Implementation setConnectionProcessorThreadTimeout(
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
			throw new MissingAssemblyPartException(NetworkConnectionProcessor.class);
		}

		@Override
		protected synchronized NetworkConnectionProcessor.Provider<?> provideConnectionProcessorProvider()
		{
			return new NetworkConnectionProcessor.Provider.TrivialImplementation(
				this.getConnectionProcessor()
			);
		}

		@Override
		public synchronized NetworkConnectionServer createServer()
		{
			final NetworkConnectionServer newServer = new NetworkConnectionServer.Implementation(
				this.createConnectionServerSetup()
			);
			return newServer;
		}

	}

}
