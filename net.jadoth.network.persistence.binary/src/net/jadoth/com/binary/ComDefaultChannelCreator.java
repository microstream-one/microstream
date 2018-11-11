package net.jadoth.com.binary;

import java.nio.channels.SocketChannel;

import net.jadoth.com.ComChannel;
import net.jadoth.com.ComChannelCreator;

public interface ComDefaultChannelCreator extends ComChannelCreator<SocketChannel>
{
	@Override
	public ComChannel createChannel(SocketChannel socketChannel);
	
	

	public static ComDefaultChannelCreator.Implementation New()
	{
		return new ComDefaultChannelCreator.Implementation();
	}
	
	public final class Implementation implements ComDefaultChannelCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComChannel createChannel(final SocketChannel socketChannel)
		{
			/* (01.11.2018 TM)FIXME: ComDefaultChannelCreator#createChannel()
			 * 
			 * Code from proof-of-concept:
			 */
//			final NetworkPersistenceChannelBinary channel = NetworkPersistenceChannelBinary.New(
//				socketChannel,
//				BufferSizeProvider.New()
//			);
//
//			final BinaryPersistenceFoundation<?> foundation = createFoundation(systemDirectory, isClient);
//			foundation.setPersistenceChannel(channel);
//
//			final PersistenceManager<Binary> pm = foundation.createPersistenceManager();
//
//			return ComChannel.New(pm);
			
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
}
