package one.microstream.network.types;

import static one.microstream.X.notNull;

import java.nio.channels.SocketChannel;


public interface NetworkUserSession<U, M> extends NetworkSession<M>
{
	public U user();

	public SocketChannel setNewConnection(SocketChannel connection);



	public interface Creator<U, M, S extends NetworkUserSession<U, M>>
	{
		public S createUserSession(NetworkUserSessionManager<U, S> sessionManager, U user, SocketChannel connection);
	}




	public abstract class AbstractImplementation<U, M, SELF extends NetworkUserSession<U, M>>
	extends NetworkSession.AbstractImplementation<M> implements NetworkUserSession<U, M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final NetworkUserSessionManager<U, SELF> sessionManager;
		private final U                           user          ;
		private       SocketChannel               connection    ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		protected AbstractImplementation(
			final NetworkUserSessionManager<U, SELF> sessionManager,
			final U user,
			final SocketChannel connection
		)
		{
			super();
			this.sessionManager = notNull(sessionManager);
			this.user           = notNull(user)      ;
			this.connection     = notNull(connection);
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		// CHECKSTYLE.OFF: MethodName: concise symbol for "self type"
		@SuppressWarnings("unchecked") // necessary because of missing "self type" in Java.
		protected final SELF $()
		{
			return (SELF)this;
		}
		// CHECKSTYLE.ON: MethodName

		@Override
		public final U user()
		{
			return this.user;
		}

		@Override
		public SocketChannel channel()
		{
			return this.connection;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public SocketChannel setNewConnection(final SocketChannel connection)
		{
			final SocketChannel oldConnection = this.connection;
			this.connection = connection;
			return oldConnection;
		}

		@Override
		public synchronized void close()
		{
			this.sessionManager.removeSession(this.$());
			super.close();
		}

	}

}

