package various;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BabbleServer
{
	public static void main(final String[] args) throws IOException
	{
		try(ServerSocket serverSocket = new ServerSocket(4444))
		{
			try(Socket clientSocket = serverSocket.accept())
			{
				try(
					final PrintWriter    out = new PrintWriter(clientSocket.getOutputStream(), true);
					final BufferedReader in  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
				)
				{
					String inputLine;
					while((inputLine = in.readLine()) != null)
					{
						System.out.println("Client said: "+inputLine);
						out.println("You said: "+inputLine);
						if(inputLine.equals("Bye."))
						{
							break;
						}
					}
				}
			}
			catch(final IOException e)
			{
				System.err.println("Accept failed.");
				System.exit(1);
			}
		}
		catch(final IOException e)
		{
			System.err.println("Could not listen on port: 4444.");
			System.exit(1);
		}
	}
}
