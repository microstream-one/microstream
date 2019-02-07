package net.jadoth.com.binary;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.X;
import net.jadoth.com.Com;
import net.jadoth.com.ComClient;
import net.jadoth.com.ComClientChannel;
import net.jadoth.com.ComException;
import net.jadoth.com.ComExceptionTimeout;
import net.jadoth.com.ComFoundation;
import net.jadoth.com.ComHost;
import net.jadoth.com.ComHostChannelAcceptor;
import net.jadoth.com.XSockets;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.BinaryPersistence;

public class ComBinary
{
	/**
	 * The length of the fixed-size chunk header.<p>
	 * So far, the header only consists of one field holding the length of the chunk content.
	 * See {@link BinaryPersistence#lengthLength()}.
	 * 
	 * In the future, the header might contain validation values like protocol name, version, byte order, etc.<br>
	 * Maybe, the consequence will be a dynamically sized header, meaning there
	 * 
	 * 
     * @return The length of the fixed-size chunk header.
	 */
	public static int chunkHeaderLength()
	{
		return Long.BYTES;
	}
	
	public static long getChunkHeaderContentLength(
		final ByteBuffer directByteBuffer ,
		final boolean    switchedByteOrder
	)
	{
		return switchedByteOrder
			? Long.reverseBytes(XMemory.get_long(XMemory.getDirectByteBufferAddress(directByteBuffer)))
			:                   XMemory.get_long(XMemory.getDirectByteBufferAddress(directByteBuffer))
		;
	}
	
	public static ByteBuffer setChunkHeaderContentLength(
		final ByteBuffer directByteBuffer ,
		final long       contentLength    ,
		final boolean    switchedByteOrder
	)
	{
		directByteBuffer.clear().limit(ComBinary.chunkHeaderLength());
		XMemory.set_long(
			XMemory.getDirectByteBufferAddress(directByteBuffer),
			switchedByteOrder
			? Long.reverseBytes(contentLength)
			:                   contentLength
		);
		
		return directByteBuffer;
	}
	
	/* (10.08.2018 TM)TODO: Better network timeout handling
	 * The simplistic int value should be replaced by a NetworkTimeoutEvaluator.
	 * Every time a read event leaves the target buffer with remaining bytes,
	 * the evaluator is called with the following arguments:
	 * - time instant when the filling of the buffer started
	 * - total amount of required bytes
	 * - timestamp of the last time bytes were received
	 * - amount of received bytes (or buffer remaining bytes or something like that)
	 * 
	 * This allows arbitrarily complex evaluation logic.
	 * For example:
	 * - abort if the transfer speed (bytes/s) drops too low, even though there are still bytes coming in.
	 * - abort if the whole process takes too long.
	 * - abort if the time with 0 bytes received gets too long.
	 * 
	 */
	public static int operationTimeout()
	{
		return 1000;
	}
	
	
	public static ByteBuffer readChunk(
		final SocketChannel channel          ,
		final ByteBuffer    defaultBuffer    ,
		final boolean       switchedByteOrder
	)
		throws ComException, ComExceptionTimeout
	{
		ByteBuffer filledHeaderBuffer;
		ByteBuffer filledContentBuffer;
		
		// the known-length header is read into a buffer
		filledHeaderBuffer = XSockets.readIntoBufferKnownLength(
			channel,
			defaultBuffer,
			operationTimeout(),
			ComBinary.chunkHeaderLength()
		);
		
		// the header starts with the content length (and currently, that is the whole header)
		final long chunkContentLength = ComBinary.getChunkHeaderContentLength(
			filledHeaderBuffer,
			switchedByteOrder
		);
		
		/* (13.11.2018 TM)NOTE:
		 * Should the header contain validation meta-data in the future, they have to be validated here.
		 * This would probably mean turning this method into a instance of a com-handling type.
		 */
		
		// the content after the header is read into a buffer since the header has already been siphoned off.
		filledContentBuffer = XSockets.readIntoBufferKnownLength(
			channel,
			defaultBuffer,
			operationTimeout(),
			X.checkArrayRange(chunkContentLength)
		);
		
		return filledContentBuffer;
	}
	
	public static void writeChunk(
		final SocketChannel channel     ,
		final ByteBuffer    headerBuffer,
		final ByteBuffer[]  buffers
	)
		throws ComException, ComExceptionTimeout
	{
		// the chunk header (specifying the chunk data length) is sent first, then the actual chunk data.
		XSockets.writeFromBuffer(channel, headerBuffer, operationTimeout());
		
		for(final ByteBuffer bb : buffers)
		{
			XSockets.writeFromBuffer(channel, bb, operationTimeout());
		}
	}
	
	
	public static ComPersistenceAdaptorBinary.Creator.Default DefaultPersistenceAdaptorCreator()
	{
		return ComPersistenceAdaptorBinary.Creator();
	}
	
	public static ComFoundation.Default<?> Foundation()
	{
		return ComFoundation.New()
			.setPersistenceAdaptorCreator(DefaultPersistenceAdaptorCreator())
		;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// convenience methods //
	////////////////////////
	
	public static final ComHost<SocketChannel> Host()
	{
		return Com.Host(DefaultPersistenceAdaptorCreator());
	}
	
	public static final ComHost<SocketChannel> Host(
		final int localHostPort
	)
	{
		return Com.Host(localHostPort, DefaultPersistenceAdaptorCreator());
	}
	
	public static final ComHost<SocketChannel> Host(
		final InetSocketAddress  targetAddress
	)
	{
		return Com.Host(targetAddress, DefaultPersistenceAdaptorCreator());
	}
	
	public static final ComHost<SocketChannel> Host(
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		return Com.Host(
			DefaultPersistenceAdaptorCreator(),
			channelAcceptor
		);
	}
	
	public static final ComHost<SocketChannel> Host(
		final int                                   localHostPort  ,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		return Com.Host(
			DefaultPersistenceAdaptorCreator(),
			channelAcceptor
		);
	}
	
	public static final ComHost<SocketChannel> Host(
		final InetSocketAddress                     targetAddress  ,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		return Com.Host(targetAddress, DefaultPersistenceAdaptorCreator(), channelAcceptor);
	}
	
	
	public static final void runHost()
	{
		runHost(null, null);
	}
	
	public static final void runHost(
		final int localHostPort
	)
	{
		runHost(localHostPort, null);
	}
	
	public static final void runHost(
		final InetSocketAddress targetAddress
	)
	{
		runHost(targetAddress, null);
	}
	
	public static final void runHost(
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(),
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final int                                   localHostPort  ,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(localHostPort),
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final InetSocketAddress                     targetAddress  ,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		final ComHost<SocketChannel> host = Host(targetAddress, channelAcceptor);
		host.run();
	}
	
	public static final ComClient<SocketChannel> Client()
	{
		return Com.Client(
			DefaultPersistenceAdaptorCreator()
		);
	}
	
	public static final ComClient<SocketChannel> Client(final int localHostPort)
	{
		return Com.Client(
			localHostPort                     ,
			DefaultPersistenceAdaptorCreator()
		);
	}
		
	public static final ComClient<SocketChannel> Client(
		final InetSocketAddress targetAddress
	)
	{
		return Com.Client(
			targetAddress,
			DefaultPersistenceAdaptorCreator()
		);
	}
	
	
	public static final ComClientChannel<SocketChannel> connect()
	{
		return Client()
			.connect()
		;
	}
	
	public static final ComClientChannel<SocketChannel> connect(
		final int localHostPort
	)
	{
		return Client(localHostPort)
			.connect()
		;
	}
		
	public static final ComClientChannel<SocketChannel> connect(
		final InetSocketAddress targetAddress
	)
	{
		return Client(targetAddress)
			.connect()
		;
	}
	
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private ComBinary()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
