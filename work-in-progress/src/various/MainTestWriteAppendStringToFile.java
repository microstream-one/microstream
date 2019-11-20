package various;

import java.nio.file.Path;
import java.nio.file.Paths;

import one.microstream.files.XFiles;
import one.microstream.io.XIO;

public class MainTestWriteAppendStringToFile
{
	
	public static void main(final String[] args)
	{
		final Path file = Paths.get("D:/testString.txt");
		
		final String s = XIO.execute(() ->
			XFiles.readString(file)
		);
		System.out.println(s);
		
		final Long writeCount = XIO.execute(() ->
			XFiles.writeAppend(file, s)
		);
		System.out.println("Written bytes: " + writeCount);
	}
	
}
