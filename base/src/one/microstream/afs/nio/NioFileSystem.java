package one.microstream.afs.nio;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.AItem;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableFile;
import one.microstream.chars.VarString;
import one.microstream.io.XIO;

public interface NioFileSystem extends AFileSystem
{
	public static Path toPath(final AItem item)
	{
		return NioFileSystem.toPath(item.toPath());
	}
	
	public static Path toPath(final String... pathElements)
	{
		return XIO.Path(pathElements);
	}
	
	
	public static NioFileSystem New()
	{
		/* (29.05.2020 TM)FIXME: priv#49: standard protocol strings? Constants, Enums?
		 * (02.06.2020 TM)Note: the JDK does not define such constants.
		 * E.g. the class FileSystems just uses a plain String "file:///".
		 * All other search results are false positives in JavaDoc and comments.
		 */
		return New("file:///");
	}
	
	public static NioFileSystem New(
		final String defaultProtocol
	)
	{
		return New(
			defaultProtocol,
			NioIoHandler.New()
		);
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
		
		@Override
		public synchronized AReadableFile wrapForReading(final AFile file, final Object user)
		{
			final Path path = this.resolve(file);
			
			return NioReadableFile.New(file, user, path);
		}

		@Override
		public synchronized AWritableFile wrapForWriting(final AFile file, final Object user)
		{
			final Path path = this.resolve(file);
			
			return NioWritableFile.New(file, user, path);
		}
		
		@Override
		public synchronized AReadableFile convertToReading(final AWritableFile file)
		{
			// kind of ugly/unclean casts, but there's no acceptble way to prevent it.
			return NioReadableFile.New(
				file,
				file.user(),
				((NioWritableFile)file).path(),
				((NioWritableFile)file).fileChannel()
			);
		}
		
		@Override
		public synchronized AWritableFile convertToWriting(final AReadableFile file)
		{
			// kind of ugly/unclean casts, but there's no acceptble way to prevent it.
			return NioWritableFile.New(
				file,
				file.user(),
				((NioReadableFile)file).path(),
				((NioReadableFile)file).fileChannel()
			);
		}
		
	}
	
}
