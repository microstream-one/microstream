package net.jadoth.network.persistence.binary;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.jadoth.persistence.types.BufferSizeProvider;

public class MainTestNetworkPersistenceServer
{
	public static void main(final String[] args)
	{
		
	}
	
	public static void run(final ServerSocketChannel serverSocketChannel)
	{
		final SocketChannel newConnection;
		try
		{
			newConnection = serverSocketChannel.accept();
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
		
		final NetworkPersistenceChannelBinary channel = NetworkPersistenceChannelBinary.New(
			newConnection,
			BufferSizeProvider.New()
		);
	}
	
	public static int defaultPort()
	{
		return 1337;
	}
	
	public static ServerSocketChannel openServerSocketChannel() throws IOException
	{
		return openServerSocketChannel(defaultPort());
	}
	
	public static ServerSocketChannel openServerSocketChannel(final int port) throws IOException
	{
		final ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(new InetSocketAddress(port));
		return serverChannel;
	}
}
