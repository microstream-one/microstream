package one.microstream.afs.local;

import java.nio.file.Path;

import one.microstream.afs.AFileSystem;

public interface LocalFileSystem extends AFileSystem<Path, Path>
{
	@Override
	public LocalDirectory resolveDirectory(Path directory);
	
	@Override
	public LocalFile resolveFile(Path file);
	
}
