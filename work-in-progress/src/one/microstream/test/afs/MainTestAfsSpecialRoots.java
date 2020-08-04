package one.microstream.test.afs;

import java.nio.file.Path;
import java.nio.file.Paths;

import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsSpecialRoots
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		
		// leading slash paths cause an exception on Windows stating "network path not found". This means "success".
		final Path filePath = Paths.get("/some/path/with/leading/separator.test");
		final AFile file = fs.ensureFile(filePath);

		System.out.println(filePath);
		
		// must replicate the path string, including the leading "/".
		System.out.println(file);
		
		file.ensureExists();
	}
}
