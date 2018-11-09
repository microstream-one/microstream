import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ByteServer
{
	public static void main(final String[] args) throws IOException
	{
		try(final ServerSocket serverSocket = new ServerSocket(4444))
		{
			try(final Socket clientSocket = serverSocket.accept())
			{
				try(
					final OutputStream os = clientSocket.getOutputStream();
					final InputStream is = clientSocket.getInputStream()
				)
				{
					final byte[] buffer = new byte[4];
					boolean run = true;
					while(run)
					{
						final int bytesRed = is.read(buffer);
						System.out.println("Client said: ("+bytesRed+") "+Arrays.toString(buffer));
						if(bytesRed == 0)
						{
							System.out.println("aborting");
							run = false;
						}
						buffer[0] =(byte)(buffer[0]+1);
						os.write(buffer);
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
