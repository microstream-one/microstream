package various;

import java.nio.file.Paths;

import one.microstream.io.XIO;

public class MainTestReadStringFromFile
{
	
	public static void main(final String[] args)
	{
		final String s = XIO.execute(() ->
			XIO.readString(Paths.get("D:/testString.txt"))
		);
		
		System.out.println(s);
	}
	
}
