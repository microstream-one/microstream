import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class BabbleClient
{
	public static void main(final String[] args) throws IOException
	{
		try
		{
			try(
				Socket kkSocket = new Socket("localhost", 4444);
				PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()))
			)
			{
				boolean run = true;
				String fromServer;
				while(run)
				{
					System.out.println("Sending Hello.");
					out.println("Hello");

					fromServer = in.readLine();
					System.out.println("Server said: "+fromServer);
					if(fromServer.equals("Bye."))
					{
						run = false;
					}
				}
			}
		}
		catch(final UnknownHostException e)
		{
			System.err.println("Don't know about host: taranis.");
			System.exit(1);
		}
		catch(final IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to: taranis.");
			System.exit(1);
		}
	}
}