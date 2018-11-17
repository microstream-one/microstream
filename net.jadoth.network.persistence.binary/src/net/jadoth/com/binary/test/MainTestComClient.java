package net.jadoth.com.binary.test;

import net.jadoth.com.Com;
import net.jadoth.com.ComClientChannel;
import net.jadoth.com.XSockets;
import net.jadoth.com.binary.ComClientChannelCreatorBinary;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;

public class MainTestComClient
{
	static final BinaryPersistenceFoundation<?> pf = BinaryPersistenceFoundation.New();
	
	// (16.11.2018 TM)TODO: Convenience client methods
	static final ComClientChannel COM = Com.Foundation()
		.setClientTargetAddress(XSockets.localHostSocketAddress(1337))
		.setClientChannelCreator(ComClientChannelCreatorBinary.New(pf))
		.setPersistence(pf)
		.createClient()
		.connect()
	;
	
	public static void main(final String[] args)
	{
		System.out.println(COM.request("Hello Server!"));
	}
}
