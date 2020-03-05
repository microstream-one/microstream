package one.microstream.com;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import one.microstream.memory.XMemory;

public final class XSockets
{
	public static ByteOrder byteOrder()
	{
		return ByteOrder.nativeOrder();
	}
	
	
	public static final ServerSocketChannel openServerSocketChannel(final InetSocketAddress address)
		throws ComException
	{
		try
		{
			final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(address);
			return serverSocketChannel;
		}
		catch(final IOException e)
		{
			// (12.11.2018 TM)EXCP: proper exception
			throw new ComException(e);
		}
		
	}
		
	public static final SocketChannel acceptSocketChannel(final ServerSocketChannel serverSocketChannel)
		throws ComException
	{
		try
		{
			return serverSocketChannel.accept();
		}
		catch(final Exception e)
		{
			// (12.11.2018 TM)EXCP: proper exception
			throw new ComException(e);
		}
	}
	
	public static SocketChannel openChannel(final InetSocketAddress address) throws ComException
	{
		notNull(address);
		try
		{
			final SocketChannel socketChannel = SocketChannel.open();
			socketChannel.connect(address);
			return socketChannel;
		}
		catch(final IOException e)
		{
			// (12.11.2018 TM)EXCP: proper exception
			throw new ComException(e);
		}
	}
	
	/**
	 * Alias for {@link InetAddress#getLocalHost()}.
	 * 
	 * @return the localhost {@link InetAddress}.
	 * @throws ComException if {@link InetAddress#getLocalHost()} throws an {@link UnknownHostException}
	 */
	public static InetAddress localHostAddress() throws ComException
	{
		try
		{
			return InetAddress.getLocalHost();
		}
		catch(final UnknownHostException e)
		{
			// (12.11.2018 TM)EXCP: proper exception
			throw new ComException(e);
		}
	}
	
	/**
	 * Creates a new {@link InetSocketAddress} instance with {@link #localHostAddress()} and port 0 (ephemeral port).
	 * 
	 * @return a localhost {@link InetSocketAddress}.
	 * @throws ComException
	 * 
	 * @see InetSocketAddress#InetSocketAddress(InetAddress, int)
	 */
	public static InetSocketAddress localHostSocketAddress() throws ComException
	{
		return localHostSocketAddress(0);
	}
	
	/**
	 * Creates a new {@link InetSocketAddress} instance with {@link #localHostAddress()} and the passed port value.
	 * 
	 * @param port the port to be used.
	 * @return a localhost {@link InetSocketAddress} with the passed port value.
	 * @throws ComException
	 * 
	 * @see InetSocketAddress#InetSocketAddress(InetAddress, int)
	 */
	public static InetSocketAddress localHostSocketAddress(final int port) throws ComException
	{
		return new InetSocketAddress(localHostAddress(), port);
	}
	
	public static SocketChannel openChannelLocalhost() throws ComException
	{
		return openChannel(
			localHostSocketAddress()
		);
	}
	
	public static SocketChannel openChannelLocalhost(final int port) throws ComException
	{
		return openChannel(
			localHostSocketAddress(port)
		);
	}
	
	public static final void closeChannel(final NetworkChannel channel) throws ComException
	{
		try
		{
			channel.close();
		}
		catch(final Exception e)
		{
			// (12.11.2018 TM)EXCP: proper exception
			throw new ComException(e);
		}
	}
	
	/**
	 * This method either writes all of the passed {@link ByteBuffer}'s bytes from position to limit
	 * or it throws an exception to indicate failure.
	 * 
	 * @param socketChannel
	 * @param byteBuffer
	 * 
	 * @return the passed {@link ByteBuffer} instance.
	 */
	public static ByteBuffer writeCompletely(final SocketChannel socketChannel, final ByteBuffer byteBuffer)
		throws ComException
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
			socketChannel.write(byteBuffer);
			return byteBuffer;
		}
		catch(final IOException e)
		{
			// (01.11.2018 TM)EXCP: proper exception
			throw new ComException(e);
		}
	}
	
	/**
	 * This method either read to completely fill the passed {@link ByteBuffer} from position to limit
	 * or it throws an exception to indicate failure.
	 * 
	 * @param socketChannel
	 * @param byteBuffer
	 * 
	 * @return the passed {@link ByteBuffer} instance.
	 */
	public static ByteBuffer readCompletely(final SocketChannel socketChannel, final ByteBuffer byteBuffer)
		throws ComException
	{
		/* (01.11.2018 TM)TODO: reliable socket channel reading
		 * see writeCompletely
		 */
		try
		{
			socketChannel.read(byteBuffer);
			return byteBuffer;
		}
		catch(final IOException e)
		{
			// (01.11.2018 TM)EXCP: proper exception
			throw new ComException(e);
		}
	}


	// (10.08.2018 TM)TODO: make IO_LOOP_SLEEP_TIME dynamic
	private static final int IO_LOOP_SLEEP_TIME = 10;
	
	public static final ByteBuffer readIntoBufferKnownLength(
		final SocketChannel channel        ,
		final ByteBuffer    buffer         ,
		final int           responseTimeout,
		final int           length
	)
		throws ComException
	{
		final ByteBuffer checkedBuffer;

		if(length > buffer.capacity())
		{
			checkedBuffer = XMemory.allocateDirectNative(length);
		}
		else
		{
			(checkedBuffer = buffer).clear().limit(length);
		}
		readIntoBuffer(channel, checkedBuffer, responseTimeout);
		
		// note: intentionally no flip() here, as the position is interpreted as the content length later on.

		return checkedBuffer;
	}
	
	private interface IoOperation
	{
		public void execute(SocketChannel channel, ByteBuffer buffer) throws ComException; // funny "public"
	}
	
	public static void read(final SocketChannel channel, final ByteBuffer buffer) throws ComException
	{
		try
		{
			channel.read(buffer);
		}
		catch(final IOException e)
		{
			// (01.11.2018 TM)EXCP: proper exception
			throw new ComException(e);
		}
	}
	
	public static void write(final SocketChannel channel, final ByteBuffer buffer) throws ComException
	{
		try
		{
			channel.write(buffer);
		}
		catch(final IOException e)
		{
			// (01.11.2018 TM)EXCP: proper exception
			throw new ComException(e);
		}
	}
	
	public static void readIntoBuffer(final SocketChannel channel, final ByteBuffer buffer, final int responseTimeout)
		throws ComException, ComExceptionTimeout
	{
		performIoOperation(buffer, XSockets::read, channel, responseTimeout);
	}

	public static void writeFromBuffer(final SocketChannel channel, final ByteBuffer buffer, final int responseTimeout)
		throws ComException, ComExceptionTimeout
	{
		performIoOperation(buffer, XSockets::write, channel, responseTimeout);
	}

	private static void performIoOperation(
		final ByteBuffer    buffer,
		final IoOperation   operation,
		final SocketChannel channel,
		final int           responseTimeout
		// (04.11.2012 TM)XXX: performIoOperation: add a second timeout for the whole communication process?
	)
		throws ComException, ComExceptionTimeout
	{
		long responseTimeoutPoint = System.currentTimeMillis() + responseTimeout;
		
		// monitor progress via remaining bytes to avoid unnecessary up-front read count storage
		long remaining = buffer.remaining();
		while(true)
		{
			// runtime overhead for operation abstracting should be negligible and VM-optimizable
			operation.execute(channel, buffer);
			if(!buffer.hasRemaining())
			{
				break; // all bytes read, leave loop
			}
			if(buffer.remaining() < remaining)
			{
				// reset timeout if new bytes arrived
				responseTimeoutPoint = System.currentTimeMillis() + responseTimeout;
				remaining = buffer.remaining();
			}
			else if(System.currentTimeMillis() >= responseTimeoutPoint)
			{
				// otherwise check for timeout
				throw new ComExceptionTimeout();
			}
			
			try
			{
				Thread.sleep(IO_LOOP_SLEEP_TIME);
				continue; // restart loop to try writing more bytes
			}
			catch(final InterruptedException e)
			{
				// if interrupted (rather academic in this simple example) just abort. Calling context must handle.
				return;
			}
		}
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XSockets()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
