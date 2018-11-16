package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.net.InetSocketAddress;


public interface ComClientCreator<C>
{
	public ComClient.Implementation<C> createClient(
		InetSocketAddress          hostAddress      ,
		ComConnectionHandler<C>    connectionHandler,
		ComProtocolStringConverter protocolParser   ,
		ComClientChannelCreator<C> channelCreator
	);
	
	
	public static <C> ComClientCreator.Implementation<C> New()
	{
		return new ComClientCreator.Implementation<>();
	}
	
	public final class Implementation<C> implements ComClientCreator<C>
	{
		@Override
		public ComClient.Implementation<C> createClient(
			final InetSocketAddress          hostAddress      ,
			final ComConnectionHandler<C>    connectionHandler,
			final ComProtocolStringConverter protocolParser   ,
			final ComClientChannelCreator<C> channelCreator
		)
		{
			return ComClient.New(
				notNull(hostAddress)      ,
				notNull(connectionHandler),
				notNull(protocolParser)   ,
				notNull(channelCreator)
			);
		}
		
	}
}
