package one.microstream.storage.test;

import java.net.InetAddress;

import one.microstream.X;
import one.microstream.java.net.BinaryHandlerInetAddress;


public class MainTestInetAddress
{
	public static void main(final String[] args) throws Throwable
	{
		v4();
		v6();
	}
	
	static void v4() throws Throwable
	{
		final InetAddress iaByAddress1 = InetAddress.getByAddress(X.toBytes(0xFF_80_80_01));
		final InetAddress iaByAddress2 = InetAddress.getByAddress("localhost", X.toBytes(0xFF_80_80_02));
		final InetAddress iaByName1    = InetAddress.getByName("localhost");
		final InetAddress iaByName2    = InetAddress.getByName("127.0.0.1");
	
		System.out.println(iaByAddress1);
		System.out.println(iaByAddress2);
		System.out.println(iaByName1   );
		System.out.println(iaByName2   );
	}
	
	static void v6() throws Throwable
	{
		// https://en.wikipedia.org/wiki/IPv6
		final byte[] address1 = BinaryHandlerInetAddress.parseIpV6Address("2001:0db8:0000:0000:0000:8a2e:0370:7");
		final byte[] address2 = BinaryHandlerInetAddress.parseIpV6Address("2001:db8::8a2e:370:7");
		
		final InetAddress iaByAddress1 = InetAddress.getByAddress(address1);
		final InetAddress iaByAddress2 = InetAddress.getByAddress("localhost", address2);
		final InetAddress iaByName1    = InetAddress.getByName("localhost");
		final InetAddress iaByName2    = InetAddress.getByName("2001:db8::8a2e:370:7");
	
		System.out.println(iaByAddress1);
		System.out.println(iaByAddress2);
		System.out.println(iaByName1   );
		System.out.println(iaByName2   );
	}
		
}
