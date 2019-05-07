package one.microstream.network.types;

import static one.microstream.X.notNull;

import java.lang.ref.WeakReference;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

import one.microstream.collections.HashTable;
import one.microstream.concurrency.XThreads;
import one.microstream.meta.XDebug;
import one.microstream.typing.XTypes;

public interface NetworkSessionManager<S extends NetworkSession<?>> extends Suspendable
{
	public void removeSession(S session);



	public interface Creator<S extends NetworkSession<?>>
	{
		public NetworkSessionManager<S> createSessionManager(
			NetworkMessageManager<S>                       messageManager,
			NetworkSessionManager.RegulatorSessionTimeout  regulatorSessionTimeout
		);
	}



	public abstract class AbstractImplementation<S extends NetworkSession<?>> implements NetworkSessionManager<S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final RegulatorSessionTimeout         regulatorSessionTimeout;
		private final RegulatorSessionCheckInterval   regulatorSessionCheckInterval;
		private final NetworkMessageManager<S>        messageManager;
		private final NetworkSessionTimeoutHandler<S> sessionTimeoutHandler;

		private final HashTable<SocketChannel, S> sessionsPerConnection = HashTable.New();
		private final HashTable<SocketChannel, S>.Values sessions = this.sessionsPerConnection.values();

		private Thread sessionController;

		private volatile boolean active; // (29.10.2012)XXX: use multi-state pattern like in Server?

		private final Consumer<S> synchTimeoutChecker = this::synchCheckTimeout;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public AbstractImplementation(
			final RegulatorSessionTimeout         sessionTimeoutController     ,
			final RegulatorSessionCheckInterval   regulatorSessionCheckInterval,
			final NetworkMessageManager<S>        messageManager               ,
			final NetworkSessionTimeoutHandler<S> sessionTimeoutHandler
		)
		{
			super();
			this.regulatorSessionTimeout       = notNull(sessionTimeoutController     );
			this.regulatorSessionCheckInterval = notNull(regulatorSessionCheckInterval);
			this.messageManager                = notNull(messageManager               );
			this.sessionTimeoutHandler         = notNull(sessionTimeoutHandler        );
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected S lookupSession(final SocketChannel connection)
		{
			return this.sessionsPerConnection.get(connection);
		}

		protected void synchCheckTimeout(final S session)
		{
			XDebug.println("Checking session " + session + " with " + this.regulatorSessionTimeout.sessionTimeout());
			try
			{
				if(!session.isTimedOut(this.regulatorSessionTimeout.sessionTimeout()))
				{
					return;
				}
			}
			catch(final Exception e)
			{
				this.handleErronousSession(session, e);
				return;
			}
			this.sessionTimeoutHandler.handleTimeout(session, this);
			session.close();
		}


		protected void handleErronousSession(final S session, final Exception e)
		{
			// maybe this should be modularized with an exception handler. But removal must still be possible
			this.removeSession(session);
			session.close();
		}

		protected void iterateSessions(final Consumer<? super S> procedure)
		{
			XDebug.println("" + XTypes.to_int(this.sessions.size()));
			this.sessions.iterate(procedure);
		}

		protected synchronized void synchCheckSessionTimeouts()
		{
			this.iterateSessions(this.synchTimeoutChecker);
		}

		/**
		 * To be overriden by subclass to add unregistering semantics etc.
		 *
		 * @param session
		 */
		@Override
		public void removeSession(final S session)
		{
			XDebug.println("Removing session " + session);
			this.messageManager.remove(session);
			XDebug.println(Thread.currentThread() + " Session manager removing session " + session);
			this.sessionsPerConnection.removeFor(session.channel());
			XDebug.println(Thread.currentThread() + " Registered sessions now " + this.sessionsPerConnection.size());
		}

		/**
		 * To be overriden by subclass to add registering semantics etc.
		 *
		 * @param session
		 */
		protected void registerSession(final S session)
		{
			XDebug.println("Thread " + Thread.currentThread() + " registering session " + session);
			this.sessionsPerConnection.put(session.channel(), session);
			XDebug.println("Thread " + Thread.currentThread() + " now registered sessions: \n" + this.sessionsPerConnection);
			this.messageManager.register(session);
		}

		@Override
		public synchronized boolean deactivate()
		{
			if(!this.active)
			{
				return false;
			}
			this.active = false;
			this.messageManager.deactivate();
			// (30.09.2012)FIXME: deactivate
			this.sessionController.interrupt();
			this.sessionController = null;
			return true;
		}

		@Override
		public boolean isActive()
		{
			return this.active;
		}

		@Override
		public synchronized boolean activate()
		{
			if(this.active)
			{
				return false;
			}
			this.active = true;
			this.messageManager.activate();
			this.sessionController = XThreads.start(
				new SessionManagerTimeoutThread(this, this.regulatorSessionCheckInterval)
			);
			return true;
		}

	}

	public interface RegulatorSessionTimeout
	{
		public int sessionTimeout();
	}

	public interface RegulatorSessionCheckInterval
	{
		public int checkInterval();
	}

	final class SessionManagerTimeoutThread extends Thread
	{
		private final WeakReference<AbstractImplementation<?>> sessionManager;
		private final RegulatorSessionCheckInterval            sessionCheckInterval;

		SessionManagerTimeoutThread(
			final AbstractImplementation<?>        sessionManager,
			final RegulatorSessionCheckInterval sessionCheckInterval
		)
		{
			super("Session Controller of " + System.identityHashCode(sessionManager));
			this.sessionManager       = new WeakReference<>(sessionManager);
			this.sessionCheckInterval = sessionCheckInterval; // not null ensured by parent
		}

		@Override
		public void run()
		{
			for(AbstractImplementation<?> sm; (sm = this.sessionManager.get()) != null; sm = null)
			{
				try
				{
					Thread.sleep(this.sessionCheckInterval.checkInterval());
					XDebug.println("Checking sessions...");
					sm.synchCheckSessionTimeouts();
				}
				catch(final InterruptedException e)
				{
					break;
				}
				catch(final Exception t)
				{
					/* if the timeout management thread should throw a problem for any reason it's moreless
					 * irrelevant. The important thing is that the manager itself doesn't die. So just go on
					 */
				}
			}
		}

	}

}
