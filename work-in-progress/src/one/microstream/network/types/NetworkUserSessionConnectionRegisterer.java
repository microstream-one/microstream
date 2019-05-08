package one.microstream.network.types;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import one.microstream.exceptions.IORuntimeException;

public interface NetworkUserSessionConnectionRegisterer<U, S extends NetworkUserSession<U, ?>>
extends NetworkConnectionProcessor
{
	public interface Provider<U, S extends NetworkUserSession<U, ?>>
	extends NetworkConnectionProcessor.Provider<NetworkUserSessionConnectionRegisterer<U, S>>
	{
		public class Default<U, S extends NetworkUserSession<U, ?>> implements Provider<U, S>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private final NetworkUserSessionManager<U, S>         sessionManager             ;
			private final NetworkConnectionValidator.Provider<U>  connectionValidatorProvider;
			private final NetworkSessionClientGreeter.Provider<S> clientGreeterProvider      ;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			public Default(
				final NetworkUserSessionManager<U, S>         sessionManager             ,
				final NetworkConnectionValidator.Provider<U>  connectionValidatorProvider,
				final NetworkSessionClientGreeter.Provider<S> clientGreeterProvider
			)
			{
				super();
				this.sessionManager              = notNull(sessionManager);
				this.connectionValidatorProvider = notNull(connectionValidatorProvider);
				this.clientGreeterProvider       = notNull(clientGreeterProvider);
			}

			@Override
			public NetworkUserSessionConnectionRegisterer<U, S> provideLogic()
			{
				return new NetworkUserSessionConnectionRegisterer.Default<>(
					this.sessionManager,
					this.connectionValidatorProvider.provideValidator(),
					this.clientGreeterProvider.provideClientGreeter()
				);
			}

			@Override
			public void disposeLogic(
				final NetworkUserSessionConnectionRegisterer<U, S> processor,
				final Throwable                                    cause
			)
			{
				// no-op (so far)
			}

		}

	}


	public class Default<U, S extends NetworkUserSession<U, ?>>
	implements NetworkUserSessionConnectionRegisterer<U, S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final NetworkUserSessionManager<U, S> sessionManager         ;
		private final NetworkConnectionValidator<U>   connectionAuthenticator;
		private final NetworkSessionClientGreeter<S>  clientGreeter          ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final NetworkUserSessionManager<U, S> sessionManager,
			final NetworkConnectionValidator<U>   connectionAuthenticator,
			final NetworkSessionClientGreeter<S>  clientGreeter
		)
		{
			super();
			this.sessionManager          = notNull(sessionManager)         ;
			this.connectionAuthenticator = notNull(connectionAuthenticator);
			this.clientGreeter           = notNull(clientGreeter)          ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void accept(final SocketChannel connection)
		{
			// (29.10.2012)XXX: this try-catch has to be replaced by something more proper (move blocking setter)
			try
			{
				connection.configureBlocking(false);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}

			// if authentication throws an exception, the method just aborts, no harm done
			// (28.10.2012)XXX: but who exactely closes the connection in case of (any) error?
			/* (29.10.2012)XXX: should a connection processor thread really be
			 *  bound to a single connection until it authenticates or times out
			 *  for n connection processor threads, n lagging clients would slow down the whole connection
			 *  establishing process.
			 *  Or maybe timeout has to be very short (i.e. server expects authentication data to be already present
			 *  directly after establishing connection).
			 */
			final U user    = this.connectionAuthenticator.validateConnection(connection);

			// if registering throws an exception, the method just aborts, not harm done
			final S session = this.sessionManager.registerUserConnection(user, connection);

			// if greeting fails, the greeter has to take appropriate actions (e.g. close session) internally
			// (28.10.2012)XXX: really close internally? see closing concern above
			this.clientGreeter.greetClient(session);
		}

	}

}
