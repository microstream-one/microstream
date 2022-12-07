package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceExceptionStateInvalidLength;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.util.BufferSizeProviderIncremental;


public class ChunksBuffer extends Binary implements MemoryRangeReader
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int DEFAULT_BUFFERS_CAPACITY = Long.BYTES;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ChunksBuffer New(
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
	// instance fields //
	////////////////////

	private final ChunksBuffer[]                channelBuffers    ;
	private final BufferSizeProviderIncremental bufferSizeProvider;
	
	private ByteBuffer[] buffers                  ;
	private int          currentBuffersIndex      ;
	private ByteBuffer   currentBuffer            ;
	private long         currentBufferStartAddress;
	private long         currentAddress           ;
	private long         currentBound             ;
	private long         totalLength              ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ChunksBuffer(
		final ChunksBuffer[]                channelBuffers    ,
		final BufferSizeProviderIncremental bufferSizeProvider
	)
	{
		super();
		this.channelBuffers     = channelBuffers;
		this.bufferSizeProvider = bufferSizeProvider;
		this.setCurrent((this.buffers = new ByteBuffer[DEFAULT_BUFFERS_CAPACITY])[this.currentBuffersIndex = 0] =
			XMemory.allocateDirectNative(bufferSizeProvider.provideBufferSize()))
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
		this.currentBufferStartAddress = XMemory.getDirectByteBufferAddress(this.currentBuffer = byteBuffer);
		this.currentBound = (this.currentAddress = this.currentBufferStartAddress) + byteBuffer.capacity();
		byteBuffer.clear();
	}
	
	private void updateCurrentBufferPosition()
	{
		final long contentLength = this.currentAddress - this.currentBufferStartAddress;
		
		this.currentBuffer.position(X.checkArrayRange(contentLength)).flip();
		
		this.totalLength += contentLength;
	}

	private boolean isEmptyCurrentBuffer()
	{
		return this.currentAddress == this.currentBufferStartAddress;
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
		this.setCurrent(this.buffers[this.currentBuffersIndex] = XMemory.allocateDirectNative(bufferCapacity));
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
	public final void storeEntityHeader(
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
		
		final long entityTotalLength = Binary.entityTotalLength(entityContentLength);
		this.ensureFreeStoreCapacity(entityTotalLength);
		
		this.storeEntityHeaderToAddress(this.currentAddress, entityTotalLength, entityTypeId, entityObjectId);
				
		// currentAddress is advanced to next entity, but this entity's content address has to be returned
		this.address = (this.currentAddress += entityTotalLength) - entityContentLength;
	}

	@Override
	public final ByteBuffer[] buffers()
	{
		if(this.currentBuffer != null)
		{
			throw new IllegalStateException("Cannot return buffers of incomplete chunks");
		}

		// copy tiny array in any case to a) have no trailing nulls and b) to keep actual array hidden
		final ByteBuffer[] buffers = new ByteBuffer[this.currentBuffersIndex + 1];
		System.arraycopy(this.buffers, 0, buffers, 0, buffers.length);
		
		return buffers;
	}

	public final ChunksBuffer complete()
	{
		if(this.currentBuffer == null)
		{
			return this; // already completed
		}
		
		this.updateCurrentBufferPosition();
		this.currentBuffer             = null;
		this.currentBufferStartAddress =   0L;
		this.currentAddress            =   0L;
		this.address                   =   0L;
		this.currentBound              =   0L;
		
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
			reader.readBinaryEntities(buffers[i]);
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

	@Override
	public void iterateChannelChunks(final Consumer<? super Binary> logic)
	{
		for(final ChunksBuffer channelBuffer : this.channelBuffers)
		{
			logic.accept(channelBuffer);
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
	public final void modifyLoadItem(
		final ByteBuffer directByteBuffer ,
		final long       offset           ,
		final long       entityTotalLength,
		final long       entityTypeId     ,
		final long       entityObjectId
	)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long iterateReferences(
		final BinaryReferenceTraverser[]  traversers,
		final PersistenceObjectIdAcceptor acceptor
	)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void mark()
	{
		for(int i = 0; i <= this.currentBuffersIndex; i++)
		{
			this.buffers[i].mark();
		}
	}
	
	@Override
	public void reset()
	{
		for(int i = 0; i <= this.currentBuffersIndex; i++)
		{
			this.buffers[i].reset();
		}
	}
				
}
