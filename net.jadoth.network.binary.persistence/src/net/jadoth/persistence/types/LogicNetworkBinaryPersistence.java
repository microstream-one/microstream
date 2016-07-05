package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.checkArrayRange;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.memory.Memory;
import net.jadoth.network.exceptions.NetworkExceptionTimeout;
import net.jadoth.persistence.binary.types.ChunksWrapper;

public final class LogicNetworkBinaryPersistence
{
	private static final int IO_LOOP_SLEEP_TIME = 10; // ms // not sure if making this dynamic is necessary
	private static final int CHUNK_LENGTH_LENGTH = 8; // length of chunk's length is always 8 (one long)


	public static final void resetBuffer(final ByteBuffer byteBuffer)
	{
		byteBuffer.clear().limit(CHUNK_LENGTH_LENGTH);
	}

	public static final ChunksWrapper completeReadChunk(
		final SocketChannel channel        ,
		final ByteBuffer    buffer         ,
		final int           responseTimeout,
		final int           maxChunkLength
	)
		throws IOException
	{
		// ensure that the buffer has fully received the chunk length
		if(buffer.position() < CHUNK_LENGTH_LENGTH)
		{
			fillBuffer(buffer, channel, responseTimeout);
		}
		buffer.flip();

		// read the chunk length and prepare buffer for receiving the actual chunk
		// (11.04.2013 TM)XXX: why clumsy getLong() with messed up endianess her?
		final int messageLength = checkArrayRange(buffer.getLong());
		buffer.clear();

		final ByteBuffer checkedBuffer;
		if(buffer.capacity() < messageLength)
		{
			Memory.deallocateDirectByteBuffer(buffer);
			checkedBuffer = ByteBuffer.allocateDirect(messageLength);
		}
		else
		{
			(checkedBuffer = buffer).limit(messageLength);
		}
		fillBuffer(checkedBuffer, channel, responseTimeout);
		checkedBuffer.flip();

		// wrap chunk data as a Binary instance and return it
		return ChunksWrapper.New(checkedBuffer);
	}


	private static void fillBuffer(
		final ByteBuffer    buffer,
		final SocketChannel channel,
		final int           responseTimeout
		// (04.11.2012)XXX: read: add a second timeout for the whole communication process?
	)
		throws IOException, NetworkExceptionTimeout
	{
		long respTimeoutPoint = System.currentTimeMillis() + responseTimeout;
		// monitor progress via remaining bytes to avoid unnecessary up-front read count storage
		long remaining = buffer.remaining();
		while(true)
		{
			channel.read(buffer);
			if(!buffer.hasRemaining())
			{
				break; // buffer filled, leave loop
			}
			if(buffer.remaining() < remaining)
			{
				// reset timeout if new bytes arrived
				respTimeoutPoint = System.currentTimeMillis() + responseTimeout;
				remaining = buffer.remaining();
			}
			else if(System.currentTimeMillis() >= respTimeoutPoint)
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
				// once again not sure what to do here. Throw exception? Continue? Just abort? All good and bad alike.
				return;
			}
		}
	}

	public static final ByteBuffer readBytes(
		final SocketChannel channel        ,
		final ByteBuffer    buffer         ,
		final int           responseTimeout,
		final long          length
	)
		throws IOException
	{
		final int limitedLength = checkArrayRange(length);

		final ByteBuffer checkedBuffer;
		if(length > buffer.capacity())
		{
			checkedBuffer = ByteBuffer.allocateDirect(limitedLength);
		}
		else
		{
			(checkedBuffer = buffer).clear().limit(limitedLength);
		}

		fillBuffer(checkedBuffer, channel, responseTimeout);
		checkedBuffer.flip();
		return checkedBuffer;
	}



	private LogicNetworkBinaryPersistence()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
