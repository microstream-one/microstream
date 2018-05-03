package net.jadoth.network.types;

import static net.jadoth.X.notNull;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.HashEnum;
import net.jadoth.exceptions.IORuntimeException;
import net.jadoth.meta.JadothDebug;
import net.jadoth.util.JadothTypes;


public interface NetworkMessageListener<S extends NetworkSession<?>> extends Runnable, Deactivateable
{
	/**
	 * Instructs the listener to register the passed {@code session} if the number of already registered sessions
	 * is lower than the passed {@code sizeThreshold}. If the listener instance accepts the session, 0 is returned.
	 * Otherwise, the current number of registered sessions is returned as a justification detail why the session
	 * has been rejected.
	 *
	 * @param session the session to be registered if it is there is enough space.
	 * @param sizeThreshold the threshold specifying up to which number of sessions the listener
	 *        must accept the session.
	 * @return 0 on success, the number of currently registered sessions otherwise.
	 */
	public int register(S session, int sizeThreshold);

	public void releaseSessions(Consumer<? super S> sessionCollector);

	public boolean removeSession(S session);

	@Override
	public void run();



	public interface Provider<S extends NetworkSession<?>>
	{
		public NetworkMessageListener<S> provideMessageListener(NetworkMessageHandler<S> messageAccecptor);

		public void disposeMessageListener(NetworkMessageListener<S> listener, Throwable cause);



		public class Implementaion<S extends NetworkSession<?>> implements NetworkMessageListener.Provider<S>
		{
			// note: problem handling done inside messageHandler, so no need for it here

			@Override
			public NetworkMessageListener<S> provideMessageListener(final NetworkMessageHandler<S> messageHandler)
			{
				return new NetworkMessageListener.Implementation<>(messageHandler);
			}

			@Override
			public void disposeMessageListener(final NetworkMessageListener<S> listener, final Throwable cause)
			{
				// no-op
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



	public final class Implementation<S extends NetworkSession<?>> implements NetworkMessageListener<S>, Predicate<S>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final int THREAD_SLEEP_TIME_MS = 10;



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final NetworkMessageHandler<S>     messageHandler;

		// enum mostly for the more efficient removal
		private final HashEnum<S> sessions;

		private volatile boolean active = true;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final NetworkMessageHandler<S> messageHandler)
		{
			super();
			this.messageHandler = notNull(messageHandler);
			this.sessions       = HashEnum.New();
		}

		@Override
		public void run()
		{
			while(this.active)
			{
				this.iterateSessions();
				try
				{
					// (03.11.2012)XXX: NetworkMessageListener: maybe make sleep time dynamic
					Thread.sleep(THREAD_SLEEP_TIME_MS);
				}
				catch(final InterruptedException e)
				{
					// if listener handles no sessions any more anyway, then deactivate
					if(this.sessions.isEmpty())
					{
						this.deactivate();
					}
					// otherwise? throw an illegalsomething exception? or just continue working?
					// (03.11.2012)XXX: NetworkMessageListener: handle improper interruption
				}
			}
		}

		private synchronized void iterateSessions()
		{
			/* Only remove obsolete sessions without calling potentially erroneous or long-running
			 * methods from parent instances such as session manager to unregister session.
			 * Let session manager handle proper closing etc. via timeout.
			 */
			this.sessions.removeBy(this);
		}

		/**
		 * Evaluates if the passed exception occured with the passed session means that the
		 * session has to be removed from this listener
		 *
		 * @param session
		 * @param e
		 * @return {@code true} if the session shall be removed, {@code false} otherwise.
		 */
		protected boolean evaluateSessionException(final S session, final Exception e)
		{
			JadothDebug.debugln("Error in session " + session + ": " + e);
			e.printStackTrace();
			// simply kick out erroneous session in basic implementation and swallow exception
			return true;
		}

		/**
		 * Evaluates if the passed exception occured with the passed session means that the
		 * session has to be removed from this listener
		 *
		 * @param session
		 * @param e
		 * @return {@code true} if the session shall be removed, {@code false} otherwise.
		 */
		protected boolean evaluateSessionIoException(final S session, final IOException e)
		{
			return this.evaluateSessionException(session, e);
		}

		@Override
		public boolean test(final S session)
		{
			try
			{
				if(session.isClosed())
				{
					return true;
				}
				if(!session.needsProcessing())
				{
					return false;
				}
			}
			catch(final IORuntimeException e)
			{
				return this.evaluateSessionIoException(session, e.getActual());
			}
			catch(final Exception e)
			{
				return this.evaluateSessionException(session, e);
			}
			this.messageHandler.handleMessage(session); // can/may never throw an exception
			return false;
		}

		@Override
		public synchronized int register(final S session, final int threshold)
		{
			// ignore closed sessions here to speed up registration. Closed sessions will be removed in next iteration.
			if(JadothTypes.to_int(this.sessions.size()) >= threshold)
			{
				return JadothTypes.to_int(this.sessions.size());
			}
			this.sessions.add(session);
			return 0;
		}

		@Override
		public synchronized boolean removeSession(final S session)
		{
			return this.sessions.removeOne(session);
		}

		@Override
		public boolean deactivate()
		{
			this.active = false;
			return true;
		}

		@Override
		public boolean isActive()
		{
			return this.active;
		}

		@Override
		public synchronized void releaseSessions(final Consumer<? super S> sessionCollector)
		{
			this.sessions.filterTo(sessionCollector, e -> !e.isClosed());
			this.sessions.clear();
		}

	}

}
