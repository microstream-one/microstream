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

	public boolean create(ADirectory directory);

	public boolean create(AWritableFile file);
	

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean isOpen(AReadableFile file);
	
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean openReading(AReadableFile file);
	
	public boolean openWriting(AWritableFile file);
		
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean close(AReadableFile file);
	
	
	
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
	
	public long copyTo(AReadableFile sourceFile, long sourcePosition, AWritableFile target);
	
	public long copyTo(AReadableFile sourceFile, long sourcePosition, long length, AWritableFile target);
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target, long targetPosition);
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target, long targetPosition, long length);
	
	
	
	
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

		protected abstract boolean specificIsOpen(R file);

		protected abstract boolean specificOpenReading(R file);

		protected abstract boolean specificOpenWriting(W file);

		protected abstract boolean specificClose(R file);

		protected abstract boolean specificCreate(D file);

		protected abstract boolean specificCreate(W file);
		
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
		
		protected abstract long specificCopyTo(R sourceFile, long sourcePosition, AWritableFile target);
		
		protected abstract long specificCopyTo(R sourceFile, long sourcePosition, long length, AWritableFile target);
		
		protected abstract long specificCopyTo(R sourceFile, AWritableFile target, long targetPosition);

		protected abstract long specificCopyTo(R sourceFile, AWritableFile target, long targetPosition, long length);
		
		
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
		
		protected boolean isHandledItem(final AItem item)
		{
			return this.typeItem.isInstance(item);
		}
		
		protected boolean isHandledFile(final AFile file)
		{
			return this.typeFile.isInstance(file);
		}
		
		protected boolean isHandledDirectory(final ADirectory directory)
		{
			return this.typeDirectory.isInstance(directory);
		}
		
		protected boolean isHandledReadableFile(final AReadableFile file)
		{
			return this.typeReadableFile.isInstance(file);
		}
		
		protected boolean isHandledWritableFile(final AWritableFile file)
		{
			return this.typeWritableFile.isInstance(file);
		}
		
		protected W castWritableFile(final AWritableFile file)
		{
			if(this.isHandledWritableFile(file))
			{
				return this.typeWritableFile.cast(file);
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}
		

		
		protected abstract FS toSubjectFile(final AFile file);
		
		protected abstract DS toSubjectDirectory(final ADirectory directory);
		
		protected abstract long subjectFileSize(FS file);
		
		protected abstract boolean subjectFileExists(FS file);
		
		protected abstract boolean subjectDirectoryExists(DS directory);

		
		
		@Override
		public long size(final AFile file)
		{
			if(this.isHandledFile(file))
			{
				return this.specificSize(this.typeFile.cast(file));
			}
			
			return this.subjectFileSize(this.toSubjectFile(file));
		}

		@Override
		public boolean exists(final AFile file)
		{
			if(this.isHandledFile(file))
			{
				return this.specificExists(this.typeFile.cast(file));
			}
			
			return this.subjectFileExists(this.toSubjectFile(file));
		}

		@Override
		public boolean exists(final ADirectory directory)
		{
			if(this.isHandledDirectory(directory))
			{
				return this.specificExists(this.typeDirectory.cast(directory));
			}
			
			return this.subjectDirectoryExists(this.toSubjectDirectory(directory));
		}

		@Override
		public boolean openReading(final AReadableFile file)
		{
			if(this.isHandledReadableFile(file))
			{
				return this.specificOpenReading(this.typeReadableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}

		@Override
		public boolean isOpen(final AReadableFile file)
		{
			if(this.isHandledReadableFile(file))
			{
				return this.specificIsOpen(this.typeReadableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}

		@Override
		public boolean close(final AReadableFile file)
		{
			if(this.isHandledReadableFile(file))
			{
				file.iterateObservers(o ->
					o.onBeforeFileClose(file)
				);
				
				final boolean result = this.specificClose(this.typeReadableFile.cast(file));

				file.iterateObservers(o ->
					o.onAfterFileClose(file, result)
				);
				
				return result;
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}

		@Override
		public boolean openWriting(final AWritableFile file)
		{
			if(this.isHandledWritableFile(file))
			{
				return this.specificOpenWriting(this.typeWritableFile.cast(file));
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}

		@Override
		public boolean create(final ADirectory directory)
		{
			if(this.isHandledDirectory(directory))
			{
				return this.specificCreate(this.typeDirectory.cast(directory));
			}
			
			throw this.createUnhandledTypeExceptionDirectory(directory);
		}

		@Override
		public boolean create(final AWritableFile file)
		{
			if(this.isHandledWritableFile(file))
			{
				file.parent().iterateObservers(o ->
					o.onBeforeFileCreate(file)
				);
				file.iterateObservers(o ->
					o.onBeforeFileCreate(file)
				);
				
				final boolean result = this.specificCreate(this.typeWritableFile.cast(file));
				
				file.iterateObservers(o ->
					o.onAfterFileCreate(file, result)
				);
				file.parent().iterateObservers(o ->
					o.onAfterFileCreate(file, result)
				);
				
				return result;
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}

		@Override
		public ByteBuffer readBytes(final AReadableFile sourceFile)
		{
			if(this.isHandledReadableFile(sourceFile))
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
			if(this.isHandledReadableFile(sourceFile))
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
			if(this.isHandledReadableFile(sourceFile))
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
			if(this.isHandledReadableFile(sourceFile))
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
			if(this.isHandledReadableFile(sourceFile))
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
			if(this.isHandledReadableFile(sourceFile))
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
			if(this.isHandledReadableFile(sourceFile))
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
			if(this.isHandledReadableFile(sourceFile))
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
			if(this.isHandledReadableFile(sourceFile))
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
			if(this.isHandledReadableFile(sourceFile))
			{
				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), target);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}
		
		@Override
		public long copyTo(
			final AReadableFile sourceFile    ,
			final long          sourcePosition,
			final AWritableFile target
		)
		{
			if(this.isHandledReadableFile(sourceFile))
			{
				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), sourcePosition, target);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}
		
		@Override
		public long copyTo(
			final AReadableFile sourceFile    ,
			final long          sourcePosition,
			final long          length        ,
			final AWritableFile target
		)
		{
			if(this.isHandledReadableFile(sourceFile))
			{
				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), sourcePosition, length, target);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long copyTo(
			final AReadableFile sourceFile    ,
			final AWritableFile target        ,
			final long          targetPosition
		)
		{
			if(this.isHandledReadableFile(sourceFile))
			{
				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), target, targetPosition);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long copyTo(
			final AReadableFile sourceFile    ,
			final AWritableFile target        ,
			final long          targetPosition,
			final long          length
		)
		{
			if(this.isHandledReadableFile(sourceFile))
			{
				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), target, targetPosition, length);
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
		}

		@Override
		public long writeBytes(
			final AWritableFile                  targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			if(this.isHandledWritableFile(targetFile))
			{
				targetFile.iterateObservers(o ->
					o.onBeforeFileWrite(targetFile, sourceBuffers)
				);
							
				final long writeCount = this.specificWriteBytes(
					this.typeWritableFile.cast(targetFile),
					sourceBuffers
				);
				
				targetFile.iterateObservers(o ->
					o.onAfterFileWrite(targetFile, sourceBuffers, writeCount)
				);
				
				return writeCount;
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(targetFile);
		}

		// (28.05.2020 TM)TODO: priv#49: call moveFile (plus unregister...? Or intentionally not?)
		@Override
		public void moveFile(
			final AWritableFile sourceFile,
			final AWritableFile targetFile
		)
		{
			if(this.isHandledWritableFile(sourceFile))
			{
				targetFile.parent().iterateObservers(o ->
				{
					o.onBeforeFileMove(sourceFile, targetFile);
					o.onBeforeFileDelete(sourceFile);
				});
				targetFile.iterateObservers(o ->
				{
					o.onBeforeFileMove(sourceFile, targetFile);
					o.onBeforeFileDelete(sourceFile);
				});
				
				this.specificMoveFile(this.typeWritableFile.cast(sourceFile), targetFile);
				
				targetFile.iterateObservers(o ->
				{
					o.onAfterFileMove(sourceFile, targetFile);
					o.onAfterFileDelete(sourceFile, true);
				});
				targetFile.parent().iterateObservers(o ->
				{
					o.onAfterFileMove(sourceFile, targetFile);
					o.onAfterFileDelete(sourceFile, true);
				});
				
				return;
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(sourceFile);
		}

		@Override
		public boolean deleteFile(final AWritableFile file)
		{
			if(this.isHandledWritableFile(file))
			{
				file.parent().iterateObservers(o ->
					o.onBeforeFileDelete(file)
				);
				file.iterateObservers(o ->
					o.onBeforeFileDelete(file)
				);
				
				final boolean result = this.specificDeleteFile(this.typeWritableFile.cast(file));

				file.iterateObservers(o ->
					o.onAfterFileDelete(file, result)
				);
				file.parent().iterateObservers(o ->
					o.onAfterFileDelete(file, result)
				);
				
				return result;
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}
		
	}
	
}
