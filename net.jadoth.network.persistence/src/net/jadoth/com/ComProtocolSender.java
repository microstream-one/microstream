package net.jadoth.com;

public interface ComProtocolSender<C>
{
	public void sendProtocol(C connection, ComProtocol protocol);
	
}
