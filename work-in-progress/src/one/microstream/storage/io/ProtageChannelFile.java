package one.microstream.storage.io;

import one.microstream.afs.AWritableFile;
import one.microstream.storage.types.StorageHashChannelPart;

public interface ProtageChannelFile extends AWritableFile, StorageHashChannelPart
{
	@Override
	public ProtageChannelDirectory directory();
}
