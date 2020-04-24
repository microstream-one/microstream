package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.AFileSystem;

public interface NioFileSystem extends AFileSystem<Path, Path>
{
	@Override
	public NioDirectory resolveDirectory(Path directory);
	
	@Override
	public NioFile resolveFile(Path file);
	
}
