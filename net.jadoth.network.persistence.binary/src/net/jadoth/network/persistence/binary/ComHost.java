package net.jadoth.network.persistence.binary;

public interface ComHost
{
	/* (30.10.2018 TM)TODO: JET-43:
	 * - Must know an immutable TypeDictionary, OGC version, Endianess, etc.
	 * -
	 */
	
	
	/**
	 * Listens for incoming connections and relays them for processing.
	 */
	public void acceptConnections();
	
	
	public void start();
	
	public void stop();
	
	public boolean isRunning();
	
}
