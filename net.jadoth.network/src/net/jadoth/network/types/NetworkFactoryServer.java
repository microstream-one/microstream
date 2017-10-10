package net.jadoth.network.types;

import net.jadoth.functional.InstanceDispatcherLogic;
import net.jadoth.network.types.NetworkConnectionServer.Implementation.RegulatorConnectionListenerCheckInterval;
import net.jadoth.network.types.NetworkConnectionServer.Implementation.RegulatorConnectionListenerThreadCount;
import net.jadoth.network.types.NetworkConnectionServer.Implementation.RegulatorConnectionProcessorThreadTimeout;
import net.jadoth.network.types.NetworkConnectionServer.Implementation.RegulatorConnectionProcessorsThreadCount;
import net.jadoth.util.MissingAssemblyPartException;

public interface NetworkFactoryServer
{
	public NetworkConnectionServer createServer();

	public int getConnectionListenerMaxThreadCount ();
	public int getCountConnectionProcessorMaxThread();
	public int getConnectionListenerCheckInterval  ();
	public int getConnectionProcessorThreadTimeout ();

	public NetworkFactoryServer setConnectionListenerMaxThreadCount (int maxThreadCount);
	public NetworkFactoryServer setConnectionListenerCheckInterval  (int interval);
	public NetworkFactoryServer setConnectionProcessorMaxThreadCount(int maxThreadCount);
	public NetworkFactoryServer setConnectionProcessorThreadTimeout (int timeout);



	public abstract class AbstractImplementation implements NetworkFactoryServer
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final InstanceDispatcherLogic NO_OP = new InstanceDispatcherLogic()
		{
			@Override
			public <T> T apply(final T subject)
			{
				return subject;
			}
		};



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final InstanceDispatcherLogic instanceDispatcher = NO_OP;

		private int valThreadContrlConListns = RegulatorConnectionListenerThreadCount   .DEFAULT_THREAD_COUNT;
		private int valCheckIntrvalConListns = RegulatorConnectionListenerCheckInterval .DEFAULT_INTERVAL    ;
		private int valThreadContrlConProcss = RegulatorConnectionProcessorsThreadCount .DEFAULT_THREAD_COUNT;
		private int valTimeoutCotrlConProcss = RegulatorConnectionProcessorThreadTimeout.DEFAULT_TIMEOUT     ;

		private NetworkConnectionSocket                serverSocketChannel        ;
		private NetworkConnectionListener.Provider     connectionListenerProvider ;
		private NetworkConnectionHandler.Provider      connectionHandlerProvider  ;
		private NetworkConnectionProcessor.Provider<?> connectionProcessorProvider;

		private NetworkConnectionProblemHandler.Provider problemHandlerConnectionListening ;
		private NetworkConnectionProblemHandler.Provider problemHandlerConnectionProcessing;



		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		protected final synchronized void internalSetMaxThreadCountConnectionListeners(final int maxThreadCount)
		{
			this.valThreadContrlConListns = maxThreadCount;
		}

		protected final synchronized void internalSetMaxThreadCountConnectionProcessors(final int maxThreadCount)
		{
			this.valThreadContrlConProcss = maxThreadCount;
		}

		protected final synchronized void internalSetIntervalCheckConnectionListenerCount(final int interval)
		{
			this.valCheckIntrvalConListns = interval;
		}

		protected final synchronized void internalSetTimeoutConnectionProcessorThreadIdle(final int timeout)
		{
			this.valTimeoutCotrlConProcss = timeout;
		}

		protected final synchronized void internalSetConnectionSocket(final NetworkConnectionSocket serverSocketChannel)
		{
			this.serverSocketChannel = serverSocketChannel;
		}

		protected final synchronized void internalSetConnectionListenerProvider(
			final NetworkConnectionListener.Provider connectionListenerProvider
		)
		{
			this.connectionListenerProvider = connectionListenerProvider;
		}

		protected final synchronized void internalSetConnectionHandlerProvider(
			final NetworkConnectionHandler.Provider connectionHandlerProvider
		)
		{
			this.connectionHandlerProvider = connectionHandlerProvider;
		}

		protected final synchronized void internalSetConnectionProcessorProvider(
			final NetworkConnectionProcessor.Provider<?> connectionProcessorProvider
		)
		{
			this.connectionProcessorProvider = connectionProcessorProvider;
		}

		protected final synchronized void internalSetProblemHandlerProviderConnectionListening(
			final NetworkConnectionProblemHandler.Provider problemHandlerConnectionListening
		)
		{
			this.problemHandlerConnectionListening = problemHandlerConnectionListening;
		}

		protected final synchronized void internalSetProblemHandlerProviderConnectionProcessing(
			final NetworkConnectionProblemHandler.Provider problemHandlerConnectionProcessing
		)
		{
			this.problemHandlerConnectionProcessing = problemHandlerConnectionProcessing;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected synchronized <T> T dispatch(final T newInstance)
		{
			return this.instanceDispatcher.apply(newInstance);
		}

		protected synchronized void cleanUpConstruction()
		{
			// no-op in basic implementation
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public int getConnectionListenerMaxThreadCount()
		{
			return this.valThreadContrlConListns;
		}

		@Override
		public int getCountConnectionProcessorMaxThread()
		{
			return this.valThreadContrlConProcss;
		}

		@Override
		public int getConnectionListenerCheckInterval()
		{
			return this.valCheckIntrvalConListns;
		}

		@Override
		public int getConnectionProcessorThreadTimeout()
		{
			return this.valTimeoutCotrlConProcss;
		}

		@Override
		public NetworkFactoryServer.AbstractImplementation setConnectionListenerMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionListeners(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServer.AbstractImplementation setConnectionProcessorMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionProcessors(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServer.AbstractImplementation setConnectionListenerCheckInterval(
			final int interval
		)
		{
			this.internalSetIntervalCheckConnectionListenerCount(interval);
			return this;
		}

		@Override
		public NetworkFactoryServer.AbstractImplementation setConnectionProcessorThreadTimeout(
			final int timeout
		)
		{
			this.internalSetTimeoutConnectionProcessorThreadIdle(timeout);
			return this;
		}

		public NetworkFactoryServer.AbstractImplementation setConnectionSocket(
			final NetworkConnectionSocket connectionSocket
		)
		{
			this.internalSetConnectionSocket(connectionSocket);
			return this;
		}

		public NetworkFactoryServer.AbstractImplementation setConnectionListenerProvider(
			final NetworkConnectionListener.Provider connectionListenerProvider
		)
		{
			this.internalSetConnectionListenerProvider(connectionListenerProvider);
			return this;
		}

		public NetworkFactoryServer.AbstractImplementation setConnectionHandlerProvider(
			final NetworkConnectionHandler.Provider connectionHandlerProvider
		)
		{
			this.internalSetConnectionHandlerProvider(connectionHandlerProvider);
			return this;
		}

		public NetworkFactoryServer.AbstractImplementation setConnectionProcessorProvider(
			final NetworkConnectionProcessor.Provider<?> connectionProcessorProvider
		)
		{
			this.internalSetConnectionProcessorProvider(connectionProcessorProvider);
			return this;
		}

		public NetworkFactoryServer.AbstractImplementation setProblemHandlerProviderConnectionListening(
			final NetworkConnectionProblemHandler.Provider problemHandlerConnectionListening
		)
		{
			this.internalSetProblemHandlerProviderConnectionListening(problemHandlerConnectionListening);
			return this;
		}

		public NetworkFactoryServer.AbstractImplementation setProblemHandlerProviderConnectionProcessing(
			final NetworkConnectionProblemHandler.Provider problemHandlerConnectionProcessing
		)
		{
			this.internalSetProblemHandlerProviderConnectionProcessing(problemHandlerConnectionProcessing);
			return this;
		}

		protected synchronized NetworkConnectionServer.Implementation.Setup createConnectionServerSetup()
		{
			final RegulatorConnectionListenerThreadCount    regulatorConLisThreadCount;
			final RegulatorConnectionListenerCheckInterval  regulatorConLisCheckIntrvl;
			final RegulatorConnectionProcessorsThreadCount  regulatorConPrcThreadCount;
			final RegulatorConnectionProcessorThreadTimeout regulatorConPrcThrdTimeout;
			(regulatorConLisThreadCount = new RegulatorConnectionListenerThreadCount())
				.setMaxThreadCount(this.valThreadContrlConListns)
			;
			(regulatorConLisCheckIntrvl = new RegulatorConnectionListenerCheckInterval())
				.setCheckInterval(this.valCheckIntrvalConListns)
			;
			(regulatorConPrcThreadCount = new RegulatorConnectionProcessorsThreadCount())
				.setMaxThreadCount(this.valThreadContrlConProcss)
			;
			(regulatorConPrcThrdTimeout = new RegulatorConnectionProcessorThreadTimeout())
				.setTimeout(this.valTimeoutCotrlConProcss)
			;

			final NetworkConnectionManager connectionManager = this.dispatch(this.createConnectionManager(
				regulatorConLisThreadCount,
				regulatorConLisCheckIntrvl,
				regulatorConPrcThreadCount,
				regulatorConPrcThrdTimeout
			));

			return new NetworkConnectionServer.Implementation.Setup(
				connectionManager,
				regulatorConLisThreadCount,
				regulatorConLisCheckIntrvl,
				regulatorConPrcThreadCount,
				regulatorConPrcThrdTimeout
			);
		}

		protected synchronized NetworkConnectionManager createConnectionManager(
			final RegulatorConnectionListenerThreadCount    threadContrlConListeners ,
			final RegulatorConnectionListenerCheckInterval  checkIntrvalConListeners ,
			final RegulatorConnectionProcessorsThreadCount  threadContrlConProcessors,
			final RegulatorConnectionProcessorThreadTimeout timeoutCotrlConProcessors
		)
		{
			final NetworkConnectionManager newConnectionManager =
				new NetworkConnectionManager.Implementation(
					this.getConnectionSocket()        ,
					this.getConnectionListenerProvider() ,
					threadContrlConListeners             ,
					checkIntrvalConListeners             ,
					this.getConnectionHandlerProvider() ,
					this.getConnectionProcessorProvider(),
					threadContrlConProcessors            ,
					timeoutCotrlConProcessors
				)
			;
			return newConnectionManager;
		}

		protected synchronized NetworkConnectionListener.Provider provideConnectionListenerProvider()
		{
			return new NetworkConnectionListener.Provider.Implementation(
				this.getProblemHandlerProviderConnectionListening()
			);
		}

		protected synchronized NetworkConnectionHandler.Provider provideConnectionHandlerProvider()
		{
			return new NetworkConnectionHandler.Provider.Implementation();
		}

		protected synchronized NetworkConnectionProcessor.Provider<?> provideConnectionProcessorProvider()
		{
			throw new MissingAssemblyPartException(NetworkConnectionProcessor.Provider.class);
		}

		protected synchronized NetworkConnectionProblemHandler.Provider provideProblemHandlerConnectionListening()
		{
			return null; // null, use default
		}

		protected synchronized NetworkConnectionProblemHandler.Provider provideProblemHandlerConnectionProcessing()
		{
			return null; // null, use default
		}

		protected synchronized NetworkConnectionSocket getConnectionSocket()
		{
			if(this.serverSocketChannel == null)
			{
				throw new MissingAssemblyPartException(NetworkConnectionSocket.class);
			}
			return this.serverSocketChannel;
		}

		protected synchronized NetworkConnectionListener.Provider getConnectionListenerProvider()
		{
			if(this.connectionListenerProvider == null)
			{
				this.connectionListenerProvider = this.dispatch(this.provideConnectionListenerProvider());
			}
			return this.connectionListenerProvider;
		}

		protected synchronized NetworkConnectionHandler.Provider getConnectionHandlerProvider()
		{
			if(this.connectionHandlerProvider == null)
			{
				this.connectionHandlerProvider = this.dispatch(this.provideConnectionHandlerProvider());
			}
			return this.connectionHandlerProvider;
		}

		protected synchronized NetworkConnectionProcessor.Provider<?> getConnectionProcessorProvider()
		{
			if(this.connectionProcessorProvider == null)
			{
				this.connectionProcessorProvider = this.dispatch(this.provideConnectionProcessorProvider());
			}
			return this.connectionProcessorProvider;
		}

		protected synchronized NetworkConnectionProblemHandler.Provider getProblemHandlerProviderConnectionListening()
		{
			if(this.problemHandlerConnectionListening == null)
			{
				this.problemHandlerConnectionListening = this.dispatch(this.provideProblemHandlerConnectionListening());
			}
			return this.problemHandlerConnectionListening;
		}

		protected synchronized NetworkConnectionProblemHandler.Provider getProblemHandlerProviderConnectionProcessing()
		{
			if(this.problemHandlerConnectionProcessing == null)
			{
				this.problemHandlerConnectionProcessing = this.dispatch(this.provideProblemHandlerConnectionProcessing());
			}
			return this.problemHandlerConnectionProcessing;
		}

	}

}
