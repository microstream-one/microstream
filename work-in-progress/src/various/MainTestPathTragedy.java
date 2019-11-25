package various;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MainTestPathTragedy
{
	
	public static void main(final String[] args)
	{

		final Path file = Paths.get("D:/downloads/Java/testString.txt");
		System.out.println(file.toString());
		
		final Path dir = file.getParent();
		System.out.println(dir.toString());
		
		final Path file2 = Paths.get(dir.toString(), "testString.txt");
		System.out.println(file2.toString());
		

		final Path file3 = Paths.get("", "D:/downloads", "Java", "testString.txt");
		System.out.println(file3.toString());
		
	}
	
}
