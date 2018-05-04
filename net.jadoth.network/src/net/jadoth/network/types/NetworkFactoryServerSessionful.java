package net.jadoth.network.types;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.network.types.NetworkMessageListener.Provider;
import net.jadoth.network.types.NetworkSessionServer.Implementation.RegulatorMessageListenerCheckInterval;
import net.jadoth.network.types.NetworkSessionServer.Implementation.RegulatorMessageListenerThreadCount;
import net.jadoth.network.types.NetworkSessionServer.Implementation.RegulatorMessageProcessorThreadCount;
import net.jadoth.network.types.NetworkSessionServer.Implementation.RegulatorMessageProcessorThreadTimeout;
import net.jadoth.network.types.NetworkSessionServer.Implementation.RegulatorSessionCheckInterval;
import net.jadoth.network.types.NetworkSessionServer.Implementation.RegulatorSessionTimeout;



public interface NetworkFactoryServerSessionful<S extends NetworkSession<?>> extends NetworkFactoryServer
{
	@Override
	public NetworkSessionServer createServer();

	public int getMessageListenerMaxThreadCount();
	public int getMessageListenerCheckInterval();
	public int getMessageProcessorMaxThreadCount();
	public int getMessageProcessorThreadTimeout();
	public int getSessionTimeout();
	public int getSessionCheckInterval();

	public NetworkFactoryServerSessionful<S> setMessageListenerMaxThreadCount(int maxThreadCount);
	public NetworkFactoryServerSessionful<S> setMessageListenerCheckInterval(int checkInterval);
	public NetworkFactoryServerSessionful<S> setMessageProcessorMaxThreadCount(int maxThreadCount);
	public NetworkFactoryServerSessionful<S> setMessageProcessorThreadTimeout(int timeout);
	public NetworkFactoryServerSessionful<S> setSessionTimeout(int timeout);
	public NetworkFactoryServerSessionful<S> setSessionCheckInterval(int interval);



	public abstract class AbstractImplementation<S extends NetworkSession<?>>
	extends NetworkFactoryServer.AbstractImplementation
	implements NetworkFactoryServerSessionful<S>
	{
		private int valueMsgLisThreadCount = RegulatorMessageListenerThreadCount   .DEFAULT_THREAD_COUNT;
		private int valueMsgLisCheckIntrvl = RegulatorMessageListenerCheckInterval .DEFAULT_INTERVAL    ;
		private int valueMsgPrcThreadCount = RegulatorMessageProcessorThreadCount  .DEFAULT_THREAD_COUNT;
		private int valueMsgPrcThrdTimeout = RegulatorMessageProcessorThreadTimeout.DEFAULT_TIMEOUT     ;
		private int valueSessionTimeout    = RegulatorSessionTimeout               .DEFAULT_TIMEOUT     ;
		private int valueSessionCheckIntvl = RegulatorSessionCheckInterval         .DEFAULT_INTERVAL    ;

		private NetworkMessageListener.Provider<S>     messageListenerProvider ;
		private NetworkMessageHandler.Provider<S>      messageHandlerProvider  ;
		private NetworkMessageProcessor.Provider<S, ?> messageProcessorProvider;
		private NetworkMessageProcessor<S>             messageProcessor        ;
		private NetworkSessionTimeoutHandler<S>        sessionTimeoutManager   ;
		private transient NetworkSessionManager<S>     sessionManager          ;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		protected synchronized void cleanUpConstruction()
		{
			this.sessionManager = null;
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		protected NetworkSessionManager<S> getSessionManager()
		{
			return this.sessionManager;
		}

		protected NetworkSessionTimeoutHandler<S> getSessionTimeoutHandler()
		{
			if(this.sessionTimeoutManager == null)
			{
				this.sessionTimeoutManager = this.dispatch(this.provideSessionTimeoutHandler());
			}
			return this.sessionTimeoutManager;
		}

		@Override
		public int getMessageListenerMaxThreadCount()
		{
			return this.valueMsgLisThreadCount;
		}

		@Override
		public int getMessageListenerCheckInterval()
		{
			return this.valueMsgLisCheckIntrvl;
		}

		@Override
		public int getMessageProcessorMaxThreadCount  ()
		{
			return this.valueMsgPrcThreadCount;
		}

		@Override
		public int getMessageProcessorThreadTimeout()
		{
			return this.valueMsgPrcThrdTimeout;
		}

		@Override
		public int getSessionTimeout()
		{
			return this.valueSessionTimeout;
		}

		@Override
		public int getSessionCheckInterval()
		{
			return this.valueSessionCheckIntvl;
		}

		protected synchronized NetworkMessageListener.Provider<S> provideMessageListenerProvider()
		{
			return new NetworkMessageListener.Provider.Implementaion<>();
		}

		protected synchronized NetworkSessionTimeoutHandler<S> provideSessionTimeoutHandler()
		{
			return new NetworkSessionTimeoutHandler.Trivial<>();
		}

		protected synchronized NetworkMessageHandler.Provider<S> provideMessageHandlerProvider()
		{
			return new NetworkMessageHandler.Provider.Implementation<>();
		}

		protected synchronized NetworkMessageProcessor.Provider<S, ? extends NetworkMessageProcessor<S>>
		provideMessageProcessorProvider()
		{
			return new NetworkMessageProcessor.Provider.TrivialImplementation<>(
				this.getMessageProcessor()
			);
		}

		protected synchronized NetworkMessageProcessor<S> provideMessageProcessor()
		{
			throw new MissingFoundationPartException(NetworkMessageProcessor.class);
		}

		protected synchronized NetworkMessageListener.Provider<S> getMessageListenerProvider()
		{
			if(this.messageListenerProvider == null)
			{
				this.messageListenerProvider = this.dispatch(this.provideMessageListenerProvider());
			}
			return this.messageListenerProvider;
		}

		protected synchronized NetworkMessageHandler.Provider<S> getMessageHandlerProvider()
		{
			if(this.messageHandlerProvider == null)
			{
				this.messageHandlerProvider = this.dispatch(this.provideMessageHandlerProvider());
			}
			return this.messageHandlerProvider;
		}

		protected synchronized NetworkMessageProcessor.Provider<S, ?> getMessageProcessorProvider()
		{
			if(this.messageProcessorProvider == null)
			{
				this.messageProcessorProvider = this.dispatch(this.provideMessageProcessorProvider());
			}
			return this.messageProcessorProvider;
		}

		protected abstract NetworkSessionManager<S> createSessionManager(
			NetworkMessageManager<S>                            messageManager,
			NetworkSessionManager.RegulatorSessionTimeout       regulatorSessionTimeout,
			NetworkSessionManager.RegulatorSessionCheckInterval regulatorSessionCheckInterval
		);



		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		protected final synchronized void internalSetMaxThreadCountMessageListeners(final int maxThreadCount)
		{
			this.valueMsgLisThreadCount = maxThreadCount;
		}

		protected final synchronized void internalSetCheckIntervalMessageListeners(final int checkInterval)
		{
			this.valueMsgLisCheckIntrvl = checkInterval;
		}

		protected final synchronized void internalSetMaxThreadCountMessageProcessors(final int maxThreadCount)
		{
			this.valueMsgPrcThreadCount = maxThreadCount;
		}

		protected final synchronized void internalSetThreadTimeoutMessageProcessors(final int timeout)
		{
			this.valueMsgPrcThrdTimeout = timeout;
		}

		protected final synchronized void internalSetSessionTimeout(final int timeout)
		{
			this.valueSessionTimeout = timeout;
		}

		protected final synchronized void internalSetSessionCheckInterval(final int checkInterval)
		{
			this.valueSessionCheckIntvl = checkInterval;
		}

		protected final synchronized void internalSetMessageListenerProvider(
			final NetworkMessageListener.Provider<S> messageListenerCreator
		)
		{
			this.messageListenerProvider = messageListenerCreator;
		}

		protected final synchronized void internalSetMessageHandlerProvider(
			final NetworkMessageHandler.Provider<S> messageHandlerProvider
		)
		{
			this.messageHandlerProvider = messageHandlerProvider;
		}

		protected final synchronized void internalSetMessageProcessorProvider(
			final NetworkMessageProcessor.Provider<S, ?> messageProcessorProvider
		)
		{
			this.messageProcessorProvider = messageProcessorProvider;
		}

		protected final synchronized void internalSetMessageProcessor(
			final NetworkMessageProcessor<S> messageProcessor)
		{
			this.messageProcessor = messageProcessor;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setConnectionListenerMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionListeners(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setConnectionProcessorMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionProcessors(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setConnectionListenerCheckInterval(
			final int interval
		)
		{
			this.internalSetIntervalCheckConnectionListenerCount(interval);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setConnectionProcessorThreadTimeout(
			final int timeout
		)
		{
			this.internalSetTimeoutConnectionProcessorThreadIdle(timeout);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setConnectionSocket(
			final NetworkConnectionSocket connectionSocket
		)
		{
			this.internalSetConnectionSocket(connectionSocket);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setConnectionListenerProvider(
			final NetworkConnectionListener.Provider connectionListenerProvider
		)
		{
			this.internalSetConnectionListenerProvider(connectionListenerProvider);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setConnectionHandlerProvider(
			final NetworkConnectionHandler.Provider connectionHandlerProvider
		)
		{
			this.internalSetConnectionHandlerProvider(connectionHandlerProvider);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setProblemHandlerProviderConnectionListening(
			final NetworkConnectionProblemHandler.Provider problemHandlerConnectionListening
		)
		{
			this.internalSetProblemHandlerProviderConnectionListening(problemHandlerConnectionListening);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setProblemHandlerProviderConnectionProcessing(
			final NetworkConnectionProblemHandler.Provider problemHandlerConnectionProcessing
		)
		{
			this.internalSetProblemHandlerProviderConnectionProcessing(problemHandlerConnectionProcessing);
			return this;
		}

		public NetworkFactoryServerSessionful<S> setMessageListenerProvider(
			final Provider<S> messageListenerProvider
		)
		{
			this.internalSetMessageListenerProvider(messageListenerProvider);
			return this;
		}

		public NetworkFactoryServerSessionful<S> setMessageHandlerProvider(
			final NetworkMessageHandler.Provider<S> messageHandlerProvider
		)
		{
			this.internalSetMessageHandlerProvider(messageHandlerProvider);
			return this;
		}

		public NetworkFactoryServerSessionful<S> setMessageProcessorProvider(
			final NetworkMessageProcessor.Provider<S, ?> messageProcessorProvider
		)
		{
			this.internalSetMessageProcessorProvider(messageProcessorProvider);
			return this;
		}

		public NetworkMessageProcessor<S> getMessageProcessor()
		{
			if(this.messageProcessor == null)
			{
				this.messageProcessor = this.dispatch(this.provideMessageProcessor());
			}
			return this.messageProcessor;
		}

		public NetworkFactoryServerSessionful<S> setMessageProcessor(
			final NetworkMessageProcessor<S> messageProcessor
		)
		{
			this.internalSetMessageProcessor(messageProcessor);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful<S> setMessageListenerMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountMessageListeners(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful<S> setMessageListenerCheckInterval(
			final int checkInterval
		)
		{
			this.internalSetCheckIntervalMessageListeners(checkInterval);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setMessageProcessorMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountMessageProcessors(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setMessageProcessorThreadTimeout(
			final int timeout
		)
		{
			this.internalSetThreadTimeoutMessageProcessors(timeout);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setSessionTimeout(
			final int timeout
		)
		{
			this.internalSetSessionTimeout(timeout);
			return this;
		}

		@Override
		public NetworkFactoryServerSessionful.AbstractImplementation<S> setSessionCheckInterval(
			final int checkInterval
		)
		{
			this.internalSetSessionCheckInterval(checkInterval);
			return this;
		}

		protected synchronized NetworkMessageManager<S> createMessageManager(
			final RegulatorMessageListenerThreadCount    regulatorMsgLisThreadCount,
			final RegulatorMessageListenerCheckInterval  regulatorMsgLisCheckIntrvl,
			final RegulatorMessageProcessorThreadCount   regulatorMsgPrcThreadCount,
			final RegulatorMessageProcessorThreadTimeout regulatorMsgPrcThrdTimeout
		)
		{
			return new NetworkMessageManager.Implementation<>(
				this.getMessageListenerProvider() ,
				regulatorMsgLisThreadCount        ,
				regulatorMsgLisCheckIntrvl        ,
				this.getMessageHandlerProvider()  ,
				this.getMessageProcessorProvider(),
				regulatorMsgPrcThreadCount        ,
				regulatorMsgPrcThrdTimeout
			);
		}


		protected synchronized NetworkSessionServer.Implementation.Setup createSessionServerSetup()
		{
			final RegulatorMessageListenerThreadCount    regulatorMsgLisThreadCount;
			final RegulatorMessageListenerCheckInterval  regulatorMsgLisCheckIntrvl;
			final RegulatorMessageProcessorThreadCount   regulatorMsgPrcThreadCount;
			final RegulatorMessageProcessorThreadTimeout regulatorMsgPrcThrdTimeout;
			final RegulatorSessionCheckInterval          regulatorSessionChkIntervl;
			final RegulatorSessionTimeout                regulatorSessionTimeout   ;
			(regulatorMsgLisThreadCount = new RegulatorMessageListenerThreadCount())
				.setMaxThreadCount(this.valueMsgLisThreadCount)
			;
			(regulatorMsgLisCheckIntrvl = new RegulatorMessageListenerCheckInterval())
				.setCheckInterval(this.valueMsgLisCheckIntrvl)
			;
			(regulatorMsgPrcThreadCount = new RegulatorMessageProcessorThreadCount())
				.setMaxThreadCount(this.valueMsgPrcThreadCount)
			;
			(regulatorMsgPrcThrdTimeout = new RegulatorMessageProcessorThreadTimeout())
				.setTimeout(this.valueMsgPrcThrdTimeout)
			;
			(regulatorSessionChkIntervl = new RegulatorSessionCheckInterval())
				.setCheckInterval(this.valueSessionCheckIntvl)
			;
			(regulatorSessionTimeout    = new RegulatorSessionTimeout())
				.setTimeout(this.valueSessionTimeout)
			;

			final NetworkMessageManager<S> messageManager = this.dispatch(this.createMessageManager(
				regulatorMsgLisThreadCount,
				regulatorMsgLisCheckIntrvl,
				regulatorMsgPrcThreadCount,
				regulatorMsgPrcThrdTimeout
			));

			this.sessionManager = this.dispatch(this.createSessionManager(
				messageManager,
				regulatorSessionTimeout,
				regulatorSessionChkIntervl
			));

			final NetworkConnectionServer.Implementation.Setup superSetup = this.dispatch(
				this.createConnectionServerSetup()
			);

			return new NetworkSessionServer.Implementation.Setup(
				superSetup                ,
				this.sessionManager       ,
				regulatorMsgLisThreadCount,
				regulatorMsgLisCheckIntrvl,
				regulatorMsgPrcThreadCount,
				regulatorMsgPrcThrdTimeout,
				regulatorSessionChkIntervl,
				regulatorSessionTimeout
			);
		}

		@Override
		public synchronized NetworkSessionServer createServer()
		{
			this.cleanUpConstruction(); // reset volatile/temporary construction state just in case.
			final NetworkSessionServer.Implementation.Setup setup = this.createSessionServerSetup();
			final NetworkSessionServer newServer = new NetworkSessionServer.Implementation(setup);
			this.cleanUpConstruction(); // clean up
			return newServer;
		}

	}

}
