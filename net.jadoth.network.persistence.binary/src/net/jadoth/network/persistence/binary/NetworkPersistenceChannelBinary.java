package net.jadoth.network.persistence.binary;

import static net.jadoth.X.notNull;

import java.nio.channels.SocketChannel;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.network.persistence.NetworkPersistenceChannel;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;

public interface NetworkPersistenceChannelBinary extends NetworkPersistenceChannel<Binary>
{
	public static <M> NetworkPersistenceChannelBinary New(final SocketChannel channel)
	{
		return new NetworkPersistenceChannelBinary.Implementation(
			notNull(channel)
		);
	}
	
	public final class Implementation
	extends NetworkPersistenceChannel.AbstractImplementation<Binary>
	implements NetworkPersistenceChannelBinary
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final SocketChannel channel)
		{
			super(channel);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected XGettingCollection<? extends Binary> readFromSocketChannel(final SocketChannel channel)
			throws PersistenceExceptionTransfer
		{
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
