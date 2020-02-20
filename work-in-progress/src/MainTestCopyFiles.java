import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import one.microstream.io.XIO;
import one.microstream.storage.types.StorageLockedFile;

public class MainTestCopyFiles
{
	
	public static void main(final String[] args) throws IOException
	{
		final Path source = XIO.Path("source.txt");
		final Path target = XIO.Path("target.bak");
		final StorageLockedFile slf = StorageLockedFile.openLockedFile(source);
		System.out.println("LockedFile: " + slf);
		Files.copy(source, target); // JDK crap
//		XIO.copyFile(source, XIO.ensureDirectoryAndFile(target));
		
		// this is effectively "replace existing".
		XIO.copyFile(source, target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
	

	
}
