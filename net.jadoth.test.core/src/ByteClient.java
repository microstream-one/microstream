import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ByteClient
{
	public static void main(final String[] args) throws IOException
	{
		try(
			Socket kkSocket = new Socket("localhost", 4444);
			OutputStream os = kkSocket.getOutputStream();
			InputStream is  = kkSocket.getInputStream();
		)
		{
			final byte[] buffer = new byte[4];
			boolean run = true;
			while(run)
			{
				System.out.println("Sending bytes.");
				os.write(new byte[]{0x30, 0x40, 0x50});

				final int bytesRed = is.read(buffer);
				System.out.println("Server said: ("+bytesRed+") "+Arrays.toString(buffer));
				if(bytesRed == 0)
				{
					System.out.println("aborting");
					run = false;
				}
				buffer[0] =(byte)(buffer[0]+1);
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