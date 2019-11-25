package various;

import java.nio.file.Path;
import java.nio.file.Paths;

import one.microstream.io.XIO;
import one.microstream.io.XPaths;

public class MainTestWriteAppendStringToFile
{
	
	public static void main(final String[] args)
	{
		final Path file = Paths.get("D:/testString.txt");
		
		final String s = XIO.execute(() ->
			XPaths.readString(file)
		);
		System.out.println(s);
		
		final Long writeCount = XIO.execute(() ->
			XPaths.writeAppending(file, s)
		);
		System.out.println("Written bytes: " + writeCount);
	}
	
}
