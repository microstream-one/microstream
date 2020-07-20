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
	public static ADirectory directory(final Path path)
	{
		return NioFileSystem.get().ensureDirectory(path);
	}
	
	public static ADirectory directory(final String path)
	{
		return directory(XIO.Path(path));
	}
	
	public static AFile file(final Path path)
	{
		return NioFileSystem.get().ensureFile(path);
	}
	
	public static AFile file(final String path)
	{
		return file(XIO.Path(path));
	}
	
	public static NioFileSystem get()
	{
		return Default.SINGLETON;
	}
	
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
	
		
	public static NioFileSystem New()
	{
		/* (29.05.2020 TM)TODO: priv#49: standard protocol strings? Constants, Enums?
		 * (02.06.2020 TM)Note: the JDK does not define such constants.
		 * E.g. the class FileSystems just uses a plain String "file:///".
		 * All other search results are false positives in JavaDoc and comments.
		 * 
		 * (19.07.2020 TM):
		 * Downgraded to T0D0 since MicroStream does not require super clean structures regarding this point.
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
	
	public class Default extends AFileSystem.Abstract<NioIoHandler, Path, Path> implements NioFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		static final NioFileSystem SINGLETON = NioFileSystem.New();
		
		
		
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
		public AReadableFile convertToReading(final AWritableFile file)
		{
			synchronized(file)
			{
				final NioWritableFile wf = this.ioHandler().castWritableFile(file);
				wf.closeChannel();
				
				final NioReadableFile rf = NioReadableFile.New(
					file,
					file.user(),
					wf.path(),
					null
				);
				rf.ensureOpenChannel();
				
				return rf;
			}
		}
		
		@Override
		public AWritableFile convertToWriting(final AReadableFile file)
		{
			synchronized(file)
			{
				final NioReadableFile wf = this.ioHandler().castReadableFile(file);
				wf.closeChannel();
				
				final NioWritableFile rf = NioWritableFile.New(
					file,
					file.user(),
					wf.path(),
					null
				);
				rf.ensureOpenChannel();
				
				return rf;
			}
		}
		
	}
	
}
