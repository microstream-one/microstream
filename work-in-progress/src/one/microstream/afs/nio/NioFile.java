package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.AFile;
import one.microstream.io.XIO;


public interface NioFile extends NioItem, AFile
{
	public final class Default
	extends AFile.AbstractSubjectWrapping<Path, NioDirectory>
	implements NioFile
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Path           file      ,
			final NioDirectory directory ,
			final String         identifier,
			final String         name      ,
			final String         type
		)
		{
			super(file, directory, identifier, name, type);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Path rawPath()
		{
			return this.wrapped();
		}
		
		@Override
		public final String path()
		{
			return this.rawPath().toString();
		}

		@Override
		public synchronized long length()
		{
			return XIO.unchecked.size(this.rawPath());
		}

		@Override
		public synchronized boolean exists()
		{
			return XIO.unchecked.exists(this.rawPath());
		}
		
	}

}
