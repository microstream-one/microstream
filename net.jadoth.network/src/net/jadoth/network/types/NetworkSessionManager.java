package net.jadoth.network.types;

import static net.jadoth.X.notNull;

import java.lang.ref.WeakReference;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

import net.jadoth.collections.HashTable;
import net.jadoth.concurrent.JadothThreads;
import net.jadoth.meta.JadothDebug;
import net.jadoth.util.JadothTypes;

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
		// instance fields  //
		/////////////////////

		private final RegulatorSessionTimeout         regulatorSessionTimeout;
		private final RegulatorSessionCheckInterval   regulatorSessionCheckInterval;
		private final NetworkMessageManager<S>        messageManager;
		private final NetworkSessionTimeoutHandler<S> sessionTimeoutHandler;

		private final HashTable<SocketChannel, S> sessionsPerConnection = HashTable.New();
		private final HashTable<SocketChannel, S>.Values sessions = this.sessionsPerConnection.values();

		private Thread sessionController;

		private volatile boolean active; // (29.10.2012)XXX: use multi-state pattern like in Server?

		private final Consumer<S> synchTimeoutChecker = new Consumer<S>()
		{
			@Override
			public void accept(final S session)
			{
				AbstractImplementation.this.synchCheckTimeout(session);
			}
		};



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

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
			JadothDebug.debugln("Checking session " + session + " with " + this.regulatorSessionTimeout.sessionTimeout());
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
			JadothDebug.debugln("" + JadothTypes.to_int(this.sessions.size()));
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
			JadothDebug.debugln("Removing session " + session);
			this.messageManager.remove(session);
			JadothDebug.debugln(Thread.currentThread() + " Session manager removing session " + session);
			this.sessionsPerConnection.removeFor(session.channel());
			JadothDebug.debugln(Thread.currentThread() + " Registered sessions now " + this.sessionsPerConnection.size());
		}

		/**
		 * To be overriden by subclass to add registering semantics etc.
		 *
		 * @param session
		 */
		protected void registerSession(final S session)
		{
			JadothDebug.debugln("Thread " + Thread.currentThread() + " registering session " + session);
			this.sessionsPerConnection.put(session.channel(), session);
			JadothDebug.debugln("Thread " + Thread.currentThread() + " now registered sessions: \n" + this.sessionsPerConnection);
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
			this.sessionController = JadothThreads.start(
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
					JadothDebug.debugln("Checking sessions...");
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
