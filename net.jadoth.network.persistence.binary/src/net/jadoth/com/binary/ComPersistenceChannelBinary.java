package net.jadoth.com.binary;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.com.ComException;
import net.jadoth.com.ComPersistenceChannel;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.ChunksWrapper;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.BufferSizeProvider;

public interface ComPersistenceChannelBinary extends ComPersistenceChannel<SocketChannel, Binary>
{
	public static ComPersistenceChannelBinary New(
		final SocketChannel      channel           ,
		final BufferSizeProvider bufferSizeProvider
	)
	{
		return new ComPersistenceChannelBinary.Implementation(
			notNull(channel)           ,
			notNull(bufferSizeProvider)
		);
	}
	
	public final class Implementation
	extends ComPersistenceChannel.AbstractImplementation<SocketChannel, Binary>
	implements ComPersistenceChannelBinary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BufferSizeProvider bufferSizeProvider;
		private       ByteBuffer         defaultBuffer     ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final SocketChannel channel, final BufferSizeProvider bufferSizeProvider)
		{
			super(channel);
			this.bufferSizeProvider = bufferSizeProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private ByteBuffer ensureDefaultBuffer()
		{
			if(this.defaultBuffer == null)
			{
				this.defaultBuffer = ByteBuffer.allocateDirect(
					X.checkArrayRange(this.bufferSizeProvider.provideBufferSize())
				);
			}
			
			return this.defaultBuffer;
		}

		@Override
		protected XGettingCollection<? extends Binary> internalRead(final SocketChannel channel)
			throws PersistenceExceptionTransfer
		{
			final ByteBuffer defaultBuffer = this.ensureDefaultBuffer();
			
			ByteBuffer filledContentBuffer;
			try
			{
				filledContentBuffer = ComBinary.readChunk(channel, defaultBuffer);
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
			
			return X.<Binary>Constant(ChunksWrapper.New(filledContentBuffer));
		}

		@Override
		protected void internalWrite(final SocketChannel channel, final Binary[] chunks)
			throws PersistenceExceptionTransfer
		{
			if(chunks.length != 1)
			{
				/* (11.08.2018 TM)NOTE:
				 * This is a somewhat unclean API:
				 * Chunks is only an array because the Storage's channel hashing mechanism requires it.
				 * But for each channel, there is only exactely one chunk.
				 * Here, there are no channels, so it appears that multiple chunks can/could/should be sent
				 * at a time. Doing that would require another layer of meta-header: sending how many chunks
				 * there are.
				 * The clean way would probably be to nest the channel-chunks in the Binary type itself
				 * and then provide an iterateChunks Method instead of an explicit array. Or something like that.
				 * Or some specialized Binary meta type.
				 * The short-term solution is to force the chunks length to be exactely 1, here.
				 * Not pretty. The concept should be consolidated to cover both use cases nicely instead of ugly.
				 */
				throw new UnsupportedOperationException("Can only send one chunk at a time.");
			}
			final Binary chunk = chunks[0];
			
			final ByteBuffer defaultBuffer = ComBinary.setChunkHeaderContentLength(
				this.ensureDefaultBuffer(),
				chunk.totalLength()
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
		
	}
	
}
