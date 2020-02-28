package one.microstream.storage.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import one.microstream.X;


public class MainTestInetAddress
{
	public static void main(final String[] args) throws UnknownHostException
	{
		final InetAddress iaByAddress1 = InetAddress.getByAddress("localhost", X.toBytes(0xFF_80_80_01));
		final InetAddress iaByAddress2 = InetAddress.getByAddress("localhost", X.toBytes(0xFF_80_80_02));
		final InetAddress iaByName1    = InetAddress.getByName("localhost");
		final InetAddress iaByName2    = InetAddress.getByName("127.0.0.1");
	
		System.out.println(iaByAddress1);
		System.out.println(iaByAddress2);
		System.out.println(iaByName1   );
		System.out.println(iaByName2   );
	
	}
		
}
