package net.jadoth.com;

public interface ComChannelCreator<C>
{
	public ComChannel createChannel(C connection);
		
}
