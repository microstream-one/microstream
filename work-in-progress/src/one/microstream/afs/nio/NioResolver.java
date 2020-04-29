package one.microstream.afs.nio;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AResolver;

public interface NioResolver extends AResolver<Path, Path>
{
	@Override
	public ADirectory resolveDirectory(Path directory);
	
	@Override
	public AFile resolveFile(Path file);
	
	@Override
	public ADirectory ensureDirectory(Path directory);
	
	@Override
	public AFile ensureFile(Path file);
	
	
	
	public static NioResolver New(final NioFileSystem fileSystem)
	{
		return new NioResolver.Default(
			notNull(fileSystem)
		);
	}
	
	public final class Default extends AResolver.Default<Path, Path> implements NioResolver
	{
		Default(final NioFileSystem fileSystem)
		{
			super(fileSystem, fileSystem);
		}
		
	}
}
