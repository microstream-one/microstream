package net.jadoth.network.test.sessionful;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;

import net.jadoth.concurrency.JadothThreads;
import net.jadoth.network.simplesession.LogicSimpleAuthentication;
import net.jadoth.network.simplesession.LogicSimpleNetwork;

public class MainTestSessionfulClient
{
//	private static final long CLIENT_ID = System.currentTimeMillis(); // trivial ID for simplicity



	public static void main(final String[] args) throws Throwable
	{
		// open channel
		final SocketChannel channel = LogicSimpleNetwork.openRemoteChannel(InetAddress.getLocalHost());
		channel.configureBlocking(false);
		System.out.println("channel open: "+channel);


		// authenticate
		LogicSimpleAuthentication.sendUsernamePassword(
			Long.toString(1), "secret", channel
		);

		// read greeting
		final String greeting = LogicSimpleNetwork.readString(channel);
		System.out.println(greeting);


		while(true)
		{
			for(int i = 1; i <= 10; i++)
			{
				System.out.println("Sending "+i);
				final String answer = LogicSimpleNetwork.communicate(channel, Integer.toString(i));
				System.out.println("Server said: "+answer);
				JadothThreads.sleep(100);
			}
			JadothThreads.sleep(1000);
		}
	}

}
