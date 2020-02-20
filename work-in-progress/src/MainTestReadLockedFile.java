import java.io.IOException;
import java.nio.file.Path;

import one.microstream.io.XIO;
import one.microstream.storage.types.StorageLockedFile;


public class MainTestReadLockedFile
{
	public static void main(final String[] args) throws IOException
	{
		final Path source = XIO.Path("source.txt");
		final StorageLockedFile slf = StorageLockedFile.openLockedFile(source);
		System.out.println("LockedFile: " + slf);
		
		// works (this is the channel that created the lock, the "owner" channel of the file)
		System.out.println(XIO.readString(slf.fileChannel()));
		
		// does not work as long as the owner channel is open
//		System.out.println(XIO.readString(FileChannel.open(source)));
	}
			
}
