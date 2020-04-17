package one.microstream.storage.io;

import java.nio.channels.ReadableByteChannel;

import one.microstream.afs.ProtageReadableFile;

public interface ProtageNioChannelReadableFile extends ProtageReadableFile
{
	public ReadableByteChannel channel();
}
