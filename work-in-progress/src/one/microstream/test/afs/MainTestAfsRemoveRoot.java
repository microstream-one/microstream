package one.microstream.test.afs;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsRemoveRoot
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		
		final ADirectory dir  = fs.ensureDirectoryPath(AfsTest.TEST_DIRECTORY_PATH);
		final AFile      file = fs.ensureFilePath(AfsTest.TEST_FILE_PATH);
		
		System.out.println(dir);
		System.out.println(file);
		System.out.println(file.parent());
		
		final AReadableFile rFile = file.useReading();
		System.out.println("Using: " + rFile);
		
		try
		{
			fs.removeRoot(AfsTest.TEST_DIRECTORY_PATH[0]);
			new RuntimeException("Incorrectly removed root despite being used by " + rFile).printStackTrace();
			return;
		}
		catch(final Exception e)
		{
			System.out.println("Attempting to remove a used root threw correct exception:");
			e.printStackTrace(System.out);
		}
		
		rFile.release();
		fs.removeRoot(AfsTest.TEST_DIRECTORY_PATH[0]);
		System.out.println("Removed unused root \"" + AfsTest.TEST_DIRECTORY_PATH[0] + "\".");
		
	}
	
}
