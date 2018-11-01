package net.jadoth.network.persistence;

import static net.jadoth.X.notNull;

/**
 * Host type to listen for new connections and relay them to logic for further processing,
 * potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
public interface ComHost
{
	public int port();
	
	public ComConfiguration configuration();
	
	/**
	 * Listens for incoming connections and relays them for processing.
	 */
	public void acceptConnections();
	
	public void start();
	
	public void stop();
	
	public boolean isRunning();
	
	/* (31.10.2018 TM)TODO: JET-44
	 * - ComConfiguration
	 * - Network Configuration (port and stuff)
	 * - A target for accepted connections
	 */
	
	
	
	public static ComHost New(final int port, final ComConnectionAcceptor connectionAcceptor)
	{
		return new ComHost.Implementation(
			Com.validatePort(port),
			notNull(connectionAcceptor)
		);
	}
	
	public final class Implementation implements ComHost
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int                   port              ;
		private final ComConnectionAcceptor connectionAcceptor;
		
		private transient boolean isRunning;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final int port, final ComConnectionAcceptor connectionAcceptor)
		{
			super();
			this.port               = port              ;
			this.connectionAcceptor = connectionAcceptor;
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
		public final ComConfiguration configuration()
		{
			return this.connectionAcceptor.configuration();
		}

		@Override
		public synchronized void start()
		{
			if(this.isRunning)
			{
				return;
			}
			
			this.isRunning = true;
			
			// (01.11.2018 TM)TODO: JET-44: weird to have the work loop on a stack frame called "start".
			this.acceptConnections();
		}

		@Override
		public synchronized void stop()
		{
			if(!this.isRunning)
			{
				return;
			}
			
			this.isRunning = false;
		}

		@Override
		public synchronized boolean isRunning()
		{
			return this.isRunning;
		}

		@Override
		public void acceptConnections()
		{
			/* (01.11.2018 TM)FIXME: ComHost#acceptConnections()
			 * in a loop:
			 * - get lock
			 * - check if running
			 * - listen for connection
			 * - relay to connectionAcceptor
			 */
			throw new net.jadoth.meta.NotImplementedYetError();
		}
	}
	
	
	
	public static ComHost.Creator Creator()
	{
		return new ComHost.Creator.Implementation();
	}
	
	public interface Creator
	{
		public ComHost createComHost(int port, ComConnectionAcceptor connectionAcceptor);
		
		public final class Implementation implements ComHost.Creator
		{
			@Override
			public final ComHost createComHost(final int port, final ComConnectionAcceptor connectionAcceptor)
			{
				return ComHost.New(port, connectionAcceptor);
			}
		}
	}
	
}
