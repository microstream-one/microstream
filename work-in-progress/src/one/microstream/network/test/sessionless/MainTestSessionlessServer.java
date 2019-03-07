package one.microstream.network.test.sessionless;

import java.nio.channels.SocketChannel;

import one.microstream.files.XFiles;
import one.microstream.network.simplesession.LogicSimpleNetwork;
import one.microstream.network.types.NetworkConnectionProcessor;
import one.microstream.network.types.NetworkFactoryServerSessionless;

public class MainTestSessionlessServer
{
	/**
	 * Simple example method for processing a new (one-shot) connection:<br>
	 * - print the event and the client's IP address.<br>
	 * - read and print the message based on the common network logic used by client and server.<br>
	 * - echo the message back to the client.<br>
	 * - close the connection in any case.<br>
	 *
	 * @param connection the newly established (one-shot) connection from a client.
	 */
	static final void echoRequest(final SocketChannel connection)
	{
		System.out.println(System.currentTimeMillis()+" Received client request from "+connection.socket().getInetAddress());
		try
		{
			final String message = LogicSimpleNetwork.readString(connection);        // read message
			System.out.println(System.currentTimeMillis()+" Client said: "+message); // print message
			Thread.sleep(1000); // simulate doing much work to serve the request
			LogicSimpleNetwork.sendString("You said: "+message, connection);         // echo message
		}
		catch(final Throwable t)
		{
			t.printStackTrace();     // just print the problem in simple example
		}
		finally
		{
			XFiles.closeSilent(connection); // close channel after every message in simple example
			System.gc();                         // suggest gc to keep example's memory usage constant
		}
	}

	/**
	 * Stateless function instance relaying the connection to the actual processing method. To be replaced by lambda.
	 */
	private static final NetworkConnectionProcessor requestEchoer = new NetworkConnectionProcessor()
	{
		@Override
		public void accept(final SocketChannel connection)
		{
			echoRequest(connection);
		}
	};



	public static void main(final String[] args) throws Throwable
	{
		// Server factory requires only the socket and the connection processor (= business logic) by default.
		new NetworkFactoryServerSessionless.Implementation()
//		.setConnectionListenerMaxThreadCount(1)
//		.setConnectionListenerCheckInterval(1000)
//		.setConnectionProcessorMaxThreadCount(1)
//		.setConnectionProcessorThreadTimeout(1000)
		.setConnectionSocket(LogicSimpleNetwork.openConnectionSocket())
		.setConnectionProcessor(requestEchoer)
		.createServer()
		.activate()
		;

		System.out.println("Server is running.");
	}

}
