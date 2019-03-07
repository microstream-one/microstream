package one.microstream.network.test.sessionful;

import java.nio.ByteOrder;

import one.microstream.network.types.NetworkClientGreeting;

public class MainTestNetworkGreeting
{
	public static void main(final String[] args) throws Throwable
	{
		final NetworkClientGreeting g = new NetworkClientGreeting.Implementation(
			"localhost",
			1337,
			ByteOrder.nativeOrder(),
			123456789,
			"blabla"
		);
		final String gs = g.toString();
		System.out.println(gs);
		System.out.println("------------------");
		final NetworkClientGreeting.Implementation g1 = NetworkClientGreeting.Implementation.parseGreeting(gs);
		System.out.println(g1);

	}
}
