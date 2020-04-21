package one.microstream.storage.io;

import java.nio.channels.WritableByteChannel;

import one.microstream.afs.AWritableFile;

public interface ProtageNioChannelWritableFile extends AWritableFile
{
	public WritableByteChannel channel();
}
