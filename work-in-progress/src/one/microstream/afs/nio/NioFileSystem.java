package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.AFileSystem;
import one.microstream.afs.APathResolver;

public interface NioFileSystem extends AFileSystem, APathResolver<Path, Path>
{
	public NioResolver resolver();
}
