package net.jadoth.storage.io;

import net.jadoth.storage.types.StorageHashChannelPart;

public interface ProtageChannelFile extends ProtageWritableFile, StorageHashChannelPart
{
	@Override
	public ProtageChannelDirectory directory();
}
