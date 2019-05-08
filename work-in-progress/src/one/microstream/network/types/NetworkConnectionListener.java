package one.microstream.network.types;

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;

import java.nio.channels.SocketChannel;

import one.microstream.exceptions.WrapperRuntimeException;
import one.microstream.network.exceptions.NetworkExceptionConnectionAcception;

public interface NetworkConnectionListener extends Runnable, Deactivateable
{
	@Override
	public void run() throws NetworkExceptionConnectionAcception;



	public interface Provider
	{
		public NetworkConnectionListener provideConnectionListener(
			NetworkConnectionSocket  connectionSocket,
			NetworkConnectionHandler connectionHandler
		);

		public void disposeConnectionListener(NetworkConnectionListener listener, Throwable cause);



		public class Default implements NetworkConnectionListener.Provider
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private final NetworkConnectionProblemHandler.Provider problemHandlerProvider;


			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			public Default(final NetworkConnectionProblemHandler.Provider problemHandlerProvider)
			{
				super();
				this.problemHandlerProvider = problemHandlerProvider; // may explicitly be null
			}



			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public NetworkConnectionListener provideConnectionListener(
				final NetworkConnectionSocket  connectionSocket ,
				final NetworkConnectionHandler connectionHandler
			)
			{
				return new NetworkConnectionListener.Default(
					connectionSocket,
					connectionHandler,
					this.problemHandlerProvider == null
					? null
					: this.problemHandlerProvider.providerConnectionProblemHandler()
				);
			}

			@Override
			public void disposeConnectionListener(final NetworkConnectionListener listener, final Throwable cause)
			{
				if(this.problemHandlerProvider == null)
				{
					return;
				}

				// passed listener must be of the same type as the one instantiated in the provider method or fail.
				this.problemHandlerProvider.dispose(
					((NetworkConnectionListener.Default)listener).problemHandler(),
					cause
				);
			}

		}

	}

	public interface RegulatorThreadCount
	{
		public int maxThreadCount();
	}

	public interface RegulatorCheckInterval
	{
		public int checkInterval();
	}



	public class Default implements NetworkConnectionListener
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final NetworkConnectionProblemHandler DEFAULT_PH = new NetworkConnectionProblemHandler()
		{
			@Override
			public void handleConnectionProblem(final Throwable t, final SocketChannel connection)
			{
				throw new NetworkExceptionConnectionAcception(connection, t);
			}
		};



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final NetworkConnectionSocket         connectionSocket ;
		private final NetworkConnectionHandler        connectionHandler;
		private final NetworkConnectionProblemHandler problemHandler   ;

		private volatile boolean active = true;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final NetworkConnectionSocket  connectionSocket ,
			final NetworkConnectionHandler connectionHandler
		)
		{
			this(connectionSocket, connectionHandler, null);
		}

		public Default(
			final NetworkConnectionSocket         connectionSocket ,
			final NetworkConnectionHandler        connectionHandler,
			final NetworkConnectionProblemHandler problemHandler
		)
		{
			super();
			this.connectionSocket   =          connectionSocket           ;
			this.connectionHandler  =  notNull(connectionHandler)         ;
			this.problemHandler     = coalesce(problemHandler, DEFAULT_PH);
		}


		protected NetworkConnectionProblemHandler problemHandler()
		{
			return this.problemHandler;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void run() throws NetworkExceptionConnectionAcception
		{
			while(this.active)
			{
				final SocketChannel newConnection;
				try
				{
					newConnection = this.connectionSocket.acceptConnection(); // must be blocking, see constructor
				}
				catch(final Throwable t)
				{
					this.handleThrowable(t, null);
					continue; // if handler discards the exception, just continue
				}

				try
				{
					// whatever accepting means (serve or just register)
					this.connectionHandler.handleConnection(newConnection);
				}
				catch(final Throwable t)
				{
					this.handleThrowable(t, null);
					continue; // this exception handler discarding continue has no effect (for now)
				}
			}
		}

		private void handleThrowable(final Throwable t, final SocketChannel newConnection)
		{
			final Throwable actual = t instanceof WrapperRuntimeException
				? ((WrapperRuntimeException)t).getActual()
				: t
			;
			if(Thread.currentThread().isInterrupted())
			{
				this.deactivate(); // for consistency of state
			}
			this.problemHandler.handleConnectionProblem(actual, newConnection);
		}

		@Override
		public boolean isActive()
		{
			return this.active;
		}

		@Override
		public boolean deactivate()
		{
			this.active = false;
			return true;
		}

	}

}
