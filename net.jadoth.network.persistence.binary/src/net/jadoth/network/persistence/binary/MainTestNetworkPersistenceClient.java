package net.jadoth.network.persistence.binary;

import java.nio.channels.SocketChannel;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceManager;

public class MainTestNetworkPersistenceClient
{
	private static final long CLIENT_ID     = System.currentTimeMillis();
	private static final int  REQUEST_COUNT = 1000;
	private static final int  REQUEST_DELAY = 200; // ms

	
	
	// ignore all problems for simplicity's sake
	public static void main(final String[] args) throws Throwable
	{
		// Send n messages to the server, each in an exclusive connection (simplicity's sake)
		for(int i = 1; i <= REQUEST_COUNT; i++)
		{
			try(final SocketChannel channel = UtilTestNetworkPersistence.openChannelLocalhost())
			{
				Thread.sleep(REQUEST_DELAY);
				
				final PersistenceManager<Binary> pm = UtilTestNetworkPersistence.createPersistenceManager(channel);
				
				pm.store("Request " +CLIENT_ID + ":"+ i);
			}
		}
	}
}
