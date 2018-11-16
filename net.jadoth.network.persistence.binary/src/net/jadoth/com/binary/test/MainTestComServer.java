package net.jadoth.com.binary.test;

import net.jadoth.com.Com;
import net.jadoth.com.ComHost;
import net.jadoth.com.XSockets;

public class MainTestComServer
{
	// (16.11.2018 TM)TODO: Convenience host methods
	// (16.11.2018 TM)FIXME: default host persistence plus persistable type iterable
	static final ComHost<?> COM = Com.Foundation()
		.setHostBindingAddress(XSockets.localHostSocketAddress(1337))
		.setChannelAcceptor(System.out::println)
		.setPersistence(null)
		.createHost()
	;
	
	public static void main(final String[] args)
	{
		COM.run();
	}
}
