package net.jadoth.network.persistence;

import java.nio.ByteOrder;

import net.jadoth.math._longRange;
import net.jadoth.persistence.types.PersistenceTypeDictionary;

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
	
	
	public interface Configuration
	{
		public PersistenceTypeDictionary typeDictionary();
		
		public ByteOrder byteOrder();
		
		public double version();
		
		public String protocolName();
		
		// (30.10.2018 TM)FIXME: JET-43: replace with proper types that can produce IdProviders
		public _longRange typeIdStrategy();
		
		public _longRange objectIdStrategy();
		
	}
	
}
