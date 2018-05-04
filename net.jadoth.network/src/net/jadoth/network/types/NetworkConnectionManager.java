package net.jadoth.network.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.BulkList;
import net.jadoth.typing.JadothTypes;

public interface NetworkConnectionManager extends Suspendable
{
	@Override
	public boolean activate();

	public class Implementation implements NetworkConnectionManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final NetworkConnectionSocket                           connectionSocket            ;
		private final NetworkConnectionListener.Provider                connectionListenerProvider  ;
		private final NetworkConnectionListener.RegulatorThreadCount    regulatorConListThreadCount ;
		private final NetworkConnectionListener.RegulatorCheckInterval  regulatorConLischeckInterval;
		private final NetworkConnectionProcessor.RegulatorThreadCount   regulatorConPrcThreadCount  ;
		private final NetworkConnectionProcessor.RegulatorThreadTimeout regulatorConPrcThreadTimeout;
		private final NetworkConnectionHandler.Provider                 connectionHandlerProvider   ;
		private final NetworkConnectionProcessor.Provider<?>            connectionProcessorProvider ;

		private final BulkList<ListenerThread> listeners = new BulkList<>();

		private volatile boolean active;

		private ListenerController listenerController;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final NetworkConnectionSocket                           connectionSocket                        ,
			final NetworkConnectionListener.Provider                connectionListenerCreator               ,
			final NetworkConnectionListener.RegulatorThreadCount    threadCountProviderConnectionListeners  ,
			final NetworkConnectionListener.RegulatorCheckInterval  checkIntervalProviderConnectionListeners,
			final NetworkConnectionHandler.Provider                 connectionHandlerProvider               ,
			final NetworkConnectionProcessor.Provider<?>            connectionProcessorProvider             ,
			final NetworkConnectionProcessor.RegulatorThreadCount   threadControllerConnectionProcessors    ,
			final NetworkConnectionProcessor.RegulatorThreadTimeout timeoutControllerConnectionProcessors
		)
		{
			super();
			this.connectionSocket             = notNull(connectionSocket)                        ;
			this.connectionListenerProvider   = notNull(connectionListenerCreator)               ;
			this.regulatorConListThreadCount  = notNull(threadCountProviderConnectionListeners)  ;
			this.regulatorConLischeckInterval = notNull(checkIntervalProviderConnectionListeners);
			this.connectionHandlerProvider   = notNull(connectionHandlerProvider)              ;
			this.connectionProcessorProvider  = notNull(connectionProcessorProvider)             ;
			this.regulatorConPrcThreadCount   = notNull(threadControllerConnectionProcessors)    ;
			this.regulatorConPrcThreadTimeout = notNull(timeoutControllerConnectionProcessors)   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void startListener()
		{
			final ListenerThread thread = new ListenerThread(JadothTypes.to_int(this.listeners.size()) + 1);
			this.listeners.add(thread);
			thread.start();
		}

		private void stopListener()
		{
			// thread will end or be interrupted to end, whatever comes first, and then be disposed
			this.listeners.pick().deactivate();
		}

		final synchronized void disposeListener(final ListenerThread thread, final Throwable cause)
		{
			this.listeners.remove(thread); // remove thread first in case dispose throws an exception
			if(thread.logic != null)
			{
				// can be null if providing the logic already fails
				this.connectionListenerProvider.disposeConnectionListener(thread.logic, cause);
			}
		}

		private void increaseListeners(final int desiredListenerCount)
		{
			while(JadothTypes.to_int(this.listeners.size()) < desiredListenerCount)
			{
				this.startListener();
			}
		}

		private void decreaseListeners(final int desiredListenerCount)
		{
			while(JadothTypes.to_int(this.listeners.size()) > desiredListenerCount)
			{
				this.stopListener();
			}
		}

		final synchronized void checkListenerCount()
		{
			final int desiredListenerCount;
			if((desiredListenerCount = this.regulatorConListThreadCount.maxThreadCount()) == this.listeners.size())
			{
				return;
			}
			else if(desiredListenerCount > JadothTypes.to_int(this.listeners.size()))
			{
				this.increaseListeners(desiredListenerCount);
			}
			else
			{
				this.decreaseListeners(desiredListenerCount);
			}
		}

		final NetworkConnectionListener provideConnectionListener()
		{
			return this.connectionListenerProvider.provideConnectionListener(
				this.connectionSocket,
				this.connectionHandlerProvider.provideConnectionHandler(
					this.connectionProcessorProvider,
					this.regulatorConPrcThreadCount,
					this.regulatorConPrcThreadTimeout
				)
			);
		}

		private final class ListenerThread extends Thread implements Deactivateable
		{
			NetworkConnectionListener logic; // guaranteed to be set at the beginning of run

			ListenerThread(final int number)
			{
				super("Connection Listener # " + number + " of " + System.identityHashCode(Implementation.this));
			}

			@Override
			public void run()
			{
				// get actual logic just here to have the actual worker thread invoke the providing method
				Throwable disposalCause = null; // if logic ends normally, the cause remains null
				try
				{
					(this.logic = Implementation.this.provideConnectionListener()).run();
				}
				catch(final Throwable t)
				{
					disposalCause = t;
				}
				Implementation.this.disposeListener(this, disposalCause);
			}

			@Override
			public boolean isActive()
			{
				return this.logic.isActive();
			}

			@Override
			public boolean deactivate()
			{
				try
				{
					this.logic.deactivate();
					return true;
				}
				finally
				{
					this.interrupt();
				}
			}
		}

		private final class ListenerController extends Thread
		{
			private final NetworkConnectionListener.RegulatorCheckInterval sleepController; // sounds funny :D

			ListenerController(final NetworkConnectionListener.RegulatorCheckInterval sleepController)
			{
				super("Connection Listener Controller of " + System.identityHashCode(Implementation.this));
				this.sleepController = sleepController; // null already checked by parent instance
			}

			@Override
			public void run()
			{
				try
				{
					while(true)
					{
						// execute once immediately after thread creation, then sleep
						Implementation.this.checkListenerCount();
						Thread.sleep(this.sleepController.checkInterval());
					}
				}
				catch(final InterruptedException e)
				{
					// recevied signal to terminate controller thread
				}
			}

		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized boolean activate()
		{
			if(this.active)
			{
				return false;
			}
			(this.listenerController = new ListenerController(this.regulatorConLischeckInterval)).start();
			this.active = true;
			return true;
		}

		@Override
		public boolean isActive()
		{
			return this.active;
		}

		@Override
		public synchronized boolean deactivate()
		{
			if(!this.active)
			{
				return false;
			}
			this.listenerController.interrupt(); // call first in case of security exception
			this.listenerController = null;
			this.listeners.process(Deactivateable::deactivate);
			// (02.10.2012)XXX: wait for shutdown before return? join over all threads?
			this.active = false;
			return true;
		}

	}

}
