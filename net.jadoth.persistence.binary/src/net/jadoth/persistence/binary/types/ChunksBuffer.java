package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.memory.RawValueHandler;
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
		final RawValueHandler               rawValueHandler   ,
		final BufferSizeProviderIncremental bufferSizeProvider
	)
	{
		return new ChunksBuffer(rawValueHandler, bufferSizeProvider);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

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
		final RawValueHandler               rawValueHandler   ,
		final BufferSizeProviderIncremental bufferSizeProvider
	)
	{
		super(rawValueHandler);
		this.bufferSizeProvider = notNull(bufferSizeProvider);
		this.setCurrent((this.buffers = new ByteBuffer[DEFAULT_BUFFERS_CAPACITY])[this.currentBuffersIndex = 0] =
			ByteBuffer.allocateDirect(X.checkArrayRange(bufferSizeProvider.provideBufferSize())))
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

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

	@Override
	public final long[] startOffsets()
	{
		if(this.currentBuffer != null)
		{
			throw new IllegalStateException("Cannot return offsets of incomplete chunks");
		}
		return this.internalGetStartOffsets();
	}

	@Override
	public final long[] boundOffsets()
	{
		if(this.currentBuffer != null)
		{
			throw new IllegalStateException("Cannot return offsets of incomplete chunks");
		}
		return this.internalGetBoundOffsets();
	}

	@Override
	protected long[] internalGetBoundOffsets()
	{
		final ByteBuffer[] buffers      = this.buffers;
		final int          buffersCount = this.currentBuffersIndex + 1;
		final long[]       boundOffsets = new long[buffersCount];

		for(int i = 0; i < buffersCount; i++)
		{
			boundOffsets[i] = XMemory.getDirectByteBufferAddress(buffers[i]) + buffers[i].limit(); // already flipped
		}
		return boundOffsets;
	}

	@Override
	protected final long[] internalGetStartOffsets()
	{
		final ByteBuffer[] buffers      = this.buffers;
		final int          buffersCount = this.currentBuffersIndex + 1;
		final long[]       startOffsets = new long[buffersCount];

		for(int i = 0; i < buffersCount; i++)
		{
			startOffsets[i] = XMemory.getDirectByteBufferAddress(buffers[i]);
		}
		return startOffsets;
	}

	@Override
	public final long buildItemAddress()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected final void internalIterateCurrentData(final Consumer<byte[]> iterator)
	{
		for(final ByteBuffer buffer : this.buffers)
		{
			if(buffer == null || buffer.position() == 0)
			{
				continue; // no data yet, only a reserved entry in the internal array
			}

			// defensive copy for debug purposes is very reasonable. Performance downside is irrelevant.
			final byte[] bytes = new byte[buffer.limit()];
			buffer.flip();
			buffer.get(bytes);

			// pass only a copy of the data, neither an actual bytebuffer nor the actual memory address
			iterator.accept(bytes);
		}

		final long currentDataAddress = XMemory.getDirectByteBufferAddress(this.currentBuffer);
		final byte[] bytes = new byte[X.checkArrayRange(this.currentAddress - currentDataAddress)];
		XMemory.copyRangeToArray(currentDataAddress, bytes);
		iterator.accept(bytes);
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
	
}
