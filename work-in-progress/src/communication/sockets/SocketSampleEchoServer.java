package communication.sockets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketSampleEchoServer
{
	public static void main(final String[] args) throws Throwable
	{
		System.out.println("Starting server");
		try(
			final ServerSocket   serverSocket = new ServerSocket(4444);
			final Socket         clientSocket = serverSocket.accept();
			final PrintWriter    out          = new PrintWriter(clientSocket.getOutputStream(), true);
			final BufferedReader in           = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		)
		{
			System.out.println("Server running");
			for(String inputLine; (inputLine = in.readLine()) != null;)
			{
				System.out.println("Client said: "+inputLine);
				out.println("You said: "+inputLine);
			}
		}
	}
}
