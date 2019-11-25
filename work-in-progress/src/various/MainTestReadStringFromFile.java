package various;

import java.nio.file.Paths;

import one.microstream.io.XIO;
import one.microstream.io.XPaths;

public class MainTestReadStringFromFile
{
	
	public static void main(final String[] args)
	{
		final String s = XIO.execute(() ->
			XPaths.readString(Paths.get("D:/testString.txt"))
		);
		
		System.out.println(s);
	}
	
}
