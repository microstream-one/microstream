package one.microstream.test.afs;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsEnsurePath
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		
		final ADirectory dir1  = fs.ensureDirectoryPath(AfsTest.TEST_DIRECTORY_PATH);
		final AFile      file1 = fs.ensureFilePath(AfsTest.TEST_FILE_PATH);
		
		System.out.println("dir1 = " + dir1);
		System.out.println("file1 = " + file1);
		System.out.println("file1 parent = " + file1.parent());
		System.out.println();
		
		final ADirectory dir2  = fs.ensureDirectoryPath(AfsTest.TEST_DIRECTORY_PATH);
		final AFile      file2 = fs.ensureFilePath(AfsTest.TEST_FILE_PATH);

		System.out.println("dir2 = " + dir2);
		System.out.println("file2 = " + file2);
		System.out.println("file2 parent = " + file2.parent());
		System.out.println();
		
		AfsTest.mustBeSame(dir1, dir2);
		AfsTest.mustBeSame(file1, file2);
		
		final AFile file1a = dir1.ensureFile(AfsTest.TEST_FILE_PATH[AfsTest.TEST_FILE_PATH.length - 1]);
		AfsTest.mustBeSame(file1, file1a);
	}
}
