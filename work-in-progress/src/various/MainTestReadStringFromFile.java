package various;

import one.microstream.io.XIO;

public class MainTestReadStringFromFile
{
	
	public static void main(final String[] args)
	{
		final String s = XIO.unchecked(() ->
			XIO.readString("D:/testString.txt")
		);
		
		System.out.println(s);
	}
	
}
