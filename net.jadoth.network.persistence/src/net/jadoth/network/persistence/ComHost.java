package net.jadoth.network.persistence;

public interface ComHost
{
	public ComConfiguration configuration();
	
	/**
	 * Listens for incoming connections and relays them for processing.
	 */
	public void acceptConnections();
	
	public void start();
	
	public void stop();
	
	public boolean isRunning();
	
	/* (31.10.2018 TM)TODO: JET-44
	 * - ComConfiguration
	 * - Network Configuration (port and stuff)
	 * - A target for accepted connections
	 */
	
}
