package net.jadoth.com;

import java.net.InetSocketAddress;
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
			return ComConnectionListener.New(
				XSockets.openServerSocketChannel(address)
			);
		}
		
	}
	
}
