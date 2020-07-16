package one.microstream.afs.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AIoHandler;
import one.microstream.afs.AItem;
import one.microstream.afs.AWritableFile;
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

	
	
	
	public static NioIoHandler New()
	{
		return new NioIoHandler.Default();
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
		
		// (15.07.2020 TM)TODO: priv#49: maybe use WatchService stuff to automatically register newly appearing children.
		
		@Override
		protected void specificInventorize(final ADirectory directory)
		{
			final Path dirPath = this.toSubjectDirectory(directory);
			if(!XIO.unchecked.exists(dirPath))
			{
				// no point in trying to inventorize a directory that does not physically exist, yet.
				return;
			}
			
			try(DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath))
			{
		        for(final Path p : stream)
		        {
		        	final String identifier = XIO.getFileName(p);
		        	if(XIO.isDirectory(p))
		        	{
		        		directory.ensureDirectory(identifier);
		        	}
		        	else
		        	{
		        		directory.ensureFile(identifier);
		        	}
		        }
		    }
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected boolean specificOpenReading(final NioReadableFile file)
		{
			return file.openChannel();
		}

		@Override
		protected boolean specificIsOpen(final NioReadableFile file)
		{
			return file.isChannelOpen();
		}

		@Override
		protected boolean specificClose(final NioReadableFile file)
		{
			return file.closeChannel();
		}

		@Override
		protected boolean specificOpenWriting(final NioWritableFile file)
		{
			return file.openChannel();
		}

		@Override
		protected void specificCreate(final ADirectory directory)
		{
			final Path dir = NioFileSystem.toPath(directory);
			XIO.unchecked.ensureDirectory(dir);
		}

		@Override
		protected void specificCreate(final NioWritableFile file)
		{
			try
			{
				// existence of parent directory has already been ensured before calling this method.
				Files.createFile(file.path());
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		@Override
		protected void specificTruncateFile(final NioWritableFile file, final long newSize)
		{
			try
			{
				XIO.truncate(file.path(), newSize);
			}
			catch (final IOException e)
			{
				throw new IORuntimeException(e);
			}
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
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected ByteBuffer specificReadBytes(final NioReadableFile sourceFile)
		{
			try
			{
				return XIO.read(sourceFile.ensureOpenChannel());
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
			try
			{
				return XIO.read(sourceFile.ensureOpenChannel(), position);
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
			try
			{
				return XIO.read(sourceFile.ensureOpenChannel(), position, length);
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
			try
			{
				return XIO.read(sourceFile.ensureOpenChannel(), targetBuffer);
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
			try
			{
				return XIO.read(sourceFile.ensureOpenChannel(), targetBuffer, position);
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
			try
			{
				return XIO.read(sourceFile.ensureOpenChannel(), targetBuffer, position, length);
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
				return this.specificReadBytes(sourceFile, bufferProvider.provideBuffer());
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
				return this.specificReadBytes(sourceFile, bufferProvider.provideBuffer(), position);
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
				return this.specificReadBytes(sourceFile, bufferProvider.provideBuffer(length), position, length);
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
			final NioWritableFile handlableTarget = this.castWritableFile(target);
			
			try
			{
				return XIO.copyFile(
					sourceFile.ensureOpenChannel(),
					handlableTarget.ensureOpenChannel()
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected long specificCopyTo(
			final NioReadableFile sourceFile    ,
			final long            sourcePosition,
			final AWritableFile   target
		)
		{
			final NioWritableFile handlableTarget = this.castWritableFile(target);
			
			try
			{
				return XIO.copyFile(
					sourceFile.ensureOpenChannel(),
					sourcePosition,
					handlableTarget.ensureOpenChannel()
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected long specificCopyTo(
			final NioReadableFile sourceFile    ,
			final long            sourcePosition,
			final long            length        ,
			final AWritableFile   target
		)
		{
			final NioWritableFile handlableTarget = this.castWritableFile(target);
			
			try
			{
				return XIO.copyFile(
					sourceFile.ensureOpenChannel(),
					sourcePosition,
					length,
					handlableTarget.ensureOpenChannel()
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

//		@Override
//		protected long specificCopyTo(
//			final NioReadableFile sourceFile    ,
//			final AWritableFile   target        ,
//			final long            targetPosition
//		)
//		{
//			final NioWritableFile handlableTarget = this.castWritableFile(target);
//
//			try
//			{
//				return XIO.copyFile(
//					sourceFile.ensureOpenChannel(),
//					handlableTarget.ensureOpenChannel(),
//					targetPosition
//				);
//			}
//			catch(final IOException e)
//			{
//				throw new IORuntimeException(e);
//			}
//		}

//		@Override
//		protected long specificCopyTo(
//			final NioReadableFile sourceFile    ,
//			final AWritableFile   target        ,
//			final long            targetPosition,
//			final long            length
//		)
//		{
//			final NioWritableFile handlableTarget = this.castWritableFile(target);
//
//			try
//			{
//				return XIO.copyFile(
//					sourceFile.ensureOpenChannel(),
//					handlableTarget.ensureOpenChannel(),
//					targetPosition,
//					length
//				);
//			}
//			catch(final IOException e)
//			{
//				throw new IORuntimeException(e);
//			}
//		}

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
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected void specificMoveFile(
			final NioWritableFile sourceFile,
			final AWritableFile   targetFile
		)
		{
			// (28.05.2020 TM)TODO: priv#49: support generic moving (copy and delete)
			
			final NioWritableFile handlableTarget = this.castWritableFile(targetFile);
			
			try
			{
				XIO.move(
					sourceFile.path(),
					handlableTarget.path()
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
	}
	
}
