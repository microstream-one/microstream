package one.microstream.network.simplesession;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import one.microstream.memory.XMemory;
import one.microstream.network.exceptions.NetworkExceptionTimeout;
import one.microstream.network.types.Network;
import one.microstream.network.types.NetworkConnectionSocket;
import one.microstream.network.types.NetworkFactoryUserSessionServer;


public final class LogicSimpleNetwork
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	/* (15.04.2016 TM)XXX: Very strange idea to put that there.
	 * Probably an oversight leftover from a test class that evolved into a util class
	 * Must be overhauled
	 */
	private static final int     PORT                     = 1337;
	private static final Charset CHARSET                  = StandardCharsets.UTF_8;
	private static final int     DEFAULT_RESPONSE_TIMEOUT = 100000; // ms
	private static final int     IO_LOOP_SLEEP_TIME       = 10; // ms // not sure if making this dynamic is necessary

	private static final int     BYTE_SIZE_LENGTH_HEADER  = 4;

	private static final SimpleSessionProtocol SINGLETON_PROTOCOL = new SimpleSessionProtocol();



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final int defaultPort()
	{
		return PORT;
	}

	public static final SimpleSessionProtocol protocol()
	{
		return SINGLETON_PROTOCOL;
	}

	public static final NetworkFactoryUserSessionServer.Default<SimpleSessionUser, SimpleSession> serverFactory()
	{
		return new NetworkFactoryUserSessionServer.Default<>();
	}

	// should be pretty well optimizable by the VM if it can be certain there are only 2 implementations
	private interface IoOperation
	{
		public void execute(SocketChannel channel, ByteBuffer buffer) throws IOException; // funny "public"
	}
	
	private static final IoOperation READ = new IoOperation()
	{
		@Override
		public void execute(final SocketChannel channel, final ByteBuffer buffer) throws IOException
		{
			channel.read(buffer);
		}
	};
	
	private static final IoOperation WRITE = new IoOperation()
	{
		@Override
		public void execute(final SocketChannel channel, final ByteBuffer buffer) throws IOException
		{
			channel.write(buffer);
		}
	};
	
	// note that functions are logic, not constants, hence located here and written in lower case.

	private static void fillBuffer(final SocketChannel channel, final ByteBuffer buffer, final int responseTimeout)
		throws IOException, NetworkExceptionTimeout
	{
		performIoOperation(buffer, READ, channel, responseTimeout);
	}

	private static void flushBuffer(final SocketChannel channel, final ByteBuffer buffer, final int responseTimeout)
		throws IOException, NetworkExceptionTimeout
	{
		performIoOperation(buffer, WRITE, channel, responseTimeout);
	}

	private static void performIoOperation(
		final ByteBuffer    buffer,
		final IoOperation   operation,
		final SocketChannel channel,
		final int           responseTimeout
		// (04.11.2012)XXX: performIoOperation: add a second timeout for the whole communication process?
	)
		throws IOException, NetworkExceptionTimeout
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
				throw new NetworkExceptionTimeout();
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

	public static final void resetBuffer(final ByteBuffer byteBuffer)
	{
		byteBuffer.order(ByteOrder.BIG_ENDIAN).clear().limit(BYTE_SIZE_LENGTH_HEADER);
	}

	// sending //

	public static void sendString(
		final String        string         ,
		final SocketChannel channel        ,
		final ByteBuffer    buffer         ,
		final int           responseTimeout
	)
		throws IOException
	{
		final ByteBuffer checkedBuffer;

		final byte[] bytes = string.getBytes(CHARSET);
		if(buffer.capacity() < bytes.length + BYTE_SIZE_LENGTH_HEADER)
		{
			checkedBuffer = ByteBuffer.allocateDirect(bytes.length + BYTE_SIZE_LENGTH_HEADER);
		}
		else
		{
			(checkedBuffer = buffer).clear().limit(bytes.length + BYTE_SIZE_LENGTH_HEADER);
		}
		checkedBuffer.order(ByteOrder.BIG_ENDIAN).putInt(bytes.length).put(bytes).flip();
		flushBuffer(channel, checkedBuffer, responseTimeout);
	}

	public static final void sendString(final String string, final SocketChannel channel, final int responseTimeout)
		throws IOException
	{
		sendString(string, channel, ByteBuffer.allocateDirect(XMemory.pageSize()), responseTimeout);
	}

	public static final void sendString(final String string, final SocketChannel channel)
		throws IOException
	{
		sendString(string, channel, ByteBuffer.allocateDirect(XMemory.pageSize()), DEFAULT_RESPONSE_TIMEOUT);
	}

	// reading //

	public static final int readStringLength(
		final SocketChannel channel        ,
		final ByteBuffer    buffer         ,
		final int           responseTimeout
	)
		throws IOException
	{
		// enforce network standard byte order no matter what
		resetBuffer(buffer);
		fillBuffer(channel, buffer, responseTimeout);
		buffer.clear();
		return buffer.getInt();
	}

	public static final ByteBuffer readBytes(
		final SocketChannel channel        ,
		final ByteBuffer    buffer         ,
		final int           responseTimeout,
		final int           length
	)
		throws IOException
	{
		final ByteBuffer checkedBuffer;

		if(length > buffer.capacity())
		{
			checkedBuffer = ByteBuffer.allocateDirect(length);
		}
		else
		{
			(checkedBuffer = buffer).clear().limit(length);
		}
		fillBuffer(channel, checkedBuffer, responseTimeout);
		checkedBuffer.flip();
		return checkedBuffer;
	}

	public static final String completeReadString(
		final SocketChannel channel        ,
		final ByteBuffer    buffer         ,
		final int           responseTimeout,
		final int           maxStringLength
	)
		throws IOException
	{
		if(buffer.position() < BYTE_SIZE_LENGTH_HEADER)
		{
			fillBuffer(channel, buffer, responseTimeout);
		}
		buffer.clear();
		return readKnownLengthString(channel, buffer, responseTimeout, maxStringLength, buffer.getInt());
	}

	public static final String readString(
		final SocketChannel channel        ,
		final ByteBuffer    buffer         ,
		final int           responseTimeout,
		final int           maxStringLength
	)
		throws IOException
	{
		final int length = readStringLength(channel, buffer, responseTimeout);
		return readKnownLengthString(channel, buffer, responseTimeout, maxStringLength, length);
	}

	private static final String readKnownLengthString(
		final SocketChannel channel        ,
		final ByteBuffer    buffer         ,
		final int           responseTimeout,
		final int           maxStringLength,
		final int           actualLength
	)
		throws IOException
	{
		if(actualLength > maxStringLength)
		{
			// e.g. prevent unauthenticated connection applicants from flooding the system
			throw new RuntimeException("Invalid message length: " + actualLength); // trivial exception for simplicity
		}

		final ByteBuffer checkedBuffer = readBytes(channel, buffer, responseTimeout, actualLength);
		final byte[] bytes;
		checkedBuffer.get(bytes = new byte[actualLength]);
		return new String(bytes, CHARSET);
	}

	public static final String readString(
		final SocketChannel channel,
		final int           responseTimeout,
		final int           maxStringLength
	)
		throws IOException
	{
		return readString(channel, ByteBuffer.allocateDirect(XMemory.pageSize()), responseTimeout, maxStringLength);
	}

	public static final String readString(final SocketChannel channel, final int maxStringLength) throws IOException
	{
		return readString(
			channel,
			ByteBuffer.allocateDirect(XMemory.pageSize()),
			DEFAULT_RESPONSE_TIMEOUT,
			maxStringLength
		);
	}

	public static final String readString(final SocketChannel channel) throws IOException
	{
		return readString(channel, Integer.MAX_VALUE);
	}

	// channel utils //

	public static final String communicate(final SocketChannel channel, final String message) throws IOException
	{
		sendString(message, channel);
		return readString(channel);
	}

	public static SocketChannel openRemoteChannel(final InetAddress address) throws IOException
	{
		final SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(address, defaultPort()));
		return socketChannel;
	}

	public static ServerSocketChannel openNioSocket() throws IOException
	{
		final ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(new InetSocketAddress(defaultPort()));
		return serverChannel;
	}

	public static NetworkConnectionSocket openConnectionSocket() throws IOException
	{
		return Network.wrapServerSocketChannel(openNioSocket());
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private LogicSimpleNetwork()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
