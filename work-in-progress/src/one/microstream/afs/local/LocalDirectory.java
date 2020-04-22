package one.microstream.afs.local;

import java.nio.file.Path;

import one.microstream.afs.ADirectory;
import one.microstream.collections.types.XGettingTable;
import one.microstream.io.XIO;


public interface LocalDirectory extends LocalItem, ADirectory
{
	@Override
	public LocalDirectory parent();
	
	@Override
	public XGettingTable<String, ? extends LocalDirectory> directories();
	
	@Override
	public XGettingTable<String, ? extends LocalFile> files();
	
	
	
	public final class Default
	extends ADirectory.AbstractWrapper<Path, LocalDirectory, LocalFile>
	implements LocalDirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Path           wrapped   ,
			final LocalDirectory directory ,
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
