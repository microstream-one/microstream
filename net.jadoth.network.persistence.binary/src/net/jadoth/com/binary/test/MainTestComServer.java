package net.jadoth.com.binary.test;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.jadoth.com.Com;
import net.jadoth.com.ComException;
import net.jadoth.com.ComHostChannel;
import net.jadoth.com.binary.ComBinary;

public class MainTestComServer
{
	public static void main(final String[] args)
	{
//		final BinaryPersistenceFoundation<?> persistence = BinaryPersistence.Foundation()
//			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
//				XFiles.ensureDirectory(new File("TypeDictionary"))
//			))
//			.setObjectIdProvider(PersistenceObjectIdProvider.Transient())
//			.setTypeIdProvider(PersistenceTypeIdProvider.Transient())
//		;
		
		// convenience & customization example 1
//		final ComHost<?> host = Com.Foundation()
//			.setPersistenceAdaptor(ComPersistenceAdaptorBinary.New(persistence))
//			.setHostBindingAddress(Com.localHostSocketAddress(Com.defaultPort()))
//			.setHostChannelAcceptor(Com::bounce)
//			.createHost()
//		;
//		System.out.println("Starting host ...");
//		host.run();
		
		// convenience & customization example 2
//		final ComHost<?> host = Com.Host(
//			ComPersistenceAdaptorBinaryCreator.New(persistence)
//		);
//		System.out.println("Starting host ...");
//		host.run();

		// convenience & customization example 3
//		ComBinary.runHost(channel -> channel.send("Go away."));
		
		// convenience & customization example 4
//		ComBinary.runHost(MainTestComServer::logAndBounce);
		
		// convenience & customization example 5
		ComBinary.runHost();
	}
	
	/**
	 * Slightly improved version of {@link Com#bounce(ComHostChannel)}
	 * @param channel
	 */
	public static void logAndBounce(final ComHostChannel<SocketChannel> channel)
	{
		final Object received = channel.receive();
		final String string = received.toString();
		
		try
		{
			System.out.println("Received from " + channel.connection().getRemoteAddress()+": " + string);
		}
		catch(final IOException e)
		{
			throw new ComException(e);
		}
		
		final String answer = "You said: \"" + string + "\"";
		channel.send(answer);
		channel.close();
	}
	
}
