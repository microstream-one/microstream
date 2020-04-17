package one.microstream.storage.io;

import one.microstream.afs.ProtageWritableFile;
import one.microstream.storage.types.StorageHashChannelPart;

public interface ProtageChannelFile extends ProtageWritableFile, StorageHashChannelPart
{
	@Override
	public ProtageChannelDirectory directory();
}
