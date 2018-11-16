package net.jadoth.com.binary.test;

import net.jadoth.com.Com;
import net.jadoth.com.ComClientChannel;
import net.jadoth.com.XSockets;

public class MainTestComClient
{
	// (16.11.2018 TM)TODO: Convenience client methods
	static final ComClientChannel COM = Com.Foundation()
		.setClientTargetAddress(XSockets.localHostSocketAddress(1337))
		.createClient()
		.connect()
	;
	
	public static void main(final String[] args)
	{
		
	}
}
