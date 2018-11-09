package net.jadoth.com;

public interface ComConnectionAcceptorCreator<C>
{
	public ComConnectionAcceptor<C> createConnectionAcceptor(
		ComProtocolProvider        protocolProvider       ,
		ComProtocolStringConverter protocolStringConverter,
		ComProtocolSender<C>       protocolSender         ,
		ComChannelCreator<C>       channelCreator         ,
		ComChannelAcceptor         channelAcceptor
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
			final ComProtocolSender<C>       protocolSender         ,
			final ComChannelCreator<C>       channelCreator         ,
			final ComChannelAcceptor         channelAcceptor
		)
		{
			return ComConnectionAcceptor.New(
				protocolProvider       ,
				protocolSender         ,
				protocolStringConverter,
				channelCreator         ,
				channelAcceptor
			);
		}
		
	}
	
}
