package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.jadoth.memory.XMemory;
//CHECKSTYLE.OFF: IllegalImport: low-level system tools are required for high performance low-level operations
import sun.nio.ch.DirectBuffer;
//CHECKSTYLE.ON: IllegalImport


public final class ChunksWrapper extends Binary
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	public static final ChunksWrapper New(final ByteBuffer... chunkDirectBuffers)
	{
		return new ChunksWrapper(chunkDirectBuffers);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final ByteBuffer[] buffers    ;
	private final long         totalLength;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	// private constructor. Does not validate arguments!
	private ChunksWrapper(final ByteBuffer[] chunks)
	{
		super();
		
		long totalLength = 0;
		for(int i = 0; i < chunks.length; i++)
		{
			if(!(chunks[i] instanceof DirectBuffer))
			{
				throw new IllegalArgumentException();
			}
			
			totalLength += chunks[i].position();
		}

		this.buffers     = chunks      ;
		this.totalLength = totalLength ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public void iterateEntityData(final BinaryEntityDataReader reader)
	{
		final ByteBuffer[] buffers = this.buffers;
				
		for(int i = 0; i < buffers.length; i++)
		{
			iterateBufferLoadItems(
				XMemory.getDirectByteBufferAddress(buffers[i]),
				XMemory.getDirectByteBufferAddress(buffers[i]) + buffers[i].position(),
				reader
			);
		}
	}
	
	private static void iterateBufferLoadItems(
		final long                   startAddress,
		final long                   boundAddress,
		final BinaryEntityDataReader reader
	)
	{
		// the start of an entity always contains its length. Loading chunks do not contain gaps (negative length)
		for(long address = startAddress; address < boundAddress; address += XMemory.get_long(address))
		{
			reader.readBinaryEntityData(address);
		}
	}
	
	@Override
	public Chunk[] channelChunks()
	{
		// FIXME JET-49: ChunksBuffer#channelChunks()
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public final ByteBuffer[] buffers()
	{
		return this.buffers;
	}

	@Override
	public final long storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	)
	{
		// optimization inheritance artifact: only storing chunk implementation can store
		throw new UnsupportedOperationException();
	}

	@Override
	public final long loadItemEntityContentAddress()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final void setLoadItemEntityContentAddress(final long entityContentAddress)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final void clear()
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected final void internalIterateCurrentData(final Consumer<byte[]> iterator)
	{
		DEBUG_BinaryPersistence.iterateByteBuffers(this.buffers, iterator);
	}

	@Override
	public final boolean isEmpty()
	{
		return this.totalLength != 0;
	}
	
	@Override
	public final long totalLength()
	{
		return this.totalLength;
	}

}
