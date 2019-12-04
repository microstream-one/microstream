package one.microstream.com.binary.test;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import one.microstream.com.Com;
import one.microstream.com.ComException;
import one.microstream.com.ComHost;
import one.microstream.com.ComHostChannel;
import one.microstream.com.binary.ComBinary;

public class MainTestComServer
{
	public static void main(final String[] args)
	{
//		final BinaryPersistenceFoundation<?> persistence = BinaryPersistence.Foundation()
//			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
//				XIO.unchecked.ensureDirectory(XIO.Path("TypeDictionary"))
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
//		ComBinary.runHost();
		
		
		
		// advanced example with arbitrary host address and business logic
		
		// setup the host instance for a custom address and business logic
		final ComHost<?> host = ComBinary.Foundation()
//			.setHostBindingAddress(new InetSocketAddress("www.myAddress.com", 1337))
			.registerEntityTypes(Customer.class)
			.setHostByteOrder(ByteOrder.BIG_ENDIAN)
			.setHostChannelAcceptor(hostChannel ->
			{
				// sessionless / stateless greeting service.
				final Customer customer = (Customer)hostChannel.receive();
				hostChannel.send("Welcome, " + customer.name());
				hostChannel.close();
			})
			.createHost()
		;
		
		// run the host, making it constantly listen for new connections and relaying them to the logic
		host.run();
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
