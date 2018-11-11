package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.jadoth.low.XVM;
import net.jadoth.math.XMath;

public class Com
{
	public static long defaultObjectIdBaseServer()
	{
		return 9_200_000_000_000_000_000L;
	}
	
	public static long defaultObjectIdBaseClient()
	{
		return 9_100_000_000_000_000_000L;
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategy(final long startingObjectId)
	{
		return ComDefaultIdStrategy.New(startingObjectId);
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategyServer()
	{
		return DefaultIdStrategy(defaultObjectIdBaseServer());
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategyClient()
	{
		return DefaultIdStrategy(defaultObjectIdBaseClient());
	}
	
	public static ByteOrder byteOrder()
	{
		return XVM.nativeByteOrder();
	}
	
	public static int validatePort(final int port)
	{
		return XMath.positive(port);
	}
			
	public static ServerSocketChannel openServerSocketChannel(final InetSocketAddress address) throws IOException
	{
		final ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(address); // may be null according to bind() JavaDoc.
		return serverChannel;
	}
	
	public static SocketChannel openChannelLocalhost(final int port) throws IOException
	{
		return openChannel(
			new InetSocketAddress(InetAddress.getLocalHost(), port)
		);
	}
	
	public static SocketChannel openChannel(final InetSocketAddress address) throws IOException
	{
		notNull(address);
		final SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(address);
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
	
	public static void close(final SocketChannel socketChannel)
	{
		try
		{
			socketChannel.close();
		}
		catch(final Exception e)
		{
			// (01.11.2018 TM)EXCP: proper exception
			throw new RuntimeException(e);
		}
	}
	
	public static void close(final ServerSocketChannel serverSocketChannel)
	{
		try
		{
			serverSocketChannel.close();
		}
		catch(final Exception e)
		{
			// (01.11.2018 TM)EXCP: proper exception
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * This method either writes all of the passed {@link ByteBuffer}'s bytes from position to limit
	 * or it throws an exception to indicate failure.
	 * 
	 * @param socketChannel
	 * @param byteBuffer
	 * 
	 * @return the amount of bytes written, which always equals byteBuffer.remaining() at the time of the method call.
	 */
	public static int writeComplete(final SocketChannel socketChannel, final ByteBuffer byteBuffer)
	{
		/* (01.11.2018 TM)TODO: reliable socket channel writing
		 * full-grown IO-logic with:
		 * - a loop doing multiple attempts with waiting time in between
		 * - an interface for a checking type concerning:
		 * - timeout
		 * - time between last written byte
		 * - time and byte count since the beginning
		 * - amount of attempts
		 */
		try
		{
			return socketChannel.write(byteBuffer);
		}
		catch(final IOException e)
		{
			// (01.11.2018 TM)EXCP: proper exception
			throw new RuntimeException(e);
		}
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
