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

	private final ByteBuffer[] buffers     ;
	private final long[]       startOffsets;
	private final long[]       boundOffsets;
	private final long         totalLength ;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	// private constructor. Does not validate arguments!
	private ChunksWrapper(final ByteBuffer[] chunks)
	{
		super();
		final long[] startOffsets = new long[chunks.length];
		final long[] boundOffsets = new long[chunks.length];
		
		long totalLength = 0;
		for(int i = 0; i < chunks.length; i++)
		{
			if(!(chunks[i] instanceof DirectBuffer))
			{
				throw new IllegalArgumentException();
			}
//			startOffsets[i] = BinaryPersistence.chunkDataAddress(chunks[i]);
//			boundOffsets[i] = Memory.directByteBufferAddress(chunks[i]) + chunks[i].position();

			boundOffsets[i] = (startOffsets[i] = XMemory.getDirectByteBufferAddress(chunks[i])) + chunks[i].position();
			totalLength += chunks[i].position();
		}

		this.buffers      = chunks      ;
		this.startOffsets = startOffsets;
		this.boundOffsets = boundOffsets;
		this.totalLength  = totalLength ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final ByteBuffer[] buffers()
	{
		return this.buffers;
	}

	@Override
	public final long[] startOffsets()
	{
		return this.startOffsets;
	}

	@Override
	public final long[] boundOffsets()
	{
		return this.boundOffsets;
	}

	@Override
	protected final long[] internalGetStartOffsets()
	{
		return this.startOffsets();
	}

	@Override
	protected final long[] internalGetBoundOffsets()
	{
		return this.boundOffsets();
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
	public final long entityContentAddress()
	{
		// optimization inheritance artifact: only single build item implementation has a build item address
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
