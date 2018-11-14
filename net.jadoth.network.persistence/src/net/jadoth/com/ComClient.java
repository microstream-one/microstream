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
		private final ComClientChannelCreator<C> channelCreator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Abstract(
			final InetSocketAddress          hostAddress   ,
			final ComClientChannelCreator<C> channelCreator
		)
		{
			super();
			this.hostAddress    = hostAddress   ;
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
		
	}
	
	
	public final class Default extends Abstract<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final InetSocketAddress                      hostAddress   ,
			final ComClientChannelCreator<SocketChannel> channelCreator
		)
		{
			super(hostAddress, channelCreator);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComClientChannel connect() throws ComException
		{
			final SocketChannel channel = XSockets.openChannel(this.hostAddress());
			this.channelCreator.createChannel(channel, null, null);
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ComClient#connect()
		}
	}
	
}
