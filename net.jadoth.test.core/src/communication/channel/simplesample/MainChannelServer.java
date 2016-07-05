package communication.channel.simplesample;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public class MainChannelServer
{
	public static void main(final String[] args) throws Exception
	{
		// open server channel to listen for client requests on the specified port
		final ServerSocketChannel serverChannel = ChannelCommon.openServerChannelLocalhost();

		while(true)
		{
			// accept client request as new temporary channel (blocking wait)
			System.out.println("Waiting for request...");
			try(final SocketChannel clientChannel = serverChannel.accept())
			{
				System.out.println("Received client request from "+clientChannel.socket().getInetAddress());

				// single threaded client handling (for simplicity)
				for(String message = null; !"bye".equals(message);)
				{
					System.out.println("Waiting for message...");
					message = ChannelCommon.readString(clientChannel);
					System.out.println("Client said: "+message);

					// build response
					final String response =
						"Hello " + clientChannel.socket().getInetAddress()+". You said \""+message+"\"."
					;

					// send response
					ChannelCommon.sendString(clientChannel, response);
				}
				System.out.println("Client disconnected. Closing channel");
			}
		}
	}

}
