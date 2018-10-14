package net.jadoth.storage.io;

public interface ProtageWritableFile extends ProtageReadableFile
{
	@Override
	public ProtageWritableDirectory directory();
	
	public ProtageWritingFileChannel createWritingChannel();
}
