package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;

import net.jadoth.X;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.exceptions.BinaryPersistenceExceptionStateInvalidLength;
import net.jadoth.util.BufferSizeProviderIncremental;


public final class ChunksBuffer extends Binary implements MemoryRangeReader
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final int DEFAULT_BUFFERS_CAPACITY = Long.BYTES;



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	public static final ChunksBuffer New(
		final ChunksBuffer[]                channelBuffers    ,
		final BufferSizeProviderIncremental bufferSizeProvider
	)
	{
		return new ChunksBuffer(
			notNull(channelBuffers),
			notNull(bufferSizeProvider)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final ChunksBuffer[]                channelBuffers    ;
	private final BufferSizeProviderIncremental bufferSizeProvider;
	
	private ByteBuffer[] buffers            ;
	private int          currentBuffersIndex;
	private ByteBuffer   currentBuffer      ;
	private long         currentAddress     ;
	private long         currentBound       ;
	private long         totalLength        ;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	ChunksBuffer(
		final ChunksBuffer[]                channelBuffers    ,
		final BufferSizeProviderIncremental bufferSizeProvider
	)
	{
		super();
		this.channelBuffers     = channelBuffers;
		this.bufferSizeProvider = bufferSizeProvider;
		this.setCurrent((this.buffers = new ByteBuffer[DEFAULT_BUFFERS_CAPACITY])[this.currentBuffersIndex = 0] =
			ByteBuffer.allocateDirect(X.checkArrayRange(bufferSizeProvider.provideBufferSize())))
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Binary channelChunk(final int channelIndex)
	{
		return this.channelBuffers[channelIndex];
	}
	
	@Override
	public final int channelCount()
	{
		return this.channelBuffers.length;
	}

	private void setCurrent(final ByteBuffer byteBuffer)
	{
		this.currentBound = (this.currentAddress = XMemory.getDirectByteBufferAddress(this.currentBuffer = byteBuffer))
			+ byteBuffer.capacity()
		;
		byteBuffer.clear();
	}
	
	private void updateCurrentBufferPosition()
	{
		final long contentLength = this.currentAddress - XMemory.getDirectByteBufferAddress(this.currentBuffer);
		
		this.currentBuffer.position(X.checkArrayRange(contentLength)).flip();
		
		this.totalLength += contentLength;
	}

	private boolean isEmptyCurrentBuffer()
	{
		return this.currentAddress == XMemory.getDirectByteBufferAddress(this.currentBuffer);
	}

	private void enlargeBufferCapacity(final int bufferCapacity)
	{
		// if current buffer is still empty, replace it instead of enqueing a new one to avoid storing "dummy" chunks
		if(this.isEmptyCurrentBuffer())
		{
			XMemory.deallocateDirectByteBuffer(this.currentBuffer);
			this.allocateNewCurrent(bufferCapacity);
			return;
		}
		this.updateCurrentBufferPosition();
		this.addBuffer(bufferCapacity);
	}

	private int calculateNewBufferCapacity(final long requiredCapacity)
	{
		final long defaultBufferCapacity = this.bufferSizeProvider.provideIncrementalBufferSize();
		
		// never allocate less than the default, but more if needed.
		return X.checkArrayRange(Math.max(requiredCapacity, defaultBufferCapacity));
	}

	private void ensureFreeStoreCapacity(final long requiredCapacity)
	{
		if(this.currentAddress + requiredCapacity > this.currentBound)
		{
			this.enlargeBufferCapacity(this.calculateNewBufferCapacity(requiredCapacity));
		}
	}

	private void incrementBuffersCount()
	{
		if(++this.currentBuffersIndex >= this.buffers.length)
		{
			// shifting overflow is ignored because it is highly unlikely to ever reach 1 billion buffers ^^
			System.arraycopy(
				this.buffers,
				0,
				this.buffers = new ByteBuffer[this.buffers.length << 1],
				0,
				this.currentBuffersIndex
			);
		}
	}

	private void addBuffer(final int bufferCapacity)
	{
		this.incrementBuffersCount();
		this.allocateNewCurrent(bufferCapacity);
	}

	private void allocateNewCurrent(final int bufferCapacity)
	{
		this.setCurrent(this.buffers[this.currentBuffersIndex] = ByteBuffer.allocateDirect(bufferCapacity));
	}

	@Override
	public final void clear()
	{
		final ByteBuffer[] buffers = this.buffers;
		for(int i = this.currentBuffersIndex; i >= 1; i--)
		{
			XMemory.deallocateDirectByteBuffer(buffers[i]);
			buffers[i] = null;
		}
		this.setCurrent(buffers[this.currentBuffersIndex = 0]);
	}

	/**
	 * It is completely the caller's responsibility that the passed array contains
	 * a valid [LEN][TID][OID][data] byte sequence.
	 *
	 */
	@Override
	public void readMemory(final long address, final long length)
	{
		this.ensureFreeStoreCapacity(length);
		XMemory.copyRange(address, this.currentAddress, length);
		this.currentAddress += length;
	}

	@Override
	public final long storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	)
	{
//		debugln("Storing\t" + entityObjectId + "\t" + entityTypeId + "\t" + BinaryPersistence.entityTotalLength(entityContentLength));

		if(entityContentLength < 0)
		{
			// length has to be checked to avoid messing up the current address.
			throw new BinaryPersistenceExceptionStateInvalidLength(
				this.currentAddress, entityContentLength, entityTypeId, entityObjectId
			);
		}
		this.ensureFreeStoreCapacity(BinaryPersistence.entityTotalLength(entityContentLength));

		/*
		 * static methods returns entity bound address for updating this current address,
		 * but content address has to be returned, so the content length has to be subtracted again
		 */
		return (this.currentAddress = BinaryPersistence.storeEntityHeader(
			this.currentAddress,
			entityContentLength,
			entityTypeId,
			entityObjectId
		)) - entityContentLength
		;
	}

	@Override
	public final ByteBuffer[] buffers()
	{
		if(this.currentBuffer != null)
		{
			throw new IllegalStateException("Cannot return buffers of incomplete chunks");
		}

		// copy tiny array in any case to a) have no trailing nulls and b) to keep actual array hidden
		final ByteBuffer[] buffers;
		System.arraycopy(
			this.buffers,
			0,
			buffers = new ByteBuffer[this.currentBuffersIndex + 1],
			0,
			buffers.length
		);
		return buffers;
	}

	public final ChunksBuffer complete()
	{
		if(this.currentBuffer == null)
		{
			return this; // already completed
		}
		this.updateCurrentBufferPosition();
		this.currentBuffer  = null;
		this.currentAddress = 0L;
		this.currentBound   = 0L;
		return this;
	}
	
	
	private void iterateEntityDataLocal(final BinaryEntityDataReader reader)
	{
		if(this.currentBuffer != null)
		{
			throw new IllegalStateException("Incomplete chunks");
		}

		final ByteBuffer[] buffers = this.buffers;
		final int     buffersCount = this.currentBuffersIndex + 1;
				
		for(int i = 0; i < buffersCount; i++)
		{
			// buffer is already flipped
			iterateBufferLoadItems(
				XMemory.getDirectByteBufferAddress(buffers[i]),
				XMemory.getDirectByteBufferAddress(buffers[i]) + buffers[i].limit(),
				reader
			);
		}
	}
	
	@Override
	public void iterateEntityData(final BinaryEntityDataReader reader)
	{
		for(final ChunksBuffer channelBuffer : this.channelBuffers)
		{
			channelBuffer.iterateEntityDataLocal(reader);
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
	public final boolean isEmpty()
	{
		return this.buffers[0] == null;
	}

	@Override
	public final long totalLength()
	{
		return this.totalLength;
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
	public final void iterateKeyValueEntriesReferences(
		final long           offset  ,
		final _longProcedure iterator
	)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final long getListElementCountKeyValue(final long listStartOffset)
	{
		throw new UnsupportedOperationException();
	}
	
}
