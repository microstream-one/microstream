import java.io.File;

import one.microstream.collections.HashTable;
import one.microstream.util.FileContentComparer;

public class MainTestFileContentComparer
{
	public static void main(final String[] args)
	{
		final HashTable<File, File> files = HashTable.New();
		add(files, "D:/Image1.jpg", "D:/Image1 - Kopie.jpg");
		add(files, "D:/Image1.jpg", "D:/Logo2.jpg");
		add(files, "D:/Logo2.jpg", "D:/Logo2.jpg");
		
		System.out.println(FileContentComparer.compareFilesAndAssembleResult(files));
	}
	
	static void add(final HashTable<File, File> files, final String file1, final String file2)
	{
		add(files, new File(file1), new File(file2));
	}
	
	static void add(final HashTable<File, File> files, final File file1, final File file2)
	{
		files.add(file1, file2);
	}
	
}
