package net.jadoth.com;

import java.net.InetSocketAddress;

public interface ComClient
{
	public ComClientChannel connect() throws ComException;
	
	public InetSocketAddress hostAddress();
	
	
	
	public static <C> ComClient.Implementation<C> New(
		final InetSocketAddress          hostAddress      ,
		final ComConnectionHandler<C>    connectionHandler,
		final ComProtocolStringConverter protocolParser   ,
		final ComClientChannelCreator<C> channelCreator
	)
	{
		return new ComClient.Implementation<>(
			hostAddress      ,
			connectionHandler,
			protocolParser   ,
			channelCreator
		);
	}
	
	public final class Implementation<C> implements ComClient
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final InetSocketAddress          hostAddress      ;
		private final ComConnectionHandler<C>    connectionHandler;
		private final ComProtocolStringConverter protocolParser   ;
		private final ComClientChannelCreator<C> channelCreator   ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final InetSocketAddress          hostAddress      ,
			final ComConnectionHandler<C>    connectionHandler,
			final ComProtocolStringConverter protocolParser   ,
			final ComClientChannelCreator<C> channelCreator
		)
		{
			super();
			this.hostAddress       = hostAddress      ;
			this.connectionHandler = connectionHandler;
			this.protocolParser    = protocolParser   ;
			this.channelCreator    = channelCreator   ;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final InetSocketAddress hostAddress()
		{
			return this.hostAddress;
		}
		
		@Override
		public ComClientChannel connect() throws ComException
		{
			final C           connection = this.connectionHandler.openConnection(this.hostAddress);
			final ComProtocol protocol   = this.connectionHandler.receiveProtocol(connection, this.protocolParser);
			
			return this.channelCreator.createChannel(connection, protocol, this);
		}
		
	}
	
}
