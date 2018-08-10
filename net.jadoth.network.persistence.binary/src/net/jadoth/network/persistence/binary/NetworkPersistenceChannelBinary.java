package net.jadoth.network.persistence.binary;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.X;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.network.persistence.NetworkPersistenceChannel;
import net.jadoth.persistence.binary.types.Binary;
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
		// instance fields //
		////////////////////
		
		private final BufferSizeProvider bufferSizeProvider;
		private       ByteBuffer         initialBuffer     ;
		
		
		
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
		
		private ByteBuffer ensureInitialBuffer()
		{
			if(this.initialBuffer == null)
			{
				this.initialBuffer = ByteBuffer.allocateDirect(
					X.checkArrayRange(this.bufferSizeProvider.initialBufferSize())
				);
			}
			return this.initialBuffer;
		}

		@Override
		protected XGettingCollection<? extends Binary> readFromSocketChannel(final SocketChannel channel)
			throws PersistenceExceptionTransfer
		{
			final ByteBuffer           currentBuffer = this.ensureInitialBuffer();
			final BulkList<ByteBuffer> chunks        = new BulkList<>();
			
			
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME NetworkPersistenceChannel.AbstractImplementation<Binary>#readFromSocketChannel()
		}

		@Override
		protected XGettingCollection<? extends Binary> writeToSocketChannel(
			final SocketChannel channel,
			final Binary[]      data
		)
			throws PersistenceExceptionTransfer
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME NetworkPersistenceChannel.AbstractImplementation<Binary>#writeToSocketChannel()
		}
		
	}
}
