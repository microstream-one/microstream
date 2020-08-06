package one.microstream.test.afs;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsConsolidate
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		
		final ADirectory dir  = fs.ensureDirectoryPath(AfsTest.TEST_DIRECTORY_PATH);
		final AFile f1 = dir.ensureFile("f1");
		final AFile f2 = dir.ensureFile("f2");
		final AFile f3 = dir.ensureFile("f3");
		
		f1.ensureExists();
		f2.ensureExists();
		f3.ensureExists();
		
		System.out.println(dir);
		System.out.println(dir.listFiles());
		
		final int consolidatedCount = dir.consolidate();
		System.out.println("Consolidated " + consolidatedCount);
		System.out.println(dir.listFiles());
	}
}
