package one.microstream.afs.nio;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.AItem;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AResolver;
import one.microstream.afs.AWritableFile;
import one.microstream.chars.VarString;
import one.microstream.io.XIO;


public interface NioFileSystem extends AFileSystem, AResolver<Path, Path>
{
	public static Path toPath(final AItem item)
	{
		return NioFileSystem.toPath(item.toPath());
	}
	
	public static Path toPath(final String... pathElements)
	{
		return XIO.Path(pathElements);
	}
		
	
	
	@Override
	public NioIoHandler ioHandler();
	
	
	public interface Defaults
	{
		public static String defaultProtocol()
		{
			/* (29.05.2020 TM)TODO: priv#49: standard protocol strings? Constants, Enums?
			 * (02.06.2020 TM)Note: the JDK does not define such constants.
			 * E.g. the class FileSystems just uses a plain String "file:///".
			 * All other search results are false positives in JavaDoc and comments.
			 * 
			 * (19.07.2020 TM):
			 * Downgraded to T0D0 since MicroStream does not require super clean structures regarding this point.
			 */
			return "file:///";
		}
		
	}
			
	public static NioFileSystem New()
	{
		return New(Defaults.defaultProtocol());
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
		final NioIoHandler ioHandler
	)
	{
		return New(
			Defaults.defaultProtocol(),
			notNull(ioHandler)
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
	
	public class Default extends AFileSystem.Abstract<NioIoHandler, Path, Path> implements NioFileSystem
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
		public String deriveFileIdentifier(final String fileName, final String fileType)
		{
			return XIO.addFileSuffix(fileName, fileType);
		}
		
		@Override
		public String deriveFileName(final String fileIdentifier)
		{
			return XIO.getFilePrefix(fileIdentifier);
		}
		
		@Override
		public String deriveFileType(final String fileIdentifier)
		{
			return XIO.getFileSuffix(fileIdentifier);
		}
		
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
		public AFile createFile(
			final ADirectory parent    ,
			final String     identifier,
			final String     name      ,
			final String     type
		)
		{
			if(identifier != null)
			{
				return super.createFile(parent, identifier, name, type);
			}
			
			if(type == null)
			{
				return this.createFile(parent, name);
			}
			
			return this.createFile(parent, XIO.addFileSuffix(name, type));
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
		public AReadableFile wrapForReading(final AFile file, final Object user)
		{
			// note: no locking required for thread-safe trivial logic here.
			final Path path = this.resolve(file);
			
			return NioReadableFile.New(file, user, path);
		}

		@Override
		public AWritableFile wrapForWriting(final AFile file, final Object user)
		{
			// note: no locking required for thread-safe trivial logic here.
			final Path path = this.resolve(file);
			
			return NioWritableFile.New(file, user, path);
		}
		
		@Override
		public AReadableFile convertToReading(final AWritableFile file)
		{
			final NioWritableFile wf = this.ioHandler().castWritableFile(file);
			final boolean actuallyClosedChannel = wf.closeChannel();
			
			final NioReadableFile rf = NioReadableFile.New(
				file,
				file.user(),
				wf.path(),
				null
			);
			
			// replicate opened channel (ONLY!) if necessary
			if(actuallyClosedChannel)
			{
				rf.ensureOpenChannel();
			}
			
			return rf;
		}
		
		@Override
		public AWritableFile convertToWriting(final AReadableFile file)
		{
			final NioReadableFile wf = this.ioHandler().castReadableFile(file);
			final boolean actuallyClosedChannel = wf.closeChannel();
			
			final NioWritableFile rf = NioWritableFile.New(
				file,
				file.user(),
				wf.path(),
				null
			);
			
			// replicate opened channel (ONLY!) if necessary
			if(actuallyClosedChannel)
			{
				rf.ensureOpenChannel();
			}
			
			return rf;
		}
		
	}
	
}
