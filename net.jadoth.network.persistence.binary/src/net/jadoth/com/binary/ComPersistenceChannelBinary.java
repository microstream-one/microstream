package net.jadoth.com.binary;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.com.ComException;
import net.jadoth.com.ComPersistenceChannel;
import net.jadoth.com.XSockets;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.ChunksWrapper;
import net.jadoth.persistence.binary.types.ChunksWrapperByteReversing;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.ByteOrderTargeting;
import net.jadoth.util.BufferSizeProvider;

public interface ComPersistenceChannelBinary<C> extends ComPersistenceChannel<C, Binary>
{
	public static ComPersistenceChannelBinary.Default New(
		final SocketChannel               channel           ,
		final BufferSizeProvider          bufferSizeProvider,
		final ByteOrderTargeting<?> byteOrderTargeting
	)
	{
		return new ComPersistenceChannelBinary.Default(
			notNull(channel)           ,
			notNull(bufferSizeProvider),
			        byteOrderTargeting
		);
	}
	
	public abstract class Abstract<C>
	extends ComPersistenceChannel.Abstract<C, Binary>
	implements ComPersistenceChannelBinary<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BufferSizeProvider bufferSizeProvider;
		private       ByteBuffer         defaultBuffer     ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Abstract(final C channel, final BufferSizeProvider bufferSizeProvider)
		{
			super(channel);
			this.bufferSizeProvider = bufferSizeProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected ByteBuffer ensureDefaultBuffer()
		{
			if(this.defaultBuffer == null)
			{
				this.defaultBuffer = ByteBuffer.allocateDirect(
					X.checkArrayRange(this.bufferSizeProvider.provideBufferSize())
				);
			}
			
			return this.defaultBuffer;
		}
		
	}
	

	
	public final class Default extends ComPersistenceChannelBinary.Abstract<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final ByteOrderTargeting<?> byteOrderTargeting;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final SocketChannel               channel           ,
			final BufferSizeProvider          bufferSizeProvider,
			final ByteOrderTargeting<?> byteOrderTargeting
		)
		{
			super(channel, bufferSizeProvider);
			this.byteOrderTargeting = byteOrderTargeting;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		///////////
		
		@Override
		protected XGettingCollection<? extends Binary> internalRead(final SocketChannel channel)
			throws PersistenceExceptionTransfer
		{
			final ByteBuffer defaultBuffer = this.ensureDefaultBuffer();
			
			ByteBuffer filledContentBuffer;
			try
			{
				filledContentBuffer = ComBinary.readChunk(
					channel,
					defaultBuffer,
					this.switchByteOrder()
				);
			}
			catch(final ComException e)
			{
				/* (13.11.2018 TM)TODO: shouldn't the content bytes be siphoned off, here?
				 * But what if the content was incomplete (or the specified content length too long) and
				 * the sockets already reads into the next chunk?
				 * Network communication can encounter all kinds of problems and it is not clear
				 * what guarantees the nio and underlying layers already make.
				 */
				throw new PersistenceExceptionTransfer(e);
			}
			
			return X.<Binary>Constant(this.createChunksWrapper(filledContentBuffer));
		}
		
		private boolean switchByteOrder()
		{
			return this.byteOrderTargeting.isByteOrderMismatch();
		}
		
		private ChunksWrapper createChunksWrapper(final ByteBuffer... byteBuffers)
		{
			return this.switchByteOrder()
				? ChunksWrapper.New(byteBuffers)
				: ChunksWrapperByteReversing.New(byteBuffers)
			;
		}

		@Override
		protected void internalWrite(final SocketChannel channel, final Binary chunk)
			throws PersistenceExceptionTransfer
		{
			final ByteBuffer defaultBuffer = ComBinary.setChunkHeaderContentLength(
				this.ensureDefaultBuffer(),
				chunk.totalLength(),
				this.switchByteOrder()
			);
			
			try
			{
				ComBinary.writeChunk(channel, defaultBuffer, chunk.buffers());
			}
			catch(final ComException e)
			{
				throw new PersistenceExceptionTransfer(e);
			}
			
		}
		
		private final void close()
		{
			XSockets.closeChannel(this.getConnection());
		}
		
		@Override
		public void prepareSource()
		{
			// nothing to prepare when using a SocketChannel
		}
		
		@Override
		public void prepareTarget()
		{
			// nothing to prepare when using a SocketChannel
		}
		
		@Override
		public void closeSource()
		{
			// SocketChannel#close is idempotent
			this.close();
		}
		
		@Override
		public void closeTarget()
		{
			// SocketChannel#close is idempotent
			this.close();
		}
		
	}
	
}
