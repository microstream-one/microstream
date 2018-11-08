package net.jadoth.com;

public interface ComConnectionListenerCreator<C>
{
	public ComConnectionListener<C> createConnectionListener(int port);
	
	public static <C> ComConnectionListenerCreator<C> New()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME enclosing_type#enclosing_method()
	}
}
