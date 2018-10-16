package net.jadoth.storage.io.fs;

import java.nio.channels.FileChannel;

import net.jadoth.storage.io.ProtageReadingFileChannel;

public interface FileSystemReadingChannel extends ProtageReadingFileChannel
{
	public FileChannel fileChannel();
}
