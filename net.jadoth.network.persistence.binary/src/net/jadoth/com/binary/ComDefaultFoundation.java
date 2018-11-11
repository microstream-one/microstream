package net.jadoth.com.binary;

import java.nio.channels.SocketChannel;

import net.jadoth.com.ComFoundation;

public interface ComDefaultFoundation<F extends ComDefaultFoundation<F>>
extends ComFoundation<SocketChannel, F>
{
	public static ComDefaultFoundation<?> New()
	{
		return new ComDefaultFoundation.Implementation<>();
	}
	
	public class Implementation<F extends ComDefaultFoundation.Implementation<F>>
	extends ComFoundation.Implementation<SocketChannel, F>
	implements ComDefaultFoundation<F>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public ComDefaultChannelCreator ensureChannelCreator()
		{
			return ComDefaultChannelCreator.New();
		}
				
		@Override
		public ComDefaultProtocolSender ensureProtocolSender()
		{
			return ComDefaultProtocolSender.New(
				this.getProtocolStringConverter()
			);
		}
		
	}
	
}
