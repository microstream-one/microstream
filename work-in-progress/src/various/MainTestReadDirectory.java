package various;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import one.microstream.io.XIO;

public class MainTestReadDirectory
{
	
	public static void main(final String[] args) throws IOException
	{

		final Path dir = Paths.get("D:/Download/");
		final byte[] content = XIO.read_bytes(dir);
		
		System.out.println(Arrays.toString(content));
		
		
	}
	
}
