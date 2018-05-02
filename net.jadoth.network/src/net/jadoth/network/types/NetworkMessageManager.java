package net.jadoth.network.types;

import static net.jadoth.Jadoth.notNull;
import net.jadoth.Jadoth;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.MiniMap;


public interface NetworkMessageManager<S extends NetworkSession<?>> extends Suspendable
{
	public void register(S session);

	public void remove(S session);



	public class Implementation<S extends NetworkSession<?>> implements NetworkMessageManager<S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final NetworkMessageListener.Provider<S>             messageListenerProvider                ;
		private final NetworkMessageListener.RegulatorThreadCount    threadCountProviderMessageListeners    ;
		private final NetworkMessageListener.RegulatorCheckInterval  checkIntervalProviderMessageListeners  ;
		private final NetworkMessageHandler.Provider<S>             messageHandlerProvider                ;
		private final NetworkMessageProcessor.Provider<S, ?>         messageProcessorProvider               ;
		private final NetworkMessageProcessor.RegulatorThreadCount   threadCountProviderMessageProcessors   ;
		private final NetworkMessageProcessor.RegulatorThreadTimeout threadTimeoutControllerMessageProcessor;

		private final BulkList<ListenerThread>   listeners  = new BulkList<>();
		private final MiniMap<S, ListenerThread> sessionMap = new MiniMap<>();

		private volatile boolean active;

		private ListenerController listenerController;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final NetworkMessageListener.Provider<S>             messageListenerCreator                 ,
			final NetworkMessageListener.RegulatorThreadCount    threadCountProviderMessageListeners    ,
			final NetworkMessageListener.RegulatorCheckInterval  checkIntervalProviderMessageListeners  ,
			final NetworkMessageHandler.Provider<S>             messageHandlerProvider                ,
			final NetworkMessageProcessor.Provider<S, ?>         messageProcessorProvider               ,
			final NetworkMessageProcessor.RegulatorThreadCount   threadCountProviderMessageProcessors   ,
			final NetworkMessageProcessor.RegulatorThreadTimeout threadTimeoutControllerMessageProcessor
		)
		{
			super();
			this.messageListenerProvider                 = notNull(messageListenerCreator)                 ;
			this.threadCountProviderMessageListeners     = notNull(threadCountProviderMessageListeners)    ;
			this.checkIntervalProviderMessageListeners   = notNull(checkIntervalProviderMessageListeners)  ;
			this.messageHandlerProvider                 = notNull(messageHandlerProvider)                ;
			this.messageProcessorProvider                = notNull(messageProcessorProvider)               ;
			this.threadCountProviderMessageProcessors    = notNull(threadCountProviderMessageProcessors)   ;
			this.threadTimeoutControllerMessageProcessor = notNull(threadTimeoutControllerMessageProcessor);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void startListener()
		{
			final ListenerThread thread = new ListenerThread(Jadoth.to_int(this.listeners.size()) + 1);
			this.listeners.add(thread);
			thread.start();
		}

		private void stopListener(final BulkList<S> sessionsToBeTransfered)
		{
			// thread will end or be interrupted to end, whatever comes first, and then be disposed
			this.listeners.pick().transferSessions(sessionsToBeTransfered).deactivate();
		}

		final synchronized void disposeListener(final ListenerThread thread, final Throwable cause)
		{
			this.listeners.remove(thread); // remove thread first in case dispose throws an exception
			if(thread.logic != null)
			{
				// can be null if providing the logic already fails
				this.messageListenerProvider.disposeMessageListener(thread.logic, cause);
			}
		}

		private void synchIncreaseListeners(final int desiredListenerCount)
		{
			while(Jadoth.to_int(this.listeners.size()) < desiredListenerCount)
			{
				this.startListener();
			}
		}

		private void synchDecreaseListeners(final int desiredListenerCount)
		{
			final BulkList<S> sessionsToTransfer = new BulkList<>();
			while(Jadoth.to_int(this.listeners.size()) > desiredListenerCount)
			{
				this.stopListener(sessionsToTransfer);
			}
			synchTransferSessions(this.listeners, this.sessionMap, sessionsToTransfer);
		}

		private static <S extends NetworkSession<?>> void synchTransferSessions(
			final BulkList<Implementation<S>.ListenerThread>   listeners ,
			final MiniMap<S, Implementation<S>.ListenerThread> sessionMap,
			final BulkList<S> sessionsToTransfer
		)
		{
			final int size = Jadoth.to_int(sessionsToTransfer.size());
			for(int i = 0; i < size; i++)
			{
				final S session;
				sessionMap.put(
					session = sessionsToTransfer.at(i),
					synchRegisterSession(listeners, session)
				);
			}
		}

		final synchronized void checkListenerCount()
		{
			final int desiredLisCount;
			if((desiredLisCount = this.threadCountProviderMessageListeners.maxThreadCount()) == this.listeners.size())
			{
				return;
			}
			else if(desiredLisCount > Jadoth.to_int(this.listeners.size()))
			{
				this.synchIncreaseListeners(desiredLisCount);
			}
			else
			{
				this.synchDecreaseListeners(desiredLisCount);
			}
		}

		final NetworkMessageListener<S> provideMessageListener()
		{
			return this.messageListenerProvider.provideMessageListener(
				this.messageHandlerProvider.provideMessageHandler(
					this.messageProcessorProvider,
					this.threadCountProviderMessageProcessors,
					this.threadTimeoutControllerMessageProcessor
				)
			);
		}

		private final class ListenerThread extends Thread implements Deactivateable
		{
			/* intentionally no weak reference here because parent instance must be guaranteed
			 * to exist to dispose the listener when it's done.
			 */

			NetworkMessageListener<S> logic; // guaranteed to be set at the beginning of run

			ListenerThread(final int number)
			{
				super("Message Listener # " + number + " of " + System.identityHashCode(Implementation.this));
			}

			@Override
			public void run()
			{
				// get actual logic just here to have the actual worker thread invoke the providing method
				Throwable disposalCause = null; // if logic ends normally, the cause remains null
				try
				{
					(this.logic = Implementation.this.provideMessageListener()).run();
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

			final ListenerThread transferSessions(final BulkList<S> vessel)
			{
				this.logic.releaseSessions(vessel);
				return this;
			}

		}

		private final class ListenerController extends Thread
		{
			private final NetworkMessageListener.RegulatorCheckInterval sleepController; // sounds funny :D

			ListenerController(final NetworkMessageListener.RegulatorCheckInterval sleepController)
			{
				super("Message Listener Controller of " + System.identityHashCode(Implementation.this));
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
			(this.listenerController = new ListenerController(this.checkIntervalProviderMessageListeners)).start();
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

		private static <S extends NetworkSession<?>> Implementation<S>.ListenerThread synchRegisterSession(
			final BulkList<Implementation<S>.ListenerThread> listeners,
			final S session
		)
		{
			// external iteration is okay for implementation detail array-backed bulklist with feedback loop
			final int size = Jadoth.to_int(listeners.size());
			for(int i = 0, limit = 1; i < size; i++)
			{
				if((limit = listeners.at(i).logic.register(session, limit)) == 0)
				{
					return listeners.at(i); // placed session in a suitable listener, abort
				}
			}
			// if no suitable listener has been found (e.g. all equally charged), then force first listener to take it.
			listeners.first().logic.register(session, Integer.MAX_VALUE);
			// note: the case that the listener has MAX size and thus will reject the session again can be neglected.
			return listeners.first();
		}


		@Override
		public synchronized void register(final S session)
		{
			this.sessionMap.put(
				session,
				synchRegisterSession(this.listeners, session)
			);
		}

		@Override
		public synchronized void remove(final S session)
		{
			final ListenerThread thread = this.sessionMap.get(session);
			if(thread == null)
			{
				throw new IllegalArgumentException(); // (29.10.2012)EXCP: proper exception
			}
			// ignore if session was actually removed or got removed before already (e.g. due to IoException)
			thread.logic.removeSession(session);
			this.sessionMap.remove(session);
		}

	}

}
