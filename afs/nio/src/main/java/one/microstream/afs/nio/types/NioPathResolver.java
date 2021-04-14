package one.microstream.afs.nio.types;

import static one.microstream.X.notNull;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import one.microstream.io.XIO;

@FunctionalInterface
public interface NioPathResolver
{
	public Path resolvePath(final String... pathElements);
	
	
	public static NioPathResolver New()
	{
		return new Default(
			FileSystems.getDefault()
		);
	}
	
	public static NioPathResolver New(final FileSystem fileSystem)
	{
		return new Default(
			notNull(fileSystem)
		);
	}
	
	
	public static class Default implements NioPathResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final FileSystem fileSystem;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final FileSystem fileSystem)
		{
			super();
			this.fileSystem = fileSystem;
		}
		 
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public Path resolvePath(final String... pathElements)
		{
			return XIO.Path(this.fileSystem, pathElements);
		}
	}
	
}
