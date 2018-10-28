package net.jadoth.storage.io;

import java.nio.channels.ByteChannel;

public interface ProtageNioChannel extends ProtageNioChannelReadable, ProtageNioChannelWritable
{
	@Override
	public ByteChannel channel();
}
