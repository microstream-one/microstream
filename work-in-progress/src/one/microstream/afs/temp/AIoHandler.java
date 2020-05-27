package one.microstream.afs.temp;

import java.nio.ByteBuffer;

import one.microstream.chars.XChars;
import one.microstream.io.BufferProvider;
import one.microstream.util.UtilStackTrace;

public interface AIoHandler
{
	public long size(AFile file);

	public boolean exists(AFile file);
	
	public boolean exists(ADirectory directory);
	
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean openReading(AReadableFile file);

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean isOpenReading(AReadableFile file);
		
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean close(AReadableFile file);

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean isClosed(AReadableFile file);
	
	
	public boolean openWriting(AWritableFile file);
	
	public boolean isOpenWriting(AWritableFile file);

	public boolean ensure(ADirectory directory);

	public boolean ensure(AReadableFile file);

	public ActionReport ensureWritable(AWritableFile file);
	
	
	
	
	public ByteBuffer readBytes(AReadableFile sourceFile);
	
	public ByteBuffer readBytes(AReadableFile sourceFile, long position);
	
	public ByteBuffer readBytes(AReadableFile sourceFile, long position, long length);
	
	
	public long readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer);
	
	public long readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer, long position);
	
	public long readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer, long position, long length);
	
	
	public long readBytes(AReadableFile sourceFile, BufferProvider bufferProvider);
	
	public long readBytes(AReadableFile sourceFile, BufferProvider bufferProvider, long position);
	
	public long readBytes(AReadableFile sourceFile, BufferProvider bufferProvider, long position, long length);
		
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target);
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target, long sourcePosition);
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target, long sourcePosition, long length);
	
	
	
	
	public long writeBytes(AWritableFile targetFile, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(AWritableFile sourceFile, AWritableFile targetFile);
	
	public boolean deleteFile(AWritableFile file);
	
	
	public abstract class Abstract<
		FS,
		DS,
		I extends AItem,
		F extends AFile,
		D extends ADirectory,
		R extends AReadableFile,
		W extends AWritableFile
	>
	implements AIoHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<I> typeItem        ;
		private final Class<F> typeFile        ;
		private final Class<D> typeDirectory   ;
		private final Class<R> typeReadableFile;
		private final Class<W> typeWritableFile;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final Class<I> typeItem        ,
			final Class<F> typeFile        ,
			final Class<D> typeDirectory   ,
			final Class<R> typeReadableFile,
			final Class<W> typeWritableFile
		)
		{
			super();
			this.typeItem         = typeItem        ;
			this.typeFile         = typeFile        ;
			this.typeDirectory    = typeDirectory   ;
			this.typeReadableFile = typeReadableFile;
			this.typeWritableFile = typeWritableFile;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected abstract long specificSize(F file);

		protected abstract boolean specificExists(F file);

		protected abstract boolean specificExists(D directory);

		protected abstract boolean specificOpenReading(R file);

		protected abstract boolean specificIsOpenReading(R file);

		protected abstract boolean specificClose(R file);

		protected abstract boolean specificIsClosed(R file);

		protected abstract boolean specificOpenWriting(W file);

		protected abstract boolean specificIsOpenWriting(W file);

		protected abstract boolean specificEnsure(D file);

		protected abstract boolean specificEnsure(R file);

		protected abstract ActionReport specificEnsureWritable(W file);
		
		protected abstract ByteBuffer specificReadBytes(R sourceFile);
		
		protected abstract ByteBuffer specificReadBytes(R sourceFile, long position);
		
		protected abstract ByteBuffer specificReadBytes(R sourceFile, long position, long length);
		
		
		protected abstract long specificReadBytes(R sourceFile, ByteBuffer targetBuffer);
		
		protected abstract long specificReadBytes(R sourceFile, ByteBuffer targetBuffer, long position);
		
		protected abstract long specificReadBytes(R sourceFile, ByteBuffer targetBuffer, long position, long length);
		
		
		protected abstract long specificReadBytes(R sourceFile, BufferProvider bufferProvider);
		
		protected abstract long specificReadBytes(R sourceFile, BufferProvider bufferProvider, long position);
		
		protected abstract long specificReadBytes(R sourceFile, BufferProvider bufferProvider, long position, long length);
			
		
		protected abstract long specificCopyTo(R sourceFile, AWritableFile target);
		
		protected abstract long specificCopyTo(R sourceFile, AWritableFile target, long sourcePosition);
		
		protected abstract long specificCopyTo(R sourceFile, AWritableFile target, long sourcePosition, long length);
		
		
		protected abstract long specificWriteBytes(W targetFile, Iterable<? extends ByteBuffer> sourceBuffers);

		protected abstract void specificMoveFile(W sourceFile, AWritableFile targetFile);
		
		protected abstract boolean specificDeleteFile(W file);
		
		
		
		protected RuntimeException createUnhandledTypeException(final Object subject, final Class<?> checkedType)
		{
			return this.createUnhandledTypeException(subject, checkedType, 1);
		}
		
		protected RuntimeException createUnhandledTypeException(
			final Object   subject      ,
			final Class<?> checkedType  ,
			final int      cutStackTrace
		)
		{
			// (26.05.2020 TM)EXCP: proper exception
			return UtilStackTrace.cutStacktraceByN(new RuntimeException(
				"Instance is not of type \"" + checkedType
				+ "\" and thus cannot be handled: " + XChars.systemString(subject)
			), cutStackTrace + 1);
		}
		
		protected RuntimeException createUnhandledTypeExceptionItem(final Object subject)
		{
			return this.createUnhandledTypeException(subject, this.typeItem, 1);
		}
		
		protected RuntimeException createUnhandledTypeExceptionFile(final Object subject)
		{
			return this.createUnhandledTypeException(subject, this.typeFile, 1);
		}
		
		protected RuntimeException createUnhandledTypeExceptionDirectory(final Object subject)
		{
			return this.createUnhandledTypeException(subject, this.typeDirectory, 1);
		}
		
		protected RuntimeException createUnhandledTypeExceptionReadableFile(final Object subject)
		{
			return this.createUnhandledTypeException(subject, this.typeReadableFile, 1);
		}
		
		protected RuntimeException createUnhandledTypeExceptionWritableFile(final Object subject)
		{
			return this.createUnhandledTypeException(subject, this.typeWritableFile, 1);
		}

		
		protected abstract FS toSubjectFile(final AFile file);
		
		protected abstract DS toSubjectDirectory(final ADirectory directory);
		
		protected abstract long subjectFileSize(FS file);
		
		protected abstract boolean subjectFileExists(FS file);
		
		protected abstract boolean subjectDirectoryExists(DS directory);

		
		
		@Override
		public long size(final AFile file)
		{
			if(this.typeFile.isInstance(file))
			{
				return this.specificSize(this.typeFile.cast(file));
			}
			
			return this.subjectFileSize(this.toSubjectFile(file));
		}

		@Override
		public boolean exists(final AFile file)
		{
			if(this.typeFile.isInstance(file))
			{
				return this.specificExists(this.typeFile.cast(file));
			}
			
			return this.subjectFileExists(this.toSubjectFile(file));
		}

		@Override
		public boolean exists(final ADirectory directory)
		{
			if(this.typeDirectory.isInstance(directory))
			{
				return this.specificExists(this.typeDirectory.cast(directory));
			}
			
			return this.subjectDirectoryExists(this.toSubjectDirectory(directory));
		}

		@Override
		public boolean openReading(final AReadableFile file)
		{
			if(this.typeReadableFile.isInstance(file))
			{
				return this.specificOpenReading(this.typeReadableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}

		@Override
		public boolean isOpenReading(final AReadableFile file)
		{
			if(this.typeReadableFile.isInstance(file))
			{
				return this.specificIsOpenReading(this.typeReadableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}

		@Override
		public boolean close(final AReadableFile file)
		{
			if(this.typeReadableFile.isInstance(file))
			{
				return this.specificClose(this.typeReadableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}

		@Override
		public boolean isClosed(final AReadableFile file)
		{
			if(this.typeReadableFile.isInstance(file))
			{
				return this.specificIsClosed(this.typeReadableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}

		@Override
		public boolean openWriting(final AWritableFile file)
		{
			if(this.typeWritableFile.isInstance(file))
			{
				return this.specificOpenWriting(this.typeWritableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}

		@Override
		public boolean isOpenWriting(final AWritableFile file)
		{
			if(this.typeWritableFile.isInstance(file))
			{
				return this.specificIsOpenWriting(this.typeWritableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}

		@Override
		public boolean ensure(final ADirectory directory)
		{
			if(this.typeDirectory.isInstance(directory))
			{
				return this.specificEnsure(this.typeDirectory.cast(directory));
			}
			
			throw this.createUnhandledTypeExceptionDirectory(directory);
		}

		@Override
		public boolean ensure(final AReadableFile file)
		{
			if(this.typeReadableFile.isInstance(file))
			{
				return this.specificEnsure(this.typeReadableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}

		@Override
		public ActionReport ensureWritable(final AWritableFile file)
		{
			if(this.typeWritableFile.isInstance(file))
			{
				return this.specificEnsureWritable(this.typeWritableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}

		@Override
		public ByteBuffer readBytes(final AReadableFile sourceFile)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificReadBytes(this.typeReadableFile.cast(sourceFile));
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public ByteBuffer readBytes(
			final AReadableFile sourceFile,
			final long          position
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), position);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public ByteBuffer readBytes(
			final AReadableFile sourceFile,
			final long          position  ,
			final long          length
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), position, length);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long readBytes(
			final AReadableFile sourceFile  ,
			final ByteBuffer    targetBuffer
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), targetBuffer);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long readBytes(
			final AReadableFile sourceFile  ,
			final ByteBuffer    targetBuffer,
			final long          position
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), targetBuffer, position);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long readBytes(
			final AReadableFile sourceFile  ,
			final ByteBuffer    targetBuffer,
			final long          position    ,
			final long          length
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), targetBuffer, position, length);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long readBytes(
			final AReadableFile  sourceFile    ,
			final BufferProvider bufferProvider
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), bufferProvider);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long readBytes(
			final AReadableFile  sourceFile    ,
			final BufferProvider bufferProvider,
			final long           position
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), bufferProvider, position);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long readBytes(
			final AReadableFile  sourceFile    ,
			final BufferProvider bufferProvider,
			final long           position      ,
			final long           length
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), bufferProvider, position, length);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long copyTo(
			final AReadableFile sourceFile,
			final AWritableFile target
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), target);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long copyTo(
			final AReadableFile sourceFile    ,
			final AWritableFile target        ,
			final long          sourcePosition
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), target, sourcePosition);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long copyTo(
			final AReadableFile sourceFile    ,
			final AWritableFile target        ,
			final long          sourcePosition,
			final long          length
		)
		{
			if(this.typeReadableFile.isInstance(sourceFile))
			{
				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), target, sourcePosition, length);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long writeBytes(
			final AWritableFile                  targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			if(this.typeWritableFile.isInstance(targetFile))
			{
				return this.specificWriteBytes(this.typeWritableFile.cast(targetFile), sourceBuffers);
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(targetFile);
		}

		@Override
		public void moveFile(
			final AWritableFile sourceFile,
			final AWritableFile targetFile
		)
		{
			if(this.typeWritableFile.isInstance(sourceFile))
			{
				this.specificMoveFile(this.typeWritableFile.cast(sourceFile), targetFile);
				return;
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(sourceFile);
		}

		@Override
		public boolean deleteFile(final AWritableFile file)
		{
			if(this.typeWritableFile.isInstance(file))
			{
				return this.specificDeleteFile(this.typeWritableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}
		
	}
	
}
