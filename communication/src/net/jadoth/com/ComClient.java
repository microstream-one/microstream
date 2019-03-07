package net.jadoth.com;

import java.net.InetSocketAddress;

public interface ComClient<C>
{
	public ComClientChannel<C> connect() throws ComException;
	
	public InetSocketAddress hostAddress();
	
	
	
	public static <C> ComClientCreator<C> Creator()
	{
		return ComClientCreator.New();
	}
	
	public static <C> ComClient.Implementation<C> New(
		final InetSocketAddress          hostAddress       ,
		final ComConnectionHandler<C>    connectionHandler ,
		final ComProtocolStringConverter protocolParser    ,
		final ComPersistenceAdaptor<C>   persistenceAdaptor
	)
	{
		return new ComClient.Implementation<>(
			hostAddress       ,
			connectionHandler ,
			protocolParser    ,
			persistenceAdaptor
		);
	}
	
	public final class Implementation<C> implements ComClient<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final InetSocketAddress          hostAddress       ;
		private final ComConnectionHandler<C>    connectionHandler ;
		private final ComProtocolStringConverter protocolParser    ;
		private final ComPersistenceAdaptor<C>   persistenceAdaptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final InetSocketAddress          hostAddress       ,
			final ComConnectionHandler<C>    connectionHandler ,
			final ComProtocolStringConverter protocolParser    ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor
		)
		{
			super();
			this.hostAddress        = hostAddress       ;
			this.connectionHandler  = connectionHandler ;
			this.protocolParser     = protocolParser    ;
			this.persistenceAdaptor = persistenceAdaptor;
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
		public ComClientChannel<C> connect() throws ComException
		{
			final C                   conn     = this.connectionHandler.openConnection(this.hostAddress);
			final ComProtocol         protocol = this.connectionHandler.receiveProtocol(conn, this.protocolParser);
			final ComClientChannel<C> channel  = this.persistenceAdaptor.createClientChannel(conn, protocol, this);
			
			return channel;
		}
		
	}
	
}
