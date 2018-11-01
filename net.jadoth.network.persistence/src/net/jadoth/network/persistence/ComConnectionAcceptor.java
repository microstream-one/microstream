package net.jadoth.network.persistence;

import static net.jadoth.X.notNull;

import java.nio.channels.SocketChannel;


/**
 * Logic to greet/authenticate the client, exchange metadata, create a {@link ComChannel} instance.
 * Potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
public interface ComConnectionAcceptor
{
	public ComConfiguration configuration();
	
	public void acceptConnection(SocketChannel socketChannel);
	
	
	
	public static Creator Creator()
	{
		return new Creator.Implementation();
	}
	
	public interface Creator
	{
		public ComConnectionAcceptor createConnectionAcceptor(
			ComConfiguration   configuration  ,
			ComChannel.Creator channelCreator ,
			ComChannelAcceptor channelAcceptor
		);
		
		public final class Implementation implements ComConnectionAcceptor.Creator
		{
			Implementation()
			{
				super();
			}

			@Override
			public ComConnectionAcceptor createConnectionAcceptor(
				final ComConfiguration   configuration  ,
				final ComChannel.Creator channelCreator ,
				final ComChannelAcceptor channelAcceptor
			)
			{
				return New(configuration, channelCreator, channelAcceptor);
			}
			
		}
		
	}
	
	
	
	public static ComConnectionAcceptor New(
		final ComConfiguration   configuration  ,
		final ComChannel.Creator channelCreator ,
		final ComChannelAcceptor channelAcceptor
	)
	{
		return new ComConnectionAcceptor.Implementation(
			notNull(configuration),
			notNull(channelCreator),
			notNull(channelAcceptor)
		);
	}
	
	public final class Implementation implements ComConnectionAcceptor
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComConfiguration   configuration  ;
		private final ComChannel.Creator channelCreator ;
		private final ComChannelAcceptor channelAcceptor;
		
		// (01.11.2018 TM)TODO: JET-43: cache UTF-8 bytes for configuration
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final ComConfiguration   configuration  ,
			final ComChannel.Creator channelCreator ,
			final ComChannelAcceptor channelAcceptor
		)
		{
			super();
			this.configuration   = configuration  ;
			this.channelCreator  = channelCreator ;
			this.channelAcceptor = channelAcceptor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ComConfiguration configuration()
		{
			return this.configuration;
		}
		
		@Override
		public void acceptConnection(final SocketChannel socketChannel)
		{
			/* (01.11.2018 TM)TODO: JET-43
			 *  - send (cached) configuration to peer.
			 *  ? recognize closed channel
			 *  - create comChannel instance
			 *  - pass to channel acceptor
			 */
		}
		
	}
	
}
