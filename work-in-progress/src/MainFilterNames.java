import java.io.File;
import java.io.IOException;

import one.microstream.files.XFiles;


public class MainFilterNames
{
	public static void main(final String[] args) throws IOException
	{
		final String content = XFiles.readStringFromFileDefaultCharset(new File("D:/_HumanLegacy/HuLe Docs/_Ideen allgemein/Namen.txt"));
		
		final String[] lines = content.split("\\n");
		
		for(final String line : lines)
		{
			String s = line.trim();
			if(s.matches("[A-Z]+"))
			{
				s = s.toLowerCase();
				System.out.println(Character.toUpperCase(s.charAt(0))+s.substring(1));
			}
		}
	}
}
