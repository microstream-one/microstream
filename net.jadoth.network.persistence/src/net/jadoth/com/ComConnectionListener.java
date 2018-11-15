package net.jadoth.com;

public interface ComConnectionListener<C>
{
	public C listenForConnection();
	
	public void close();
	
}
