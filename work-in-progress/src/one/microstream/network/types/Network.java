package one.microstream.network.types;

import java.nio.channels.ServerSocketChannel;

public final class Network
{
	public static NetworkConnectionSocket wrapServerSocketChannel(final ServerSocketChannel serverSocketChannel)
	{
		return new NetworkConnectionSocket.Implementation(serverSocketChannel);
	}


	private Network()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
