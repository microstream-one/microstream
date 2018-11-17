package net.jadoth.com;

public interface ComConnectionAcceptorCreator<C>
{
	public ComConnectionAcceptor<C> createConnectionAcceptor(
		ComProtocolProvider        protocolProvider       ,
		ComProtocolStringConverter protocolStringConverter,
		ComConnectionHandler<C>    connectionHandler      ,
		ComHostChannelCreator<C>   channelCreator         ,
		ComHostChannelAcceptor<C>      channelAcceptor
	);
	
	
	public static <C> ComConnectionAcceptorCreator<C> New()
	{
		return new ComConnectionAcceptorCreator.Implementation<>();
	}
	
	public final class Implementation<C> implements ComConnectionAcceptorCreator<C>
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
		public ComConnectionAcceptor<C> createConnectionAcceptor(
			final ComProtocolProvider        protocolProvider       ,
			final ComProtocolStringConverter protocolStringConverter,
			final ComConnectionHandler<C>    connectionHandler      ,
			final ComHostChannelCreator<C>   channelCreator         ,
			final ComHostChannelAcceptor<C>      channelAcceptor
		)
		{
			return ComConnectionAcceptor.New(
				protocolProvider       ,
				protocolStringConverter,
				connectionHandler      ,
				channelCreator         ,
				channelAcceptor
			);
		}
		
	}
	
}
