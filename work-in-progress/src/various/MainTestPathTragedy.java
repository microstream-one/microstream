package various;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainTestPathTragedy
{
	
	public static void main(final String[] args)
	{

//		final Path file = Paths.get("D:/downloads/Java/testString.txt");
//		System.out.println(file.toString());
//
//		final Path dir = file.getParent();
//		System.out.println(dir.toString());
//
//		final Path file2 = Paths.get(dir.toString(), "testString.txt");
//		System.out.println(file2.toString());
//
//
//		final Path file3 = Paths.get("", "D:/downloads", "Java", "testString.txt");
//		System.out.println(file3.toString());
		
//		System.out.println(Paths.get("D:/downloads/Java/testString.txt").toUri().toString());

//		System.out.println(Paths.get("/Java/testString.txt").toUri().toString());
//		System.out.println(Paths.get("/Java/testString.txt").toAbsolutePath().toUri().toString());
//		System.out.println(Paths.get("./Java/testString.txt").toUri().toString());
		System.out.println(Paths.get("./Java/testString.txt").toUri().toString());
		
		final String uri = Paths.get("./Java/testString.txt").toUri().toString();
		System.out.println(uri);
		final Path reconstructed = Paths.get(URI.create(uri));
		System.out.println(reconstructed);
		
		
	}
	
}
