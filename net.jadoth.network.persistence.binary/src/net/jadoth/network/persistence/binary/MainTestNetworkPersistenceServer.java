package net.jadoth.network.persistence.binary;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceManager;

public class MainTestNetworkPersistenceServer
{
	public static void main(final String[] args) throws Exception
	{
		final ServerSocketChannel serverSocketChannel = UtilTestNetworkPersistence.openServerSocketChannel();
		
		run(serverSocketChannel, System.out::println);
	}
	
	public static void run(final ServerSocketChannel serverSocketChannel, final Consumer<Object> logic)
	{
		while(true)
		{
			// accept (wait for) the next client connection, process the request/data sent via it and then close it.
			final SocketChannel socketChannel = UtilTestNetworkPersistence.accept(serverSocketChannel);
			processNextRequest(socketChannel, logic);
			UtilTestNetworkPersistence.close(socketChannel);
		}
	}
	
	public static void processNextRequest(final SocketChannel socketChannel, final Consumer<Object> logic)
	{
		// create a PersistenceManager around the connection to receive and interpret data (= rebuild the serialized graph)
		final PersistenceManager<Binary> pm = UtilTestNetworkPersistence.createPersistenceManager(socketChannel);
		
		// receiving arbitrary instances is a generic "get" for the PersistenceManager
		final Object graphRoot = pm.get();
		
		// process the rebuilt graph via the arbitrary server logic
		logic.accept(graphRoot);
	}
	
}
