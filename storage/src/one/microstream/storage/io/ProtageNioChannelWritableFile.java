package one.microstream.storage.io;

import java.nio.channels.WritableByteChannel;

public interface ProtageNioChannelWritableFile extends ProtageWritableFile
{
	public WritableByteChannel channel();
}
