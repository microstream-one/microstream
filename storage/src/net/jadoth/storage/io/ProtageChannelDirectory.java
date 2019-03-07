package net.jadoth.storage.io;

import net.jadoth.storage.types.StorageHashChannelPart;

public interface ProtageChannelDirectory extends ProtageWritableDirectory, StorageHashChannelPart
{
	@Override
	public ProtageChannelFile createFile(String fileName);

	public ProtageChannelDataFile createNextDataFile(String fileName);
}
