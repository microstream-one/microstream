package one.microstream.storage.io;

import java.nio.channels.ByteChannel;

public interface ProtageNioChannelFile extends ProtageNioChannelReadableFile, ProtageNioChannelWritableFile
{
	@Override
	public ByteChannel channel();
}
