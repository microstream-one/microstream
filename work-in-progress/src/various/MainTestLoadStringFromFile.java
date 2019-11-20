package various;

import java.nio.file.Paths;

import one.microstream.files.XFiles;
import one.microstream.io.XIO;

public class MainTestLoadStringFromFile
{
	
	public static void main(final String[] args)
	{
		final String s = XIO.execute(() ->
			XFiles.readStringFromFile(Paths.get("D:/testString.txt"))
		);
		
		System.out.println(s);
	}
	
}
