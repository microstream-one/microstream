package one.microstream.network.types;

import one.microstream.exceptions.MissingFoundationPartException;




public interface NetworkFactoryUserSessionServer<U, S extends NetworkUserSession<U, ?>>
extends NetworkFactoryServerSessionful<S>
{
	public class Implementation<U, S extends NetworkUserSession<U, ?>>
	extends NetworkFactoryServerSessionful.AbstractImplementation<S>
	implements NetworkFactoryUserSessionServer<U, S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private NetworkUserSessionConnectionRegisterer.Provider<U, S> connectionRegistererProvider;
		private NetworkUserSessionProtocol<U, ?, S>                   sessionProtocol             ;



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setMessageListenerMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountMessageListeners(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setMessageListenerCheckInterval(
			final int checkInterval
		)
		{
			this.internalSetCheckIntervalMessageListeners(checkInterval);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setMessageProcessorMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountMessageProcessors(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setMessageProcessorThreadTimeout(
			final int timeout
		)
		{
			this.internalSetThreadTimeoutMessageProcessors(timeout);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setSessionTimeout(
			final int timeout
		)
		{
			this.internalSetSessionTimeout(timeout);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setSessionCheckInterval(
			final int checkInterval
		)
		{
			this.internalSetSessionCheckInterval(checkInterval);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setConnectionSocket(
			final NetworkConnectionSocket connectionSocket
		)
		{
			this.internalSetConnectionSocket(connectionSocket);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setConnectionListenerMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionListeners(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setConnectionProcessorMaxThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMaxThreadCountConnectionProcessors(maxThreadCount);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setConnectionListenerCheckInterval(
			final int interval
		)
		{
			this.internalSetIntervalCheckConnectionListenerCount(interval);
			return this;
		}

		@Override
		public NetworkFactoryUserSessionServer.Implementation<U, S> setConnectionProcessorThreadTimeout(
			final int timeout
		)
		{
			this.internalSetTimeoutConnectionProcessorThreadIdle(timeout);
			return this;
		}


		public NetworkFactoryUserSessionServer.Implementation<U, S> setSessionProtocol(
			final NetworkUserSessionProtocol<U, ?, S> userSessionCreator
		)
		{
			this.internalSetUserSessionProtocol(userSessionCreator);
			return this;
		}

		protected void internalSetUserSessionProtocol(final NetworkUserSessionProtocol<U, ?, S> sessionProtocol)
		{
			this.sessionProtocol = sessionProtocol;
		}

		protected NetworkUserSessionProtocol<U, ?, S> getUserSessionProtocol()
		{
			if(this.sessionProtocol == null)
			{
				this.sessionProtocol = this.dispatch(this.provideUserSessionProtocol());
			}
			return this.sessionProtocol;
		}

		protected NetworkUserSessionProtocol<U, ?, S> provideUserSessionProtocol()
		{
			throw new MissingFoundationPartException(NetworkUserSession.Creator.class);
		}

		@SuppressWarnings("unchecked") // safety of cast type paramters ensured by logic
		@Override
		protected NetworkUserSessionManager<U, S> getSessionManager()
		{
			// safety of cast is guaranteed by createSessionManager() implementation
			return (NetworkUserSessionManager<U, S>)super.getSessionManager();
		}

		@Override
		protected NetworkUserSessionManager<U, S> createSessionManager(
			final NetworkMessageManager<S>                            messageManager,
			final NetworkSessionManager.RegulatorSessionTimeout       regulatorSessionTimeout,
			final NetworkSessionManager.RegulatorSessionCheckInterval regulatorSessionCheckInterval
		)
		{
			return new NetworkUserSessionManager.Implementation<>(
				this.getUserSessionProtocol().provideUserSessionCreator(),
				regulatorSessionTimeout,
				regulatorSessionCheckInterval,
				messageManager,
				this.getSessionTimeoutHandler()
			);
		}

		@Override
		public synchronized NetworkConnectionProcessor.Provider<NetworkUserSessionConnectionRegisterer<U, S>>
		getConnectionProcessorProvider()
		{
			if(this.connectionRegistererProvider == null)
			{
				this.connectionRegistererProvider = this.dispatch(this.provideConnectionProcessorProvider());
			}
			return this.connectionRegistererProvider;
		}

		@Override
		protected synchronized NetworkUserSessionConnectionRegisterer.Provider<U, S>
		provideConnectionProcessorProvider()
		{
			return new NetworkUserSessionConnectionRegisterer.Provider.Implementation<>(
				this.getSessionManager(),
				this.getUserSessionProtocol(),
				this.getUserSessionProtocol()
			);
		}

	}

}
