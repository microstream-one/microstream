package one.microstream.afs.nio;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Arrays;

import one.microstream.io.XIO;

public interface NioPathResolver
{
	public Path toPath(final String... pathElements);
	
	public final class Default implements NioPathResolver
	{
		public Default()
		{
			super();
		}

		@Override
		public Path toPath(final String... pathElements)
		{
			return XIO.Path(pathElements);
		}
	}
	
	public final class Custom implements NioPathResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final FileSystem fileSystem;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Custom(final FileSystem fileSystem)
		{
			this.fileSystem = fileSystem;
		}
		 
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public Path toPath(final String... pathElements)
		{
			if(pathElements.length > 1)
			{
				return this.fileSystem.getPath(pathElements[0], Arrays.copyOfRange(pathElements, 1, pathElements.length));
			}
			else if(pathElements.length == 1)
			{
				return this.fileSystem.getPath(pathElements[0]);
			}
			
			return this.fileSystem.getPath(null);
		}
	}
}
