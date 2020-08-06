package one.microstream.test.afs;

import java.util.Arrays;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsFileQuerying
{
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		
		final ADirectory dir  = fs.ensureDirectoryPath(AfsTest.TEST_DIRECTORY_PATH);
		final AFile      file = fs.ensureFilePath(AfsTest.TEST_FILE_PATH);
			
		System.out.println(dir);
		System.out.println(file);
		System.out.println(file.parent());

		System.out.println("Dir path = " + Arrays.toString(dir.toPath()));
		System.out.println("File path = " + Arrays.toString(file.toPath()));
		System.out.println("FileName = " + file.name());
		System.out.println("FileType = " + file.type());
	}
}
