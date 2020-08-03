package one.microstream.test.afs;

import java.nio.file.Path;

import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.io.XIO;

public class MainTestAfsSpecialRoots
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		
		final Path filePath = XIO.Path("/test/bla/blub/stuff.lol");
		System.out.println(filePath);
		
		final AFile file = fs.ensureFile(filePath);
		
		// must replicate the path string, including the leading "/".
		System.out.println(file);
	}
}
