package one.microstream.storage.io;

import one.microstream.afs.AWritableDirectory;
import one.microstream.storage.types.StorageHashChannelPart;

public interface ProtageChannelDirectory extends AWritableDirectory, StorageHashChannelPart
{
	public ProtageChannelDataFile createNextDataFile(String fileName);
}
