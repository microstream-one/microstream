package net.jadoth.com;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface ComConnectionListenerCreator<C>
{
	public ComConnectionListener<C> createConnectionListener(InetSocketAddress address);
	
	
	
	public static ComConnectionListenerCreator.Default New()
	{
		return new ComConnectionListenerCreator.Default();
	}
	
	public final class Default implements ComConnectionListenerCreator<SocketChannel>
	{

		@Override
		public ComConnectionListener<SocketChannel> createConnectionListener(final InetSocketAddress address)
		{
			// (12.11.2018 TM)TODO: move all Socket-logic to a central static context.
			try
			{
				final ServerSocketChannel serverChannel = ServerSocketChannel.open();
				serverChannel.socket().bind(address);
				return ComConnectionListener.New(serverChannel);
			}
			catch(final IOException e)
			{
				// (12.11.2018 TM)EXCP: proper exception
				throw new RuntimeException(e);
			}
			
		}
		
	}
	
}
