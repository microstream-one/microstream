package one.microstream.test.afs;

import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsEnsurePathPriv412
{
	public static void main(final String[] args)
	{
		final AFileSystem fs = NioFileSystem.New();
		final String[] path = new String[] {"MyDir", "MyFile.txt"};
				
		final AFile file = fs.ensureFilePath(path);
		file.ensureExists();
				
		fs.resolveFilePath(path);
	}
}
