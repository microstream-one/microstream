package one.microstream.afs.nio;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import one.microstream.afs.temp.ADirectory;
import one.microstream.afs.temp.AFile;
import one.microstream.afs.temp.AIoHandler;
import one.microstream.afs.temp.AItem;
import one.microstream.afs.temp.AWritableFile;
import one.microstream.afs.temp.ActionReport;
import one.microstream.io.BufferProvider;
import one.microstream.io.XIO;


public interface NioIoHandler extends AIoHandler
{

	public static Path toPath(final AItem item)
	{
		if(item instanceof NioItemWrapper)
		{
			return ((NioItemWrapper)item).path();
		}
		
		return NioFileSystem.toPath(item);
	}
	
	public static Path toPath(final String... pathElements)
	{
		return NioFileSystem.toPath(pathElements);
	}

	
	
	
	public final class Default
	extends AIoHandler.Abstract<Path, Path, NioItemWrapper, NioFileWrapper, ADirectory, NioReadableFile, NioWritableFile>
	implements NioIoHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super(
				NioItemWrapper.class,
				NioFileWrapper.class,
				ADirectory.class,
				NioReadableFile.class,
				NioWritableFile.class
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		protected Path toSubjectFile(final AFile file)
		{
			return NioFileSystem.toPath(file.toPath());
		}
		
		@Override
		protected Path toSubjectDirectory(final ADirectory directory)
		{
			return NioFileSystem.toPath(directory.toPath());
		}
		
		@Override
		protected long subjectFileSize(final Path file)
		{
			return XIO.unchecked.size(file);
		}
		
		@Override
		protected boolean subjectFileExists(final Path file)
		{
			return XIO.unchecked.exists(file);
		}
		
		@Override
		protected boolean subjectDirectoryExists(final Path directory)
		{
			return XIO.unchecked.exists(directory);
		}

		@Override
		protected long specificSize(final NioFileWrapper file)
		{
			return this.subjectFileSize(file.path());
		}

		@Override
		protected boolean specificExists(final NioFileWrapper file)
		{
			return this.subjectFileExists(file.path());
		}

		@Override
		protected boolean specificExists(final ADirectory directory)
		{
			return this.subjectFileExists(this.toSubjectDirectory(directory));
		}

		@Override
		protected boolean specificOpenReading(final NioReadableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificOpenReading()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificIsOpenReading(final NioReadableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificIsOpenReading()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificClose(final NioReadableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificClose()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificIsClosed(final NioReadableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificIsClosed()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificOpenWriting(final NioWritableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificOpenWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificIsOpenWriting(final NioWritableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificIsOpenWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificEnsure(final ADirectory file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificEnsure()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificEnsure(final NioReadableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificEnsure()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected ActionReport specificEnsureWritable(final NioWritableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificEnsureWritable()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected ByteBuffer specificReadBytes(final NioReadableFile sourceFile)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificReadBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final NioReadableFile sourceFile,
			final long            position
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificReadBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final NioReadableFile sourceFile,
			final long            position  ,
			final long            length
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificReadBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile  ,
			final ByteBuffer      targetBuffer
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificReadBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile  ,
			final ByteBuffer      targetBuffer,
			final long            position
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificReadBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile  ,
			final ByteBuffer      targetBuffer,
			final long            position    ,
			final long            length
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificReadBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile    ,
			final BufferProvider  bufferProvider
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificReadBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile    ,
			final BufferProvider  bufferProvider,
			final long            position
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificReadBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile    ,
			final BufferProvider  bufferProvider,
			final long            position      ,
			final long            length
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificReadBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificCopyTo(
			final NioReadableFile sourceFile,
			final AWritableFile   target
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificCopyTo()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificCopyTo(
			final NioReadableFile sourceFile    ,
			final AWritableFile   target        ,
			final long            sourcePosition
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificCopyTo()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificCopyTo(
			final NioReadableFile sourceFile    ,
			final AWritableFile   target        ,
			final long            sourcePosition,
			final long            length
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificCopyTo()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected long specificWriteBytes(
			final NioWritableFile                targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificWriteBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected void specificMoveFile(
			final NioWritableFile sourceFile,
			final AWritableFile   targetFile
		)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificMoveFile()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificDeleteFile(final NioWritableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificDeleteFile()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
	
}
