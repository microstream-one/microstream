package net.jadoth.network.persistence;

import static net.jadoth.X.notNull;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

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
		
		private transient ServerSocketChannel serverSocketChannel;
		
		
		
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
			if(this.serverSocketChannel != null)
			{
				return;
			}
			
			this.synchOpenServerSocketChannel();
			
			// (01.11.2018 TM)TODO: JET-44: weird to have the work loop on a stack frame called "start".
			this.acceptConnections();
		}
		
		private void synchOpenServerSocketChannel()
		{
			try
			{
				this.serverSocketChannel = Com.openServerSocketChannel(this.port);
			}
			catch(final IOException e)
			{
				// (01.11.2018 TM)EXCP: proper exception
				throw new RuntimeException(e);
			}
		}

		@Override
		public synchronized void stop()
		{
			if(this.serverSocketChannel == null)
			{
				return;
			}
			
			Com.close(this.serverSocketChannel);
			this.serverSocketChannel = null;
		}

		@Override
		public synchronized boolean isRunning()
		{
			return this.serverSocketChannel != null;
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
			final SocketChannel socketChannel = Com.accept(this.serverSocketChannel);
			this.connectionAcceptor.acceptConnection(socketChannel);
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
