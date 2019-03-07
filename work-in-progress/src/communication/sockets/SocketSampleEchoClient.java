package communication.sockets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class SocketSampleEchoClient
{
	public static void main(final String[] args) throws Throwable
	{
		System.out.println("Starting client");
		try(
			final Socket         kkSocket = new Socket("localhost", 4444);
			final PrintWriter    out      = new PrintWriter(kkSocket.getOutputStream(), true);
			final BufferedReader in       = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
			final BufferedReader stdIn    = new BufferedReader(new InputStreamReader(System.in));
		)
		{
			System.out.println("Client running");
			out.println("login");
			for(String fromServer, fromUser; (fromServer = in.readLine()) != null;)
			{
				System.out.println("Server said: " + fromServer);

				fromUser = stdIn.readLine();
				if(fromUser != null)
				{
					System.out.println("Input: " + fromUser);
					out.println(fromUser);
				}
			}
		}
	}
}
