package one.microstream.storage.io;

import one.microstream.afs.AMutableDirectory;
import one.microstream.storage.types.StorageHashChannelPart;

public interface ProtageChannelDirectory extends AMutableDirectory, StorageHashChannelPart
{
	public ProtageChannelDataFile createNextDataFile(String fileName);
}
