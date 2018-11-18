package net.jadoth.com.binary.test;

import net.jadoth.com.ComChannel;
import net.jadoth.com.binary.ComBinary;

public class MainTestComClient
{
	
//	static final ComChannel COM = Com.Foundation()
////		.setClientTargetAddress(XSockets.localHostSocketAddress(1337))
//		.setPersistenceAdaptor(ComPersistenceAdaptorBinary.New())
//		.createClient()
//		.connect()
//	;

//	static final ComChannel COM = Com.connect(ComPersistenceAdaptorBinary.New());
	
	static final ComChannel COM = ComBinary.connect();
	
	public static void main(final String[] args)
	{
		System.out.println(COM.request("Hello Server!"));
	}
}
