package net.jadoth.com;

import java.net.InetSocketAddress;

public interface ComConnectionListenerCreator<C>
{
	public ComConnectionListener<C> createConnectionListener(InetSocketAddress address);
	
	public static <C> ComConnectionListenerCreator<C> New()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME enclosing_type#enclosing_method()
	}
}
