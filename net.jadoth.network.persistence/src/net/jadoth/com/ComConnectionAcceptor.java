package net.jadoth.com;

import static net.jadoth.X.notNull;


/**
 * Logic to greet/authenticate the client, exchange metadata, create a {@link ComChannel} instance.
 * Potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
public interface ComConnectionAcceptor<C>
{
	public ComProtocolProvider protocolProvider();
	
	public void acceptConnection(C socketChannel);
	
	
	
	public static <C> ComConnectionAcceptorCreator<C> Creator()
	{
		return ComConnectionAcceptorCreator.New();
	}
	
	public static <C> ComConnectionAcceptor<C> New(
		final ComProtocolProvider  protocolProvider,
		final ComProtocolSender<C> protocolSender  ,
		final ComHostChannelCreator<C> channelCreator  ,
		final ComChannelAcceptor   channelAcceptor
	)
	{
		
		return new ComConnectionAcceptor.Implementation<>(
			notNull(protocolProvider),
			notNull(protocolSender)  ,
			notNull(channelCreator)  ,
			notNull(channelAcceptor)
		);
	}
	
	public final class Implementation<C> implements ComConnectionAcceptor<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComProtocolProvider  protocolProvider;
		private final ComProtocolSender<C> protocolSender  ;
		private final ComHostChannelCreator<C> channelCreator  ;
		private final ComChannelAcceptor   channelAcceptor ;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final ComProtocolProvider  protocolProvider,
			final ComProtocolSender<C> protocolSender  ,
			final ComHostChannelCreator<C> channelCreator  ,
			final ComChannelAcceptor   channelAcceptor
		)
		{
			super();
			this.protocolProvider = protocolProvider;
			this.protocolSender   = protocolSender  ;
			this.channelCreator   = channelCreator  ;
			this.channelAcceptor  = channelAcceptor ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ComProtocolProvider protocolProvider()
		{
			return this.protocolProvider;
		}
		
		@Override
		public void acceptConnection(final C socketChannel)
		{
			// note: things like authentication could be done here in a wrapping implementation.
						
			final ComProtocol protocol = this.protocolProvider.provideProtocol();
			this.protocolSender.sendProtocol(socketChannel, protocol);
			
			final ComChannel comChannel = this.channelCreator.createChannel(socketChannel);
			this.channelAcceptor.acceptChannel(comChannel);
		}
		
	}
	
}
