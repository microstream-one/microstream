package one.microstream.storage.io;

import java.nio.channels.ReadableByteChannel;

public interface ProtageNioChannelReadableFile extends ProtageReadableFile
{
	public ReadableByteChannel channel();
}
