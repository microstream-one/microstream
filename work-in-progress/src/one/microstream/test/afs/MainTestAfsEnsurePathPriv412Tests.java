package one.microstream.test.afs;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsEnsurePathPriv412Tests
{
	public static void main(final String[] args)
	{
		final AFileSystem fs = NioFileSystem.New();
		
		testFile(fs, "D:",  "MyFile1.txt");

		testFile(fs, "RelFileDirLevel1", "MyFile1.txt");
		testFile(fs, "D:", "AbsFileDirLevel1", "MyFile1.txt");

		testFile(fs, "RelFileDirLevel1", "RelFileDirLevel2", "MyFile2.txt");
		testFile(fs, "D:", "AbsFileDirLevel1", "AbsFileDirLevel2", "MyFile2.txt");

		
		testDirectory(fs, "D:");

		testDirectory(fs, "RelFileDirLevel1");
		testDirectory(fs, "D:", "AbsFileDirLevel1");
		testDirectory(fs, "RelFileDirLevel1", "RelFileDirLevel2");
		testDirectory(fs, "D:", "AbsFileDirLevel1", "AbsFileDirLevel2");
	}
	
	static void testFile(final AFileSystem fs, final String... path)
	{
		final AFile file = fs.ensureFilePath(path);
		file.ensureExists();
				
		final AFile reresolved = fs.resolveFilePath(path);
		System.out.println(reresolved);
	}
	
	static void testDirectory(final AFileSystem fs, final String... path)
	{
		final ADirectory file = fs.ensureDirectoryPath(path);
		file.ensureExists();
				
		final ADirectory reresolved = fs.resolveDirectoryPath(path);
		System.out.println(reresolved);
	}
	
}
