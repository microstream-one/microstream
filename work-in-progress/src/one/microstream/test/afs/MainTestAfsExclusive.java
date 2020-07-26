package one.microstream.test.afs;

import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsExclusive
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		final AFile file = fs.ensureFilePath(AfsTest.TEST_FILE_PATH);
		
		final AWritableFile wFile1 = file.useWriting();
		final AWritableFile wFile1a = file.useWriting();
		System.out.println("wFile1 = " + wFile1);
		System.out.println();
		AfsTest.mustBeSame(wFile1, wFile1a);
		
		final AWritableFile wFile2;
		try
		{
			wFile2 = file.useWriting("Other user 2");
			new RuntimeException("Second WritableFile has been created despite existing exclusive usage: " + wFile2).printStackTrace();
		}
		catch(final RuntimeException e)
		{
			System.out.println("Attempting exclusive use on existing exclusive use threw correct exception:");
			e.printStackTrace(System.out);
		}
		
		
		final AReadableFile rFile2;
		try
		{
			rFile2 = file.useReading("Other user 2");
			new RuntimeException("Another ReadableFile has been created despite existing exclusive usage: " + rFile2).printStackTrace();
		}
		catch(final RuntimeException e)
		{
			System.out.println("Attempting exclusive use on existing exclusive use threw correct exception:");
			e.printStackTrace(System.out);
		}
		
	}
	
}
