package net.jadoth.storage.io;

import java.nio.ByteBuffer;

public interface ProtageWritingFileChannel extends ProtageReadingFileChannel
{
	@Override
	public ProtageWritableFile file();
	
	public long write(Iterable<? extends ByteBuffer> sources);
}
