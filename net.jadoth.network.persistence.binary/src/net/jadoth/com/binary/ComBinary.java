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
import net.jadoth.com.XSockets;
import net.jadoth.low.XVM;
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
		return BinaryPersistence.lengthLength();
	}
	
	public static long getChunkHeaderContentLength(final ByteBuffer directByteBuffer)
	{
		return XVM.get_long(XVM.getDirectByteBufferAddress(directByteBuffer));
	}
	
	public static ByteBuffer setChunkHeaderContentLength(
		final ByteBuffer directByteBuffer,
		final long       contentLength
	)
	{
		directByteBuffer.clear().limit(ComBinary.chunkHeaderLength());
		XVM.set_long(XVM.getDirectByteBufferAddress(directByteBuffer), contentLength);
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
		final SocketChannel channel      ,
		final ByteBuffer    defaultBuffer
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
			filledHeaderBuffer
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
	
	
	public static final ComClient<SocketChannel> Client()
	{
		return Com.Client(
			DefaultPersistenceAdaptorCreator()
		);
	}
	
	public static final ComClient<SocketChannel> Client(final int localHostPort)
	{
		return Com.Client(
			localHostPort              ,
			DefaultPersistenceAdaptor()
		);
	}
		
	public static final ComClient<SocketChannel> Client(
		final InetSocketAddress targetAddress
	)
	{
		return Com.Client(
			targetAddress,
			DefaultPersistenceAdaptor()
		);
	}
	
	public static final ComClient<SocketChannel> Client(
		final ComPersistenceAdaptorBinary<SocketChannel> persistenceAdaptor
	)
	{
		return Com.Client(persistenceAdaptor);
	}
	
	public static final ComClient<SocketChannel> Client(
		final int                                        localHostPort     ,
		final ComPersistenceAdaptorBinary<SocketChannel> persistenceAdaptor
	)
	{
		return Com.Client(localHostPort, persistenceAdaptor);
	}
	
	public static final ComClient<SocketChannel> Client(
		final InetSocketAddress                          targetAddress     ,
		final ComPersistenceAdaptorBinary<SocketChannel> persistenceAdaptor
	)
	{
		return Com.Client(targetAddress, persistenceAdaptor);
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
	
	public static final ComClientChannel<SocketChannel> connect(
		final ComPersistenceAdaptorBinary<SocketChannel> persistenceAdaptor
	)
	{
		return Client(persistenceAdaptor)
			.connect()
		;
	}
	
	public static final ComClientChannel<SocketChannel> connect(
		final int                                        localHostPort     ,
		final ComPersistenceAdaptorBinary<SocketChannel> persistenceAdaptor
	)
	{
		return Client(localHostPort, persistenceAdaptor)
			.connect()
		;
	}
	
	public static final ComClientChannel<SocketChannel> connect(
		final InetSocketAddress                          targetAddress     ,
		final ComPersistenceAdaptorBinary<SocketChannel> persistenceAdaptor
	)
	{
		return Client(targetAddress, persistenceAdaptor)
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
