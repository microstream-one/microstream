package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.temp.AFileSystem;
import one.microstream.afs.temp.APathResolver;

public interface NioFileSystem extends AFileSystem, APathResolver<Path, Path>
{
	public NioResolver resolver();
}
