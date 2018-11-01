package net.jadoth.network.persistence;

import java.nio.channels.SocketChannel;

import net.jadoth.network.persistence.ComChannel.Creator;

public interface ComConnectionAcceptor
{
	public ComConfiguration configuration();
	
	public void acceptConnection(SocketChannel socketChannel);
	
	
	public final class Implementation implements ComConnectionAcceptor
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComConfiguration   configuration ;
		private final ComChannel.Creator channelCreator;
		private final ComManager         manager       ;
		
		// (01.11.2018 TM)TODO: JET-43: cache UTF-8 bytes for configuration
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final ComConfiguration configuration, final Creator channelCreator, final ComManager manager)
		{
			super();
			this.configuration  = configuration ;
			this.channelCreator = channelCreator;
			this.manager        = manager       ;
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
			 *  - pass to manager
			 */
		}
		
	}
	
}
