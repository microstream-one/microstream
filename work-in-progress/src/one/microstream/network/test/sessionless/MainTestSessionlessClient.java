package one.microstream.network.test.sessionless;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;

import one.microstream.files.XFiles;
import one.microstream.network.simplesession.LogicSimpleNetwork;

/**
 * Simple test client
 *
 * @author Thomas Muenz
 */
public class MainTestSessionlessClient
{
	private static final long CLIENT_ID = System.currentTimeMillis();
	private static final int REQUEST_COUNT = 1000;
	private static final int REQUEST_DELAY = 200; // ms



	// ignore all problems for sake of simplicity of the example
	public static void main(final String[] args) throws Throwable
	{
		final InetAddress localhost = InetAddress.getLocalHost();

		// Send n messages to the server, each in an exclusive connection (for simplicity of the example)
		for(int i = REQUEST_COUNT; i --> 0;)
		{
			final SocketChannel channel = LogicSimpleNetwork.openRemoteChannel(localhost);
			try
			{
				Thread.sleep(REQUEST_DELAY); // simulate that client does a lot of work before sending the message
				final String message = "Hello from "+CLIENT_ID+"! ("+i+" remaining)";
				final String answer = LogicSimpleNetwork.communicate(channel, message);
				System.out.println(System.currentTimeMillis()+" Server said: "+answer);
			}
			finally {
				XFiles.closeSilent(channel);
				System.gc();
			}
		}
	}

}
