package net.jadoth.network.types;

import static net.jadoth.X.notNull;

import java.nio.channels.SocketChannel;

public interface NetworkUserSessionManager<U, S extends NetworkUserSession<U, ?>> extends NetworkSessionManager<S>
{
	public S registerUserConnection(U user, SocketChannel connection);

	public interface Creator<U, S extends NetworkUserSession<U, ?>>
	{
		public NetworkUserSessionManager<U, S> createSessionManager(
			NetworkMessageManager<S>                       messageManager,
			NetworkSessionManager.RegulatorSessionTimeout  regulatorSessionTimeout
		);
	}


	public class Implementation<U, S extends NetworkUserSession<U, ?>>
	extends NetworkSessionManager.AbstractImplementation<S>
	implements NetworkUserSessionManager<U, S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final NetworkUserSession.Creator<U, ?, S> sessionCreator;

		/* (07.11.2012 TM)FIXME: extend SessionManagement
		 * sessions must be per connection, not just per user.
		 * Because the same user credentials can be used for multiple clients
		 * and hence multiple concurrent connections.
		 * Even though there are use cases that allow only one connection per user (e.g. online game),
		 * the interfaces/architecture must allow both.
		 * (maybe it does already? check)
		 */
//		private final EqHashTable<U,S> sessionsPerUser = X.Map();
//		private final EqHashTable<U,S>.Values sessions = this.sessionsPerUser.values();



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final NetworkUserSession.Creator<U, ?, S>                 sessionCreator               ,
			final NetworkSessionManager.RegulatorSessionTimeout       regulatorSessionTimeout      ,
			final NetworkSessionManager.RegulatorSessionCheckInterval regulatorSessionCheckInterval,
			final NetworkMessageManager<S>                            messageManager               ,
			final NetworkSessionTimeoutHandler<S>                     sessionTimeoutHandler
		)
		{
			super(regulatorSessionTimeout, regulatorSessionCheckInterval, messageManager, sessionTimeoutHandler);
			this.sessionCreator = notNull(sessionCreator);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public S registerUserConnection(final U user, final SocketChannel connection)
		{
			S session;
			if((session = this.lookupSession(connection)) == null)
			{
				session = this.sessionCreator.createUserSession(this, user, connection);
				this.registerSession(session);
			}
			return session;
		}

	}

}
