package net.jadoth.persistence.binary.internal;

import static net.jadoth.X.notNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import net.jadoth.X;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.Constant;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.ChunksWrapper;
import net.jadoth.persistence.binary.types.MessageWaiter;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.swizzling.types.SwizzleIdSet;


public class BinaryFileSource implements PersistenceSource<Binary>, MessageWaiter
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final int INITIAL_BUFFER_SIZE = 1_048_576; // or "1 << 20" or 2^20. 1 MB should be a good init size



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final File       file           ;
	private final ByteBuffer chunkDataBuffer = ByteBuffer.allocateDirect(INITIAL_BUFFER_SIZE);



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryFileSource(final File file)
	{
		super();
		this.file = notNull(file);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private ByteBuffer readChunk(final ReadableByteChannel channel, final long chunkTotalLength)
	throws IOException
	{
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(X.checkArrayRange(chunkTotalLength));
//		BinaryPersistence.setChunkTotalLength(byteBuffer);
//		byteBuffer.position(8);
		BinaryPersistence.fillBuffer(byteBuffer, channel, this); // only one buffer per chunk in simple implementation
		return byteBuffer;
	}

	private Constant<Binary> read(final long fileLength, final ReadableByteChannel channel)
	throws IOException
	{
		final BulkList<ByteBuffer> chunks = new BulkList<>();
		for(long readCount = 0, chunkTotalLength = 0; readCount < fileLength; readCount += chunkTotalLength)
		{
			chunkTotalLength = BinaryPersistence.readChunkLength(this.chunkDataBuffer, channel, this);
			chunks.add(this.readChunk(channel, chunkTotalLength));
		}
		return X.<Binary>Constant(ChunksWrapper.New(chunks.toArray(ByteBuffer.class)));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XGettingCollection<? extends Binary> read() throws PersistenceExceptionTransfer
	{
		/* Instantiation detour should still be faster than the weird Set instantiating FileChannel.open()
		 */
		try(final FileInputStream fis = new FileInputStream(this.file); final FileChannel fch = fis.getChannel())
		{
			/* How very clever of those geniuses to make all the ByteBuffer constructors
			 * package private and thus lock out everyone who needs to implement a tailored
			 * efficient implementation (e.g. one with only exactly one long field in it like here).
			 * That stupidity combined with their usual horrible hacker code style is already enough
			 * to damage the faith in the professionalism of that "new" IO stuff. Hurray.
			 */
			return this.read(this.file.length(), fch);
		}
		catch(final Exception t)
		{
			throw new PersistenceExceptionTransfer(t);
		}
	}

	@Override
	public XGettingCollection<? extends Binary> readByObjectIds(final SwizzleIdSet[] oids)
		throws PersistenceExceptionTransfer
	{
		// simple input file reading implementation can't do complex queries
		throw new UnsupportedOperationException();
	}

	@Override
	public void waitForBytes(final int readCount)
	{
		// do nothing in simple local file reading implementation
	}

}
