package net.jadoth.network.persistence.binary;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.types.BufferSizeProvider;
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
			processNextRequest(serverSocketChannel, logic);
		}
	}
	
	public static void processNextRequest(final ServerSocketChannel serverSocketChannel, final Consumer<Object> logic)
	{
		final SocketChannel newConnection;
		try
		{
			newConnection = serverSocketChannel.accept();
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
		
		final NetworkPersistenceChannelBinary channel = NetworkPersistenceChannelBinary.New(
			newConnection,
			BufferSizeProvider.New()
		);
		
		final BinaryPersistenceFoundation.Implementation foundation = new BinaryPersistenceFoundation.Implementation();
		foundation.setPersistenceChannel(channel);
		
		final PersistenceManager<Binary> pm = foundation.createPersistenceManager();
		
		final Object graphRoot = pm.get();
		
		logic.accept(graphRoot);
	}
	
}
