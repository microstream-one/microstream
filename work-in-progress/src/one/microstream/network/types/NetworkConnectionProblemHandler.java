package one.microstream.network.types;

import java.nio.channels.SocketChannel;

/**
 * Generic handling type for all problems (errors and execptions) occuring during a network operation involving
 * a specific conneciton (instance of {@link SocketChannel}).
 *
 * @author Thomas Muenz
 */
public interface NetworkConnectionProblemHandler
{
	public void handleConnectionProblem(Throwable problem, SocketChannel connection);


	public interface Provider
	{
		public NetworkConnectionProblemHandler providerConnectionProblemHandler();

		public void dispose(NetworkConnectionProblemHandler problemHandler, Throwable cause);
	}
}
