package one.microstream.com;

import static one.microstream.X.notNull;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface ComConnectionListener<C>
{
	public C listenForConnection();
	
	public void close();
	
	
	
	public static ComConnectionListener.Default Default(final ServerSocketChannel serverSocketChannel)
	{
		return new ComConnectionListener.Default(
			notNull(serverSocketChannel)
		);
	}
	
	public final class Default implements ComConnectionListener<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ServerSocketChannel serverSocketChannel;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final ServerSocketChannel serverSocketChannel)
		{
			super();
			this.serverSocketChannel = serverSocketChannel;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final SocketChannel listenForConnection()
		{
			return XSockets.acceptSocketChannel(this.serverSocketChannel);
		}

		@Override
		public final void close()
		{
			XSockets.closeChannel(this.serverSocketChannel);
		}
		
	}
	
}
