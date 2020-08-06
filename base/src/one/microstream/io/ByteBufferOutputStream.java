package one.microstream.io;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class ByteBufferOutputStream extends OutputStream
{
	public static ByteBufferOutputStream New(
		final ByteBuffer targetBuffer
	)
	{
		return new ByteBufferOutputStream(
			notNull(targetBuffer)
		);
	}


	private final ByteBuffer targetBuffer;

	private ByteBufferOutputStream(
		final ByteBuffer targetBuffer
	)
	{
		super();
		this.targetBuffer = targetBuffer;
	}

	@Override
	public void write(
		final int b
	)
	throws IOException
	{
		this.targetBuffer.put((byte)b);
	}

	@Override
	public void write(
		final byte[] bytes ,
		final int    offset,
		final int    length
	)
	throws IOException
	{
		notNull(bytes);
		if(offset < 0
		|| offset > bytes.length
		|| length < 0
		|| offset + length > bytes.length
		|| offset + length < 0)
		{
			throw new IndexOutOfBoundsException();
		}
		if(length == 0)
		{
			return;
		}

		this.targetBuffer.put(bytes, offset, length);
	}

}