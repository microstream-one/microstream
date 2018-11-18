package net.jadoth.com.binary.test;

import net.jadoth.com.Com;
import net.jadoth.com.ComChannel;
import net.jadoth.com.binary.ComPersistenceAdaptorBinary;

public class MainTestComClient
{
	// (16.11.2018 TM)TODO: Convenience client methods
	// (18.11.2018 TM)TODO: set localhost as default address plus an arbitrary default port
	static final ComChannel COM = Com.Foundation()
//		.setClientTargetAddress(XSockets.localHostSocketAddress(1337))
		.setPersistenceAdaptor(ComPersistenceAdaptorBinary.New())
		.createClient()
		.connect()
	;
	
	public static void main(final String[] args)
	{
		System.out.println(COM.request("Hello Server!"));
	}
}
