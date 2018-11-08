package net.jadoth.com;


/**
 * Gateway/relay to the actual application/framework communication logic.
 * Potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
public interface ComChannelAcceptor
{
	public void acceptChannel(ComChannel channel);
}
