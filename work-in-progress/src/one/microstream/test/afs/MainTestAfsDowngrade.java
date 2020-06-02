package one.microstream.test.afs;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsDowngrade
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New("file://");
		
		final ADirectory dir  = fs.ensureDirectoryPath("D:", "testDir", "testSubDir");
		final AFile      file = fs.ensureFilePath("D:", "testDir", "testSubDir", "file.txt");
		
		System.out.println(dir);
		System.out.println(file);
		System.out.println(file.parent());
		
		final AWritableFile wFile = file.useWriting();
		final AReadableFile rFile = wFile.downgrade();
		
		System.out.println(rFile);
	}
}
