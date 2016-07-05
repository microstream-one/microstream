package communication.channel.simplesample;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;


public class MainChanelClient
{
	public static void main(final String[] args) throws Throwable
	{
		try(final SocketChannel channel = ChannelCommon.openRemoteChannel(InetAddress.getLocalHost()))
		{
			for(int i = 3; i --> 0;)
			{
				final String message = "Hello! ("+i+" remaining)";
				final String answer = ChannelCommon.communicate(channel, message);
				System.out.println("Server said: "+answer);
				Thread.sleep(1000);
			}
			System.out.println("Server said: "+ChannelCommon.communicate(channel, "bye"));
		}
	}

}
