package net.jadoth.com.binary.test;

import net.jadoth.com.ComChannel;
import net.jadoth.com.binary.ComBinary;

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
		final ComChannel com = ComBinary.connect();
		
		System.out.println("Server reply: " + com.request("Hello Server!"));
	}
}
