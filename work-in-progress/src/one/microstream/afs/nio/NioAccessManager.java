package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.temp.AFile;
import one.microstream.afs.temp.AReadableFile;
import one.microstream.afs.temp.AWritableFile;
import one.microstream.afs.temp.AccessManager;

public interface NioAccessManager
{
	public final class Default
	extends AccessManager.Abstract<NioFileSystem>
	implements NioAccessManager
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final NioFileSystem fileSystem)
		{
			super(fileSystem);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected AReadableFile wrapForReading(final AFile file, final Object user)
		{
			final Path path = this.fileSystem().resolver().resolve(file);
			
			return AReadableFile.New(file, user, path);
		}

		@Override
		protected AWritableFile wrapForWriting(final AFile file, final Object user)
		{
			final Path path = this.fileSystem().resolver().resolve(file);
			
			return AWritableFile.New(file, user, path);
		}
		
	}
	
}
