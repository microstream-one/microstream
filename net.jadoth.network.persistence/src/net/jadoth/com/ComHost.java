package net.jadoth.com;

import static net.jadoth.X.notNull;

/**
 * Host type to listen for new connections and relay them to logic for further processing,
 * potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
public interface ComHost<C>
{
	public int port();
	
	public ComProtocol protocol();
	
	/**
	 * Listens for incoming connections and relays them for processing.
	 */
	public void acceptConnections();
	
	public void start();
	
	public void stop();
	
	public boolean isRunning();
	
	
	
	public static <C> ComHost<C> New(
		final int                             port                     ,
		final ComConnectionListenerCreator<C> connectionListenerCreator,
		final ComConnectionAcceptor<C>        connectionAcceptor
	)
	{
		return new ComHost.Implementation<>(
			Com.validatePort(port),
			notNull(connectionListenerCreator),
			notNull(connectionAcceptor)
		);
	}
	
	public final class Implementation<C> implements ComHost<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int                             port                     ;
		private final ComConnectionListenerCreator<C> connectionListenerCreator;
		private final ComConnectionAcceptor<C>        connectionAcceptor       ;
		
		private transient ComConnectionListener<C> liveConnectionListener;
		
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final int                             port                     ,
			final ComConnectionListenerCreator<C> connectionListenerCreator,
			final ComConnectionAcceptor<C>        connectionAcceptor
		)
		{
			super();
			this.port                      = port                     ;
			this.connectionListenerCreator = connectionListenerCreator;
			this.connectionAcceptor        = connectionAcceptor       ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int port()
		{
			return this.port;
		}

		@Override
		public final ComProtocol protocol()
		{
			return this.connectionAcceptor.protocol();
		}

		@Override
		public synchronized void start()
		{
			if(this.liveConnectionListener != null)
			{
				return;
			}
			
			this.liveConnectionListener = this.connectionListenerCreator.createConnectionListener(this.port);
			
			// (01.11.2018 TM)TODO: JET-44: weird to have the work loop on a stack frame called "start".
			this.acceptConnections();
		}
		
		@Override
		public synchronized void stop()
		{
			if(this.liveConnectionListener == null)
			{
				return;
			}
			
			this.liveConnectionListener.close();
			this.liveConnectionListener = null;
		}

		@Override
		public synchronized boolean isRunning()
		{
			return this.liveConnectionListener != null;
		}

		@Override
		public void acceptConnections()
		{
			// repeatedly accept new connections until stopped.
			while(true)
			{
				synchronized(this)
				{
					if(!this.isRunning())
					{
						break;
					}
					
					this.synchAcceptConnection();
				}
			}
		}
		
		private void synchAcceptConnection()
		{
			final C connection = this.liveConnectionListener.listenForConnection();
			this.connectionAcceptor.acceptConnection(connection);
		}
	}
	
	
	
	public static <C> ComHostCreator<C> Creator()
	{
		return ComHostCreator.New();
	}
	
}
