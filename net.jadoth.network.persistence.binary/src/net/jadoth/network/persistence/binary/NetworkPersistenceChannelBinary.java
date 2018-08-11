package net.jadoth.network.persistence.binary;

import static net.jadoth.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.memory.Memory;
import net.jadoth.network.persistence.NetworkPersistenceChannel;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.ChunksWrapper;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.BufferSizeProvider;

public interface NetworkPersistenceChannelBinary extends NetworkPersistenceChannel<Binary>
{
	public static <M> NetworkPersistenceChannelBinary New(
		final SocketChannel      channel           ,
		final BufferSizeProvider bufferSizeProvider
	)
	{
		return new NetworkPersistenceChannelBinary.Implementation(
			notNull(channel)           ,
			notNull(bufferSizeProvider)
		);
	}
	
	public final class Implementation
	extends NetworkPersistenceChannel.AbstractImplementation<Binary>
	implements NetworkPersistenceChannelBinary
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		
		/* (10.08.2018 TM)TODO: Better network timeout handling
		 * The simplistic int value should be replaced by a NetworkTimeoutEvaluator.
		 * Every time a read event leaves the target buffer with remaining bytes,
		 * the evaluator is called with the following arguments:
		 * - time instant when the filling of the buffer started
		 * - total amount of required bytes
		 * - timestamp of the last time bytes were received
		 * - amount of received bytes (or buffer remaining bytes or something like that)
		 * 
		 * This allows arbitrarily complex evaluation logic.
		 * For example:
		 * - abort if the transfer speed (bytes/s) drops too low, even though there are still bytes coming in.
		 * - abort if the whole process takes too long.
		 * - abort if the time with 0 bytes received gets too long.
		 * 
		 */
		static final int RESPONSE_TIMEOUT = 1000;
		
		
		
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
					X.checkArrayRange(this.bufferSizeProvider.initialBufferSize())
				);
			}
			
			return this.defaultBuffer;
		}

		@Override
		protected XGettingCollection<? extends Binary> readFromSocketChannel(final SocketChannel channel)
			throws PersistenceExceptionTransfer
		{
			final ByteBuffer defaultBuffer = this.ensureDefaultBuffer();
			
			ByteBuffer filledHeaderBuffer;
			ByteBuffer filledContentBuffer;
			try
			{
				filledHeaderBuffer = NetworkPersistenceBinary.readIntoBufferKnownLength(
					channel,
					defaultBuffer,
					RESPONSE_TIMEOUT,
					NetworkPersistenceBinary.networkChunkHeaderLength()
				);
				
				final long networkChunkContentLength = NetworkPersistenceBinary.readNetworkChunkContentLength(
					Memory.getDirectByteBufferAddress(filledHeaderBuffer)
				);
				
				filledContentBuffer = NetworkPersistenceBinary.readIntoBufferKnownLength(
					channel,
					defaultBuffer,
					RESPONSE_TIMEOUT,
					X.checkArrayRange(networkChunkContentLength)
				);
			}
			catch (final IOException e)
			{
				throw new PersistenceExceptionTransfer(e);
			}
			
			return X.<Binary>Constant(ChunksWrapper.New(filledContentBuffer));
		}

		@Override
		protected void writeToSocketChannel(
			final SocketChannel channel,
			final Binary[]      chunks
		)
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
			
			final ByteBuffer defaultBuffer = this.ensureDefaultBuffer();
			defaultBuffer.clear();
			
			// (11.08.2018 TM)FIXME: fill default buffer with chunk total length
			
			try
			{
				// the chunk header is sent first, the actual chunk data afterwards
				NetworkPersistenceBinary.writeFromBuffer(channel, defaultBuffer, RESPONSE_TIMEOUT);
				
				for(final Binary chunk : chunks)
				{
					for(final ByteBuffer bb : chunk.buffers())
					{
						NetworkPersistenceBinary.writeFromBuffer(channel, bb, RESPONSE_TIMEOUT);
					}
				}
			}
			catch(final IOException e)
			{
				throw new PersistenceExceptionTransfer(e);
			}
			
		}
		
	}
	
}
