package net.jadoth.com.binary.test;

import net.jadoth.com.Com;
import net.jadoth.com.ComChannel;
import net.jadoth.com.XSockets;
import net.jadoth.com.binary.ComPersistenceAdaptorBinary;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;

public class MainTestComClient
{
	static final BinaryPersistenceFoundation<?> pf = BinaryPersistenceFoundation.New();
	
	// (16.11.2018 TM)TODO: Convenience client methods
	// (18.11.2018 TM)TODO: test 0-port and set localhost as default address
	// (18.11.2018 TM)FIXME: implements PersistenceTypeDictionaryManager$Immutable FIX-MEs
	static final ComChannel COM = Com.Foundation()
		.setClientTargetAddress(XSockets.localHostSocketAddress(1337))
		.setPersistenceAdaptor(ComPersistenceAdaptorBinary.New(pf))
		.createClient()
		.connect()
	;
	
	public static void main(final String[] args)
	{
		System.out.println(COM.request("Hello Server!"));
	}
}
