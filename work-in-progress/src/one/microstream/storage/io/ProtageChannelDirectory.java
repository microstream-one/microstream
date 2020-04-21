package one.microstream.storage.io;

import one.microstream.afs.AWritableDirectory;
import one.microstream.storage.types.StorageHashChannelPart;

public interface ProtageChannelDirectory extends AWritableDirectory, StorageHashChannelPart
{
	@Override
	public ProtageChannelFile createFile(String fileName);

	public ProtageChannelDataFile createNextDataFile(String fileName);
}
