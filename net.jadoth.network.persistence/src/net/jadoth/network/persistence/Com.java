package net.jadoth.network.persistence;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.jadoth.low.XVM;
import net.jadoth.math.XMath;

public class Com
{
	public static int defaultPort()
	{
		return 1337;
	}
	
	public static ByteOrder byteOrder()
	{
		return XVM.nativeByteOrder();
	}
	
	public static int validatePort(final int port)
	{
		return XMath.positive(port);
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
	
	public static SocketChannel openChannelLocalhost() throws IOException
	{
		return openChannel(InetAddress.getLocalHost());
	}
	
	public static SocketChannel openChannel(final InetAddress address) throws IOException
	{
		return openChannel(address, defaultPort());
	}
	
	public static SocketChannel openChannel(final InetAddress address, final int port) throws IOException
	{
		final SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(address, port));
		return socketChannel;
	}
	
	public static SocketChannel accept(final ServerSocketChannel serverSocketChannel)
	{
		final SocketChannel socketChannel;
		try
		{
			socketChannel = serverSocketChannel.accept();
		}
		catch(final Exception e)
		{
			// (01.11.2018 TM)EXCP: proper exception
			throw new RuntimeException(e);
		}
		
		return socketChannel;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private Com()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
