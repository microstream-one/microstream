package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.temp.ACreator;
import one.microstream.afs.temp.AFileSystem;
import one.microstream.afs.temp.APathResolver;

public interface NioFileSystem extends AFileSystem, APathResolver<Path, Path>
{
	public NioResolver resolver();
	
	
	public final class Default extends AFileSystem.Abstract<Path, Path> implements NioFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                   defaultProtocol     ,
			final NioResolver              resolver            ,
			final ACreator                 creator             ,
			final NioAccessManager.Creator accessManagerCreator,
			final NioIoHandler             ioHandler
		)
		{
			super(defaultProtocol, resolver, creator, accessManagerCreator, ioHandler);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public NioResolver resolver()
		{
			// cast safety ensured by constructors
			return (NioResolver)super.resolver();
		}

		@Override
		public String[] resolveDirectoryToPath(final Path directory)
		{
			// FIXME APathResolver<Path,Path>#resolveDirectoryToPath()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public String[] resolveFileToPath(final Path file)
		{
			// FIXME APathResolver<Path,Path>#resolveFileToPath()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
}
