package net.jadoth.com;

public interface ComConnectionAcceptorCreator<C>
{
	public ComConnectionAcceptor<C> createConnectionAcceptor(
		ComProtocol          protocol       ,
		ComProtocolSender<C> protocolSender ,
		ComChannelCreator<C> channelCreator ,
		ComChannelAcceptor   channelAcceptor
	);
	
	
	public static <C> ComConnectionAcceptorCreator<C> New(final ComProtocolStringConverter protocolStringConverter)
	{
		return new ComConnectionAcceptorCreator.Implementation<>(protocolStringConverter);
	}
	
	public final class Implementation<C> implements ComConnectionAcceptorCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final ComProtocolStringConverter protocolStringConverter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final ComProtocolStringConverter protocolStringConverter)
		{
			super();
			this.protocolStringConverter = protocolStringConverter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComConnectionAcceptor<C> createConnectionAcceptor(
			final ComProtocol          protocol       ,
			final ComProtocolSender<C> protocolSender ,
			final ComChannelCreator<C> channelCreator ,
			final ComChannelAcceptor   channelAcceptor
		)
		{
			return ComConnectionAcceptor.New(protocol, protocolSender, this.protocolStringConverter, channelCreator, channelAcceptor);
		}
		
	}
	
}