package one.microstream.storage.test;

import java.net.InetAddress;

import one.microstream.X;
import one.microstream.java.net.AbstractBinaryHandlerInetAddress;


public class MainTestInetAddress
{
	public static void main(final String[] args) throws Throwable
	{
		v4();
		v6();
	}
	
	static void v4() throws Throwable
	{
		final InetAddress iav4ByAddress1 = InetAddress.getByAddress(X.toBytes(0xFF_80_80_01));
		final InetAddress iav4ByAddress2 = InetAddress.getByAddress("localhost", X.toBytes(0xFF_80_80_02));
		final InetAddress iav4ByName1    = InetAddress.getByName("localhost");
		final InetAddress iav4ByName2    = InetAddress.getByName("127.0.0.1");
	
		System.out.println(iav4ByAddress1);
		System.out.println(iav4ByAddress2);
		System.out.println(iav4ByName1   );
		System.out.println(iav4ByName2   );
	}
	
	static void v6() throws Throwable
	{
		// https://en.wikipedia.org/wiki/IPv6
		final byte[] address1 = AbstractBinaryHandlerInetAddress.parseIpV6Address("2001:0db8:0000:0000:0000:8a2e:0370:7");
		final byte[] address2 = AbstractBinaryHandlerInetAddress.parseIpV6Address("2001:db8::8a2e:370:7");
		
		final InetAddress iav6ByAddress1 = InetAddress.getByAddress(address1);
		final InetAddress iav6ByAddress2 = InetAddress.getByAddress("localhost", address2);
		final InetAddress iav6ByName1    = InetAddress.getByName("localhost");
		final InetAddress iav6ByName2    = InetAddress.getByName("2001:db8::8a2e:370:7");
	
		System.out.println(iav6ByAddress1);
		System.out.println(iav6ByAddress2);
		System.out.println(iav6ByName1   );
		System.out.println(iav6ByName2   );
	}
		
}
