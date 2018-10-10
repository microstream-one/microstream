package net.jadoth.network.persistence.binary;

import java.io.File;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

import net.jadoth.meta.XDebug;
import net.jadoth.persistence.types.PersistenceManager;

public class MainTestNetworkPersistenceServer
{
	public static void main(final String[] args) throws Exception
	{
		final ServerSocketChannel serverSocketChannel = UtilTestNetworkPersistence.openServerSocketChannel();
		
		run(serverSocketChannel, cc ->
		{
			XDebug.println("Server is reading data ...");
			final Object root = cc.receive();
			XDebug.println("* Server completed reading.");
			System.out.println(root);
			XDebug.println("Server is sending response ...");
			cc.send("You said: " + root);
			XDebug.println("* Server completed responding.");
		});
	}
	
	public static void run(final ServerSocketChannel serverSocketChannel, final Consumer<ComChannel> logic)
	{
		while(true)
		{
			// accept (wait for) the next client connection, process the request/data sent via it and then close it.
			XDebug.println("Server awaiting connection ...");
			final SocketChannel socketChannel = UtilTestNetworkPersistence.accept(serverSocketChannel);
			XDebug.println("Server accepted connection. Processing.");
			processNextRequest(socketChannel, logic);
			UtilTestNetworkPersistence.close(socketChannel);
		}
	}
	
	public static void processNextRequest(final SocketChannel socketChannel, final Consumer<ComChannel> logic)
	{
		XDebug.println("Server initializing " + PersistenceManager.class.getSimpleName());
		// create a PersistenceManager around the connection to receive and interpret data (= rebuild the serialized graph)
		final ComChannel cc = UtilTestNetworkPersistence.openComChannel(
			socketChannel,
			new File(MainTestNetworkPersistenceServer.class.getSimpleName()),
			false
		);
		
		logic.accept(cc);
	}
	
}
