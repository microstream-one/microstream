package one.microstream.storage.io;

import java.nio.channels.WritableByteChannel;

import one.microstream.afs.ProtageWritableFile;

public interface ProtageNioChannelWritableFile extends ProtageWritableFile
{
	public WritableByteChannel channel();
}
