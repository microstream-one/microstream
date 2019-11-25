package one.microstream.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import one.microstream.X;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.util.UtilStackTrace;

public final class XIO
{
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public static void execute(final IoOperation operation) throws IORuntimeException
	{
		try
		{
			operation.execute();
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static <T> T execute(final IoOperationR<T> operation) throws IORuntimeException
	{
		try
		{
			return operation.executeR();
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static <S> void execute(final IoOperationS<S> operation, final S subject) throws IORuntimeException
	{
		try
		{
			operation.executeS(subject);
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static <S, R> R execute(final IoOperationSR<S, R> operation, final S subject) throws IORuntimeException
	{
		try
		{
			return operation.executeSR(subject);
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static final <C extends Closeable> C closeSilent(final C closable)
	{
		if(closable != null)
		{
			try
			{
				closable.close();
			}
			catch(final Exception t)
			{
				// sshhh, silence!
			}
		}
		
		return closable;
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// java.nio.channels.FileChannel //
	//////////////////////////////////
	
	public static ByteBuffer determineLastNonEmpty(final ByteBuffer[] byteBuffers)
	{
		for(int i = byteBuffers.length - 1; i >= 0; i--)
		{
			if(byteBuffers[i].hasRemaining())
			{
				return byteBuffers[i];
			}
		}
		
		// either the array is empty or only contains empty buffers. Either way, no suitable buffer found.
		return null;
	}
	
	public static final ByteBuffer wrapInDirectByteBuffer(final byte[] bytes)
		throws IOException
	{
		final ByteBuffer dbb = ByteBuffer.allocateDirect(bytes.length);
		dbb.put(bytes);
		dbb.flip();
		
		return dbb;
	}
	
	/**
	 * Sets the passed {@link FileChannel}'s position to its current length and repeatedly calls
	 * {@link FileChannel#write(ByteBuffer[])} until the last non-empty buffer has no remaining bytes.<br>
	 * This is necessary because JDK's {@link FileChannel#write(ByteBuffer[])} seems to arbitrarily stop processing
	 * the passed {@link ByteBuffer}s even though they have remaining bytes left to be written.
	 * <p>
	 * The reason for this behavior is unknown, but looking at countless other issues in the JDK code,
	 * one might guess... .
	 * 
	 * @param fileChannel
	 * @param byteBuffers
	 * @throws IOException
	 */
	public static long appendAll(final FileChannel fileChannel, final ByteBuffer[] byteBuffers)
		throws IOException
	{
		// determine last non-empty buffer to be used as a write-completion check point
		final ByteBuffer lastNonEmpty = determineLastNonEmpty(byteBuffers);
		if(lastNonEmpty == null)
		{
			return 0L;
		}
		
		final long oldLength = fileChannel.size();
		
		long writeCount = 0;
		fileChannel.position(oldLength);
		while(lastNonEmpty.hasRemaining())
		{
			// file channel position is implicitely advanced by the amount of written bytes.
			writeCount += fileChannel.write(byteBuffers);
		}
		
		return writeCount;
	}
	
	/**
	 * Calls {@link #appendAll(FileChannel, ByteBuffer[])}, then {@link FileChannel#force(boolean)}, then validates
	 * if the actual new file size is really exactely what it should be based on old file size and the amount of bytes
	 * written.<p>
	 * In short: this method "guarantees" that every byte contained in the passed {@link ByteBuffer}s was appended
	 * to the passed {@link FileChannel} and actually reached the physical file.
	 * 
	 * @param fileChannel
	 * @param byteBuffers
	 * @throws IOException
	 */
	public static long appendAllGuaranteed(final FileChannel fileChannel, final ByteBuffer[] byteBuffers)
		throws IOException
	{
		final long oldLength  = fileChannel.size();
		final long writeCount = XIO.appendAll(fileChannel, byteBuffers);
		
		// this is the right place for a data-safety-securing force/flush.
		fileChannel.force(false);
		
		final long newTotalLength = fileChannel.size();
		
		if(newTotalLength != oldLength + writeCount)
		{
			 // (01.10.2014)EXCP: proper exception
			throw new IOException(
				"Inconsistent post-write file length:"
				+ " New total length " + newTotalLength +
				" is not equal " + oldLength + " + " + writeCount + " (old length and write count)"
			);
		}
		
		return writeCount;
	}
	
	public static long writeAppending(final FileChannel fileChannel, final ByteBuffer buffer)
		throws IOException
	{
		// appending logic
		return writePositioned(fileChannel, fileChannel.size(), buffer);
	}
	
	public static long writePositioned(
		final FileChannel fileChannel ,
		final long        filePosition,
		final ByteBuffer  buffer
	)
		throws IOException
	{
		fileChannel.position(filePosition);
		
		return write(fileChannel, buffer);
	}
	
	public static long write(
		final FileChannel fileChannel,
		final ByteBuffer  buffer
	)
		throws IOException
	{
		long writeCount = 0;
		while(buffer.hasRemaining())
		{
			writeCount += fileChannel.write(buffer);
		}
		
		return writeCount;
	}
	
	public static final <T> T performClosingOperation(
		final FileChannel                   fileChannel,
		final IoOperationSR<FileChannel, T> operation
	)
		throws IOException
	{
		try
		{
			return operation.executeSR(fileChannel);
		}
		finally
		{
			fileChannel.close();
		}
	}
		
	public static ByteBuffer readFile(final FileChannel fileChannel)
		throws IOException
	{
		return readFile(fileChannel, 0, fileChannel.size());
	}
	
	public static ByteBuffer readFile(
		final FileChannel fileChannel,
		final long        filePosition,
		final long        length
	)
		throws IOException
	{
		// always hilarious to see that a low-level IO-tool has a int size limitation. Geniuses.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(X.checkArrayRange(length));
		
		readFile(fileChannel, dbb, filePosition, dbb.limit());
		
		dbb.flip();
		
		return dbb;
	}
	
	public static long readFile(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer
	)
		throws IOException
	{
		return readFile(fileChannel, targetBuffer, 0, fileChannel.size());
	}
		
	public static long readFile(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer,
		final long        filePosition,
		final long        length
	)
		throws IOException
	{
		if(targetBuffer.remaining() < length)
		{
			// (20.11.2019 TM)EXCP: proper exception
			throw new IllegalArgumentException(
				"Provided target buffer has not enough space remaining to load the file content: "
				+ targetBuffer.remaining() + " < " + length
			);
		}

		final int  targetLimit = X.checkArrayRange(targetBuffer.position() + length);
		final long fileLength  = fileChannel.size();
		
		long fileOffset = X.validateRange(fileLength, filePosition, length);
		targetBuffer.limit(targetLimit);
		
		// reading should be done in one fell swoop, but better be sure
		long readCount = 0;
		while(targetBuffer.hasRemaining())
		{
			readCount += fileChannel.read(targetBuffer, fileOffset);
			fileOffset = filePosition + readCount;
		}

		return readCount;
	}
	
	public static final long sizeUnchecked(final FileChannel fileChannel) throws IORuntimeException
	{
		try
		{
			return fileChannel.size();
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
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
	private XIO()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
