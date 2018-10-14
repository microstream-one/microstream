package net.jadoth.storage.io;

public interface ProtageWritingFileChannel extends ProtageReadingFileChannel
{
	@Override
	public ProtageWritableFile file();
}
