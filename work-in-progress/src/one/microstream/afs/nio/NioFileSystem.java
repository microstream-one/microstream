package one.microstream.afs.nio;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.afs.temp.ADirectory;
import one.microstream.afs.temp.AFile;
import one.microstream.afs.temp.AFileSystem;
import one.microstream.afs.temp.AItem;
import one.microstream.chars.VarString;
import one.microstream.io.XIO;

public interface NioFileSystem extends AFileSystem
{
	public static Path toPath(final AItem item)
	{
		return XIO.Path(item.toPath());
	}
	
	
	public static NioFileSystem New(
		final String       defaultProtocol,
		final NioIoHandler ioHandler
	)
	{
		return new NioFileSystem.Default(
			notNull(defaultProtocol),
			notNull(ioHandler)
		);
	}
	
	public class Default extends AFileSystem.Abstract<Path, Path> implements NioFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String       defaultProtocol,
			final NioIoHandler ioHandler
		)
		{
			super(defaultProtocol, ioHandler);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String getFileName(final AFile file)
		{
			return XIO.getFilePrefix(file.identifier());
		}
		
		@Override
		public String getFileType(final AFile file)
		{
			return XIO.getFileSuffix(file.identifier());
		}
		
		@Override
		public String[] resolveDirectoryToPath(final Path directory)
		{
			return XIO.splitPath(directory);
		}

		@Override
		public String[] resolveFileToPath(final Path file)
		{
			return XIO.splitPath(file);
		}

		@Override
		public Path resolve(final ADirectory directory)
		{
			// does not need synchronization since it only reads immutable state and creates only thread local state.
			return NioFileSystem.toPath(directory);
		}

		@Override
		public Path resolve(final AFile file)
		{
			// does not need synchronization since it only reads immutable state and creates only thread local state.
			return NioFileSystem.toPath(file);
		}
		
		@Override
		protected VarString assembleItemPath(final AItem item, final VarString vs)
		{
			return XIO.assemblePath(vs, item.toPath());
		}
		
	}
	
}
