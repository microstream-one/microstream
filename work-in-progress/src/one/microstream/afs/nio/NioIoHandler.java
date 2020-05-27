package one.microstream.afs.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.io.RuntimeIOException;

import one.microstream.afs.temp.ADirectory;
import one.microstream.afs.temp.AFile;
import one.microstream.afs.temp.AIoHandler;
import one.microstream.afs.temp.AItem;
import one.microstream.afs.temp.AWritableFile;
import one.microstream.exceptions.IORuntimeException;
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
		protected boolean specificIsOpen(final NioReadableFile file)
		{
			return file.fileChannel() != null && file.fileChannel().isOpen();
		}

		@Override
		protected boolean specificClose(final NioReadableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificClose()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificOpenWriting(final NioWritableFile file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificOpenWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificCreate(final ADirectory file)
		{
			// FIXME AIoHandler.Abstract<Path,Path,NioItemWrapper,NioFileWrapper,ADirectory,NioReadableFile,NioWritableFile>#specificEnsure()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		protected boolean specificCreate(final NioWritableFile file)
		{
			try
			{
				// (27.05.2020 TM)FIXME: priv#49: does it create parent directories automatically?
				Files.createFile(file.path());
			}
			catch(final FileAlreadyExistsException e)
			{
				return false;
			}
			catch(final IOException e)
			{
				throw new RuntimeIOException(e);
			}

			return true;
		}

		@Override
		protected boolean specificDeleteFile(final NioWritableFile file)
		{
			try
			{
				return XIO.delete(file.path());
			}
			catch (final IOException e)
			{
				throw new RuntimeIOException(e);
			}
		}

		@Override
		protected ByteBuffer specificReadBytes(final NioReadableFile sourceFile)
		{
			// ensure file is opened for reading
			this.openReading(sourceFile);
			
			try
			{
				return XIO.read(sourceFile.fileChannel());
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final NioReadableFile sourceFile,
			final long            position
		)
		{
			// ensure file is opened for reading
			this.openReading(sourceFile);
			
			try
			{
				return XIO.read(sourceFile.fileChannel(), position);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final NioReadableFile sourceFile,
			final long            position  ,
			final long            length
		)
		{
			// ensure file is opened for reading
			this.openReading(sourceFile);
			
			try
			{
				return XIO.read(sourceFile.fileChannel(), position, length);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile  ,
			final ByteBuffer      targetBuffer
		)
		{
			// ensure file is opened for reading
			this.openReading(sourceFile);
			
			try
			{
				return XIO.read(sourceFile.fileChannel(), targetBuffer);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile  ,
			final ByteBuffer      targetBuffer,
			final long            position
		)
		{
			// ensure file is opened for reading
			this.openReading(sourceFile);
			
			try
			{
				return XIO.read(sourceFile.fileChannel(), targetBuffer, position);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile  ,
			final ByteBuffer      targetBuffer,
			final long            position    ,
			final long            length
		)
		{
			// ensure file is opened for reading
			this.openReading(sourceFile);
			
			try
			{
				return XIO.read(sourceFile.fileChannel(), targetBuffer, position, length);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile    ,
			final BufferProvider  bufferProvider
		)
		{
			bufferProvider.initializeOperation();
			try
			{
				return this.specificReadBytes(sourceFile, bufferProvider.provideNextBuffer());
			}
			finally
			{
				bufferProvider.completeOperation();
			}
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile    ,
			final BufferProvider  bufferProvider,
			final long            position
		)
		{
			bufferProvider.initializeOperation();
			try
			{
				return this.specificReadBytes(sourceFile, bufferProvider.provideNextBuffer(), position);
			}
			finally
			{
				bufferProvider.completeOperation();
			}
		}

		@Override
		protected long specificReadBytes(
			final NioReadableFile sourceFile    ,
			final BufferProvider  bufferProvider,
			final long            position      ,
			final long            length
		)
		{
			bufferProvider.initializeOperation();
			try
			{
				return this.specificReadBytes(sourceFile, bufferProvider.provideNextBuffer(), position, length);
			}
			finally
			{
				bufferProvider.completeOperation();
			}
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
			// ensure file is opened for writing
			this.openWriting(targetFile);
			
			try
			{
				return XIO.write(targetFile.fileChannel(), sourceBuffers);
			}
			catch (final IOException e)
			{
				throw new RuntimeIOException(e);
			}
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
		
	}
	
}
