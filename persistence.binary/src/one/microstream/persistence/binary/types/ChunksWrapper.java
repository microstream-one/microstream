package one.microstream.persistence.binary.types;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import one.microstream.memory.JdkInternals;


public class ChunksWrapper extends Binary
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ChunksWrapper New(final ByteBuffer... chunkDirectBuffers)
	{
		return new ChunksWrapper(chunkDirectBuffers);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ByteBuffer[] buffers    ;
	private final long         totalLength;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	// internal constructor. Does not validate arguments!
	protected ChunksWrapper(final ByteBuffer[] chunks)
	{
		super();
		
		long totalLength = 0;
		for(int i = 0; i < chunks.length; i++)
		{
			if(!JdkInternals.isDirectBuffer(chunks[i]))
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
			this.iterateBufferLoadItems(
				JdkInternals.getDirectBufferAddress(buffers[i]),
				JdkInternals.getDirectBufferAddress(buffers[i]) + buffers[i].position(),
				reader
			);
		}
	}
	
	@Override
	public void iterateChannelChunks(final Consumer<? super Binary> logic)
	{
		logic.accept(this);
	}
		
	private void iterateBufferLoadItems(
		final long                   startAddress,
		final long                   boundAddress,
		final BinaryEntityDataReader reader
	)
	{
		// the start of an entity always contains its length. Loading chunks do not contain gaps (negative length)
		for(long address = startAddress; address < boundAddress; address += this.read_long(address))
		{
//			XDebug.println(
//				"Current entity to be read :@" + address + ": ["
//					+ this.read_long(address) + "]["
//					+ this.read_long(address + 8) + "]["
//					+ this.read_long(address + 16) + "]"
//			);
			reader.readBinaryEntityData(address);
		}
	}
	
	@Override
	public final Binary channelChunk(final int channelIndex)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final int channelCount()
	{
		throw new UnsupportedOperationException();
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
	public final long loadItemEntityAddress()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final void modifyLoadItem(
		final long entityContentAddress,
		final long entityTotalLength   ,
		final long entityTypeId        ,
		final long entityObjectId
	)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final void clear()
	{
		throw new UnsupportedOperationException();
	}
			
}
