package one.microstream.storage.io;

import java.nio.channels.ReadableByteChannel;

import one.microstream.afs.AReadableFile;

public interface ProtageNioChannelReadableFile extends AReadableFile
{
	public ReadableByteChannel channel();
}
