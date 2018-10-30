package net.jadoth.network.persistence.binary;

import java.io.File;
import java.nio.channels.SocketChannel;

import net.jadoth.collections.old.OldCollections;
import net.jadoth.meta.XDebug;
import net.jadoth.network.persistence.ComChannel;
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
			XDebug.println("Client opens channel ...");
			try(final SocketChannel channel = UtilTestNetworkPersistence.openChannelLocalhost())
			{
				XDebug.println("Client opened channel. Sleeping ...");
				Thread.sleep(REQUEST_DELAY);

				XDebug.println("Client initializing " + PersistenceManager.class.getSimpleName());
				final ComChannel cc = UtilTestNetworkPersistence.openComChannel(
					channel,
					new File(MainTestNetworkPersistenceClient.class.getSimpleName()),
					true
				);

				XDebug.println("Client sending data ... "); // arbitrary graph
				cc.send(
					OldCollections.ArrayList(
						"Request " +CLIENT_ID + ":"+ i,
						"Some String",
						OldCollections.ArrayList(1, 2, 3)
					)
				);
				XDebug.println("* Client completed sending.");
				
				XDebug.println("Client reading data ... ");
				System.out.println("Server answered: " + cc.receive());
				XDebug.println("* Client completed reading.");
			}
		}
	}
}
