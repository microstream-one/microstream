package one.microstream.test.afs;

import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsShared
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		final AFile file = fs.ensureFilePath(AfsTest.TEST_FILE_PATH);
		
		final AReadableFile rFile1 = file.useReading();
		final AReadableFile rFile1a = file.useReading();
		final AReadableFile rFile2 = file.useReading("Other user 2");
		final AReadableFile rFile3 = file.useReading("Other user 3");
		
		System.out.println("rFile1 = " + rFile1);
		System.out.println("rFile2 = " + rFile2);
		System.out.println("rFile3 = " + rFile3);
		System.out.println();
		AfsTest.mustBeSame(rFile1, rFile1a);
		
		
		final AWritableFile wFile;
		try
		{
			wFile = file.useWriting();
			throw new RuntimeException("Writable File has been created despite existing shared usages: " + wFile);
		}
		catch(final RuntimeException e)
		{
			System.out.println("Attempting exclusive use on existing shared use threw correct exception:");
			e.printStackTrace(System.out);
		}
		
	}
	
}
