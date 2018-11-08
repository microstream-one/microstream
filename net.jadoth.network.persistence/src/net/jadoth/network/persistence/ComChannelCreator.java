package net.jadoth.network.persistence;

import java.nio.channels.SocketChannel;

public interface ComChannelCreator<C>
	{
		public ComChannel createChannel(C socketChannel);
		
		
		
		public static  ComChannelCreator.ImplementationSocketChannel NewForSocketChannel()
		{
			return new ComChannelCreator.ImplementationSocketChannel();
		}
		
		public final class ImplementationSocketChannel implements ComChannelCreator<SocketChannel>
		{
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			ImplementationSocketChannel()
			{
				super();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public ComChannel createChannel(final SocketChannel socketChannel)
			{
				/* (01.11.2018 TM)FIXME: ComChannel.Creator#createChannel()
				 * 
				 * Code from proof-of-concept:
				 */
//				final NetworkPersistenceChannelBinary channel = NetworkPersistenceChannelBinary.New(
//					socketChannel,
//					BufferSizeProvider.New()
//				);
//
//				final BinaryPersistenceFoundation<?> foundation = createFoundation(systemDirectory, isClient);
//				foundation.setPersistenceChannel(channel);
//
//				final PersistenceManager<Binary> pm = foundation.createPersistenceManager();
//
//				return ComChannel.New(pm);
				
				throw new net.jadoth.meta.NotImplementedYetError();
			}
			
		}
		
	}