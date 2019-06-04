package one.microstream.network.types;

import java.nio.channels.ServerSocketChannel;

public final class Network
{
	public static NetworkConnectionSocket wrapServerSocketChannel(final ServerSocketChannel serverSocketChannel)
	{
		return new NetworkConnectionSocket.Default(serverSocketChannel);
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private Network()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
