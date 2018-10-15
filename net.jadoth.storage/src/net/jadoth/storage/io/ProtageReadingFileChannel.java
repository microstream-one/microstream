package net.jadoth.storage.io;

import java.nio.ByteBuffer;

public interface ProtageReadingFileChannel extends ProtageFileChannel
{
	@Override
	public ProtageReadableFile file();
	
	public void open();
	
	public boolean isOpen();
	
	public void close();
	
	public abstract long read(ByteBuffer target, long position);
}
