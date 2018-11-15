package net.jadoth.com;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public interface ComClient
{
	public ComClientChannel connect() throws ComException;
	
	public InetSocketAddress hostAddress();
	
	
	
	public abstract class Abstract<C> implements ComClient
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final InetSocketAddress          hostAddress   ;
		private final ComProtocolStringConverter protocolParser;
		private final ComClientChannelCreator<C> channelCreator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Abstract(
			final InetSocketAddress          hostAddress   ,
			final ComProtocolStringConverter protocolParser,
			final ComClientChannelCreator<C> channelCreator
		)
		{
			super();
			this.hostAddress    = hostAddress   ;
			this.protocolParser = protocolParser;
			this.channelCreator = channelCreator;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final InetSocketAddress hostAddress()
		{
			return this.hostAddress;
		}
		
		protected ComProtocol parseProtocol(final String protocolString)
		{
			return this.protocolParser.parse(protocolString);
		}
		
		protected ComClientChannel createChannel(final C connection, final ComProtocol protocol)
		{
			return this.channelCreator.createChannel(connection, protocol, this);
		}
		
	}
	
	
	public final class Default extends Abstract<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final InetSocketAddress                      hostAddress   ,
			final ComProtocolStringConverter             protocolParser,
			final ComClientChannelCreator<SocketChannel> channelCreator
		)
		{
			super(hostAddress, protocolParser, channelCreator);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComClientChannel connect() throws ComException
		{
			final SocketChannel channel = XSockets.openChannel(this.hostAddress());
			
			/* FIXME ComClient#connect()
			 * - read protocol length and create buffer
			 * - read protocol and create a string from it
			 */
			
			final String protocolString = null;
			final ComProtocol protocol = this.parseProtocol(protocolString);
			
			return this.createChannel(channel, protocol);
		}
	}
	
}
