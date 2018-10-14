package net.jadoth.storage.io;

public interface ProtageReadableFile extends ProtageFile
{
	@Override
	public ProtageReadableDirectory directory();
	
	public ProtageReadingFileChannel createReadingChannel();
}
