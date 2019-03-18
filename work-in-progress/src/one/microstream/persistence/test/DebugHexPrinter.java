package one.microstream.persistence.test;

import static one.microstream.X.notNull;
import static one.microstream.chars.VarString.New;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@SuppressWarnings("deprecation")
public class DebugHexPrinter extends FileChannel
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final PrintStream out;
	private final FileChannel relayTarget;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public DebugHexPrinter(final PrintStream out)
	{
		super();
		this.out         = notNull(out);
		this.relayTarget = null;
	}

	public DebugHexPrinter(final PrintStream out, final FileChannel relayTarget)
	{
		super();
		this.out         = notNull(out);
		this.relayTarget = relayTarget;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public int write(final ByteBuffer src) throws IOException
	{
		// (04.09.2012 TM)FIXME: write (-> null)
		final byte[] bytes = new byte[src.limit()];
		src.get(bytes);

		this.out.println(
			DEBUG_BinaryPersistence.format8ByteWise(0,
				New().addHexDec(bytes).toString()
			)
		);
		src.flip();
		if(this.relayTarget != null)
		{
			return this.relayTarget.write(src);
		}
		return 0;
	}

	@Override
	public long write(final ByteBuffer[] srcs, final int offset, final int length) throws IOException
	{
		if(this.relayTarget != null)
		{
			return this.relayTarget.write(srcs, offset, length);
		}
		return 0L;
	}

	@Override
	protected void implCloseChannel() throws IOException
	{
//		this.relayTarget.implCloseChannel(); // you guys drive my crazy
		this.relayTarget.close(); // hope this works instead
	}

	@Override
	public int read(final ByteBuffer dst) throws IOException
	{
		return this.relayTarget.read(dst);
	}

	@Override
	public long read(final ByteBuffer[] dsts, final int offset, final int length) throws IOException
	{
		return this.relayTarget.read(dsts, offset, length);
	}

	@Override
	public long position() throws IOException
	{
		return this.relayTarget.position();
	}

	@Override
	public FileChannel position(final long newPosition) throws IOException
	{
		return this.relayTarget.position(newPosition);
	}

	@Override
	public long size() throws IOException
	{
		return this.relayTarget.size();
	}

	@Override
	public FileChannel truncate(final long size) throws IOException
	{
		return this.relayTarget.truncate(size);
	}

	@Override
	public void force(final boolean metaData) throws IOException
	{
		this.relayTarget.force(metaData);
	}

	@Override
	public long transferTo(final long position, final long count, final WritableByteChannel target) throws IOException
	{
		return this.relayTarget.transferTo(position, count, target);
	}

	@Override
	public long transferFrom(final ReadableByteChannel src, final long position, final long count) throws IOException
	{
		return this.relayTarget.transferFrom(src, position, count);
	}

	@Override
	public int read(final ByteBuffer dst, final long position) throws IOException
	{
		return this.relayTarget.read(dst, position);
	}

	@Override
	public int write(final ByteBuffer src, final long position) throws IOException
	{
		return this.relayTarget.write(src, position);
	}

	@Override
	public MappedByteBuffer map(final MapMode mode, final long position, final long size) throws IOException
	{
		return this.relayTarget.map(mode, position, size);
	}

	@Override
	public FileLock lock(final long position, final long size, final boolean shared) throws IOException
	{
		return this.relayTarget.lock(position, size, shared);
	}

	@Override
	public FileLock tryLock(final long position, final long size, final boolean shared) throws IOException
	{
		return this.relayTarget.tryLock();
	}

}
