package net.jadoth.network.persistence.binary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.memory.Memory;
import net.jadoth.network.exceptions.NetworkExceptionTimeout;
import net.jadoth.persistence.binary.types.BinaryPersistence;

public class NetworkPersistenceBinary
{
	
	public static int networkChunkHeaderLength()
	{
		/* currently just a plain simple single length value.
		 * Will be more in the future, though. E.g. endianess, protocol version, etc.
		 */
		return BinaryPersistence.lengthLength();
	}
	
	public static long readNetworkChunkContentLength(final long networkChunkHeaderOffset)
	{
		return Memory.get_long(networkChunkHeaderOffset);
	}
	
	
	// (10.08.2018 TM)TODO: make IO_LOOP_SLEEP_TIME dynamic
	private static final int IO_LOOP_SLEEP_TIME = 10;
	
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
	
	private interface IoOperation
	{
		public void execute(SocketChannel channel, ByteBuffer buffer) throws IOException; // funny "public"
	}
	
	private static void read(final SocketChannel channel, final ByteBuffer buffer) throws IOException
	{
		channel.read(buffer);
	}
	
	private static void write(final SocketChannel channel, final ByteBuffer buffer) throws IOException
	{
		channel.write(buffer);
	}
	
	private static void fillBuffer(final SocketChannel channel, final ByteBuffer buffer, final int responseTimeout)
		throws IOException, NetworkExceptionTimeout
	{
		performIoOperation(buffer, NetworkPersistenceBinary::read, channel, responseTimeout);
	}

	private static void flushBuffer(final SocketChannel channel, final ByteBuffer buffer, final int responseTimeout)
		throws IOException, NetworkExceptionTimeout
	{
		performIoOperation(buffer, NetworkPersistenceBinary::write, channel, responseTimeout);
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
	
}
