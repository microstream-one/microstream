package various;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.collections.HashTable;
import one.microstream.io.XIO;
import one.microstream.util.FileContentComparer;

public class MainTestFileContentComparer
{
	static final NioFileSystem NIO = NioFileSystem.New();
	
	public static void main(final String[] args)
	{
		final HashTable<AFile, AFile> files = HashTable.New();
		add(files, "D:/Image1.jpg", "D:/Image1 - Kopie.jpg");
		add(files, "D:/Image1.jpg", "D:/Logo2.jpg");
		add(files, "D:/Logo2.jpg", "D:/Logo2.jpg");
		
		System.out.println(FileContentComparer.compareFilesAndAssembleResult(files));
	}
	
	static void add(final HashTable<AFile, AFile> files, final String file1, final String file2)
	{
		add(files, NIO.ensureFile(XIO.Path(file1)), NIO.ensureFile(XIO.Path(file2)));
	}
	
	static void add(final HashTable<AFile, AFile> files, final AFile file1, final AFile file2)
	{
		files.add(file1, file2);
	}
	
}
