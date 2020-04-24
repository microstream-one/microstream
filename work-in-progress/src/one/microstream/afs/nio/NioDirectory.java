package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.ADirectory;
import one.microstream.collections.types.XGettingTable;
import one.microstream.io.XIO;


public interface NioDirectory extends NioItem, ADirectory
{
	@Override
	public NioDirectory parent();
	
	@Override
	public XGettingTable<String, ? extends NioDirectory> directories();
	
	@Override
	public XGettingTable<String, ? extends NioFile> files();
	
	
	
	public final class Default
	extends ADirectory.AbstractSubjectWrapping<Path, NioDirectory, NioFile>
	implements NioDirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Path           wrapped   ,
			final NioDirectory directory ,
			final String         identifier
		)
		{
			super(wrapped, directory, identifier);
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
		public synchronized boolean exists()
		{
			return XIO.unchecked.exists(this.rawPath());
		}
		
	}
	
}
