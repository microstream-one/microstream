package one.microstream.test.afs;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AWritableFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsCreateFiles
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		
		final ADirectory dir  = fs.ensureDirectoryPath(AfsTest.TEST_DIRECTORY_PATH);
		final AFile      file = fs.ensureFilePath(AfsTest.TEST_FILE_PATH);
		
		System.out.println(dir);
		System.out.println(file);
		System.out.println(file.parent());
		
		final AWritableFile wFile = file.useWriting();
		final boolean created1 = wFile.ensureExists();
		System.out.println(file + " created: " + created1);
		
		final boolean created2 = wFile.ensureExists();
		System.out.println(file + " created: " + created2);
	}
}
