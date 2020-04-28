package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.AFileSystem;
import one.microstream.afs.AResolver;

public interface NioFileSystem extends AFileSystem
{
	public AResolver<Path, Path> resolver();
}
