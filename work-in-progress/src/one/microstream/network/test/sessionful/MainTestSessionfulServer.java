package one.microstream.network.test.sessionful;

import one.microstream.network.simplesession.LogicSimpleNetwork;
import one.microstream.network.simplesession.SimpleSession;
import one.microstream.network.types.NetworkMessageProcessor;



public class MainTestSessionfulServer
{
	// simple echoing logic without any error feedback to the client for simplicity
	static void echoMessage(final SimpleSession session)
	{
		final String clientMessage = session.readMessage();
		System.out.println("Client "+session.sessionId()+" said: "+clientMessage);
		session.sendMessage("("+session.sessionId()+") You said \""+clientMessage+"\"");
	}


	// echoMessage() wrapper function
	private static final NetworkMessageProcessor<SimpleSession> messageEchoer = (final SimpleSession session) ->
	{
		echoMessage(session);
	};



	public static void main(final String[] args) throws Throwable
	{
		// Server factory requires only the server socket, protocol and message processor (= business logic) by default
		LogicSimpleNetwork.serverFactory()
		.setConnectionSocket(LogicSimpleNetwork.openConnectionSocket())
//		.setConnectionListenerMaxThreadCount(1)
//		.setConnectionListenerCheckInterval(1000)
//		.setConnectionProcessorMaxThreadCount(1)
//		.setConnectionProcessorThreadTimeout(1000)
		.setSessionProtocol(LogicSimpleNetwork.protocol())
//		.setSessionTimeout(10_000)
//		.setSessionCheckInterval(1000)
		.setMessageProcessor(messageEchoer)
//		.setMessageListenerMaxThreadCount(1)
//		.setMessageListenerCheckInterval(1000)
//		.setMessageProcessorMaxThreadCount(5)
//		.setMessageProcessorThreadTimeout(1000)
		.createServer()
		.activate()
		;
		System.out.println("Server is running.");

	}
}

