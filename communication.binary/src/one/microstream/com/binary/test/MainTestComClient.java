package one.microstream.com.binary.test;

import one.microstream.com.ComChannel;
import one.microstream.com.ComClient;
import one.microstream.com.binary.ComBinary;

public class MainTestComClient
{
	public static void main(final String[] args)
	{
		// convenience & customization example 1
//		final ComChannel com = Com.Foundation()
//			.setClientTargetAddress(Com.localHostSocketAddress(Com.defaultPort()))
//			.setPersistenceAdaptor(ComPersistenceAdaptorBinary.New())
//			.createClient()
//			.connect()
//		;

		// convenience & customization example 2
//		final ComChannel com = Com.Client(ComPersistenceAdaptorBinary.New())
//			.connect()
//		;

		// convenience & customization example 3
//		final ComChannel com = Com.connect(ComPersistenceAdaptorBinary.New());

		// convenience & customization example 4
//		final ComChannel com = ComBinary.connect();
//
//		System.out.println("Server reply: " + com.request("Hello Server!"));
		
		

		// convenience & customization example 5
		
		// setup a client instance for a custom address
		final ComClient<?> client = ComBinary.Foundation()
//			.setClientTargetAddress(new InetSocketAddress("www.myAddress.com", 1337))
			.createClient()
		;
		
		// create a channel by connecting the client
		final ComChannel channel = client.connect();
		
		// send an object graph (customer and its name) through the channel and print the response
		System.out.println("Server reply: " + channel.request(new Customer("John Doe")));
	}
}
