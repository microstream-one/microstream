package net.jadoth.com;

public interface ComConnectionListener<C>
{
	public C listenForConnection();
	
	public void close();
	
	public static <C> ComConnectionListenerCreator<C> Creator()
	{
		return ComConnectionListenerCreator.New();
	}
}
