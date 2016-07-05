package net.jadoth.network.types;

import net.jadoth.meta.JadothConsole;

public interface NetworkSessionTimeoutHandler<S extends NetworkSession<?>>
{
	public void handleTimeout(S session, NetworkSessionManager<S> sessionManager);



	/**
	 * Trivial timeout handler implementation that immediately removes the timed out session from the session manager.
	 *
	 * @author Thomas Muenz
	 * @param <S>
	 */
	public class Trivial<S extends NetworkSession<?>> implements NetworkSessionTimeoutHandler<S>
	{
		@Override
		public void handleTimeout(final S session, final NetworkSessionManager<S> sessionManager)
		{
			JadothConsole.debugln("Timeouting " + session);
			sessionManager.removeSession(session);
		}

	}

	// further imaginable implementation: start a new thread that asks the client for a life sign and then decide.

}
