package one.microstream.afs.local;

import java.nio.file.Path;

import one.microstream.afs.AFile;
import one.microstream.io.XIO;


public interface LocalFile extends LocalItem, AFile
{
	public final class Default
	extends AFile.AbstractWrapper<Path, LocalDirectory>
	implements LocalFile
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Path           file      ,
			final LocalDirectory directory ,
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
