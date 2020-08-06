package one.microstream.test.afs;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;

public class MainTestAfsMoveFile
{

	static final String[] TEST_DIRECTORY_PATH = {"D:", "testDir", "a", "a1", "start"};
	
	public static void main(final String[] args)
	{
		final NioFileSystem fs = NioFileSystem.New();
		
		final ADirectory dir = fs.ensureDirectoryPath(TEST_DIRECTORY_PATH);
		
		final AFile file = dir.ensureFile("moveMe.txt");
		file.useWriting().moveTo(dir.parent());
	}
}
