package one.microstream.afs;

import java.nio.ByteBuffer;

import one.microstream.chars.XChars;
import one.microstream.io.BufferProvider;
import one.microstream.util.UtilStackTrace;


public interface AIoHandler
{
	/* (08.06.2020 TM)TODO: priv#49: JavaDoc: guaranteed completeness
	 * guarantees that all specified bytes (where applicable) are read/written.
	 * Any iterative, reattempting, waiting, timeouting etc. logic that is required for this must be provided
	 * by the IoHandler implementation.
	 */
	
	public long size(AFile file);

	public boolean exists(AFile file);
	
	public boolean exists(ADirectory directory);

	public void create(ADirectory directory);

	public void create(AWritableFile file);
	
	public boolean ensureExists(ADirectory directory);

	public boolean ensure(AWritableFile file);
	

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
	
//	public long copyTo(AReadableFile sourceFile, AWritableFile target, long targetPosition);
//
//	public long copyTo(AReadableFile sourceFile, AWritableFile target, long targetPosition, long length);
	
	
	
	
	public long writeBytes(AWritableFile targetFile, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(AWritableFile sourceFile, AWritableFile targetFile);
	
	public boolean deleteFile(AWritableFile file);
	
	public void truncate(AWritableFile file, long newSize);
	
	
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

		protected abstract void specificCreate(D file);

		protected abstract void specificCreate(W file);
		
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
		
//		protected abstract long specificCopyTo(R sourceFile, AWritableFile target, long targetPosition);
//
//		protected abstract long specificCopyTo(R sourceFile, AWritableFile target, long targetPosition, long length);
		
		
		protected abstract long specificWriteBytes(W targetFile, Iterable<? extends ByteBuffer> sourceBuffers);

		protected abstract void specificMoveFile(W sourceFile, AWritableFile targetFile);
		
		protected abstract boolean specificDeleteFile(W file);
		
		protected abstract void specificTruncateFile(W file, long newSize);
		
		
		
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
		
		protected final void validateHandledFile(final AFile file)
		{
			if(this.isHandledFile(file))
			{
				return;
			}
			
			throw this.createUnhandledTypeExceptionFile(file);
		}
		
		protected final void validateHandledDirectory(final ADirectory directory)
		{
			if(this.isHandledDirectory(directory))
			{
				return;
			}
			
			throw this.createUnhandledTypeExceptionDirectory(directory);
		}
		
		protected final void validateHandledReadableFile(final AReadableFile file)
		{
			if(this.isHandledReadableFile(file))
			{
				return;
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}
		
		protected final void validateHandledWritableFile(final AWritableFile file)
		{
			if(this.isHandledWritableFile(file))
			{
				return;
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}
		
		protected W castWritableFile(final AWritableFile file)
		{
			this.validateHandledWritableFile(file);
			
			return this.typeWritableFile.cast(file);
		}
		

		
		protected abstract FS toSubjectFile(final AFile file);
		
		protected abstract DS toSubjectDirectory(final ADirectory directory);
		
		protected abstract long subjectFileSize(FS file);
		
		protected abstract boolean subjectFileExists(FS file);
		
		protected abstract boolean subjectDirectoryExists(DS directory);

		
		
		@Override
		public long size(final AFile file)
		{
			if(!this.isHandledFile(file))
			{
				return this.subjectFileSize(this.toSubjectFile(file));
			}

			return this.specificSize(this.typeFile.cast(file));
		}

		@Override
		public boolean exists(final AFile file)
		{
			if(!this.isHandledFile(file))
			{
				return this.subjectFileExists(this.toSubjectFile(file));
			}

			return this.specificExists(this.typeFile.cast(file));
		}

		@Override
		public boolean exists(final ADirectory directory)
		{
			if(!this.isHandledDirectory(directory))
			{
				return this.subjectDirectoryExists(this.toSubjectDirectory(directory));
			}

			return this.specificExists(this.typeDirectory.cast(directory));
		}

		@Override
		public boolean openReading(final AReadableFile file)
		{
			this.validateHandledReadableFile(file);

			return this.specificOpenReading(this.typeReadableFile.cast(file));
		}

		@Override
		public boolean isOpen(final AReadableFile file)
		{
			this.validateHandledReadableFile(file);

			return this.specificIsOpen(this.typeReadableFile.cast(file));
		}

		@Override
		public boolean close(final AReadableFile file)
		{
			this.validateHandledReadableFile(file);

			file.iterateObservers(o ->
				o.onBeforeFileClose(file)
			);
			
			final boolean result = this.specificClose(this.typeReadableFile.cast(file));

			file.iterateObservers(o ->
				o.onAfterFileClose(file, result)
			);
			
			return result;
		}

		@Override
		public boolean openWriting(final AWritableFile file)
		{
			this.validateHandledWritableFile(file);

			return this.specificOpenWriting(this.typeWritableFile.cast(file));
		}
		
		@Override
		public void create(final ADirectory directory)
		{
			this.validateHandledDirectory(directory);
			
			/* (31.05.2020 TM)TODO: priv#49: if ioHandler does locking, what about the other methods?
			 * Think through locking concept and concerned instances, potential deadlocks, etc. in general.
			 */
			synchronized(this)
			{
				// only handle non-root parent directories. Note that not all roots are of type ARoot
				if(directory.parent() != null)
				{
					this.ensureExists(directory.parent());
				}
				
				directory.iterateObservers(o ->
					o.onBeforeDirectoryCreate(directory)
				);
				
				// it is up to the specific implementation to decide how to handle root directories
				this.specificCreate(this.typeDirectory.cast(directory));
				
				directory.iterateObservers(o ->
					o.onAfterDirectoryCreate(directory)
				);
			}
		}
		

		@Override
		public void create(final AWritableFile file)
		{
			this.validateHandledWritableFile(file);
			
			synchronized(this)
			{
				this.ensureExists(file.parent());
				
				file.parent().iterateObservers(o ->
					o.onBeforeFileCreate(file)
				);
				file.iterateObservers(o ->
					o.onBeforeFileCreate(file)
				);
				
				this.specificCreate(this.typeWritableFile.cast(file));
				
				file.iterateObservers(o ->
					o.onAfterFileCreate(file)
				);
				file.parent().iterateObservers(o ->
					o.onAfterFileCreate(file)
				);
			}
		}
		
		@Override
		public boolean ensureExists(final ADirectory directory)
		{
			final ADirectory actual = ADirectory.actual(directory);
			synchronized(actual)
			{
				if(this.exists(directory))
				{
					return false;
				}
				
				this.create(directory);
				
				return true;
			}
		}

		@Override
		public boolean ensure(final AWritableFile file)
		{
			final AFile actual = file.actual();
			synchronized(actual)
			{
				if(this.exists(file))
				{
					return false;
				}
				
				this.create(file);
				
				return true;
			}
		}

		@Override
		public ByteBuffer readBytes(final AReadableFile sourceFile)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificReadBytes(this.typeReadableFile.cast(sourceFile));
		}

		@Override
		public ByteBuffer readBytes(
			final AReadableFile sourceFile,
			final long          position
		)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), position);
		}

		@Override
		public ByteBuffer readBytes(
			final AReadableFile sourceFile,
			final long          position  ,
			final long          length
		)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), position, length);
		}

		@Override
		public long readBytes(
			final AReadableFile sourceFile  ,
			final ByteBuffer    targetBuffer
		)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), targetBuffer);
		}

		@Override
		public long readBytes(
			final AReadableFile sourceFile  ,
			final ByteBuffer    targetBuffer,
			final long          position
		)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), targetBuffer, position);
		}

		@Override
		public long readBytes(
			final AReadableFile sourceFile  ,
			final ByteBuffer    targetBuffer,
			final long          position    ,
			final long          length
		)
		{
			this.validateHandledReadableFile(sourceFile);
			
			return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), targetBuffer, position, length);
		}

		@Override
		public long readBytes(
			final AReadableFile  sourceFile    ,
			final BufferProvider bufferProvider
		)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), bufferProvider);
		}

		@Override
		public long readBytes(
			final AReadableFile  sourceFile    ,
			final BufferProvider bufferProvider,
			final long           position
		)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), bufferProvider, position);
		}

		@Override
		public long readBytes(
			final AReadableFile  sourceFile    ,
			final BufferProvider bufferProvider,
			final long           position      ,
			final long           length
		)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificReadBytes(this.typeReadableFile.cast(sourceFile), bufferProvider, position, length);
		}

		@Override
		public long copyTo(
			final AReadableFile sourceFile,
			final AWritableFile target
		)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), target);
		}
		
		@Override
		public long copyTo(
			final AReadableFile sourceFile    ,
			final long          sourcePosition,
			final AWritableFile target
		)
		{
			this.validateHandledReadableFile(sourceFile);
			
			return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), sourcePosition, target);
		}
		
		@Override
		public long copyTo(
			final AReadableFile sourceFile    ,
			final long          sourcePosition,
			final long          length        ,
			final AWritableFile target
		)
		{
			this.validateHandledReadableFile(sourceFile);

			return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), sourcePosition, length, target);
		}

//		@Override
//		public long copyTo(
//			final AReadableFile sourceFile    ,
//			final AWritableFile target        ,
//			final long          targetPosition
//		)
//		{
//			if(this.isHandledReadableFile(sourceFile))
//			{
//				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), target, targetPosition);
//			}
//
//			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
//		}
//
//		@Override
//		public long copyTo(
//			final AReadableFile sourceFile    ,
//			final AWritableFile target        ,
//			final long          targetPosition,
//			final long          length
//		)
//		{
//			if(this.isHandledReadableFile(sourceFile))
//			{
//				return this.specificCopyTo(this.typeReadableFile.cast(sourceFile), target, targetPosition, length);
//			}
//
//			throw this.createUnhandledTypeExceptionReadableFile(sourceFile);
//		}

		@Override
		public long writeBytes(
			final AWritableFile                  targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			this.validateHandledWritableFile(targetFile);
			
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

		/* (28.05.2020 TM)TODO: priv#49: call moveFile
		 * But NOT automatic unregister on the abstract level, because ... it's abstract!
		 */
		@Override
		public void moveFile(
			final AWritableFile sourceFile,
			final AWritableFile targetFile
		)
		{
			this.validateHandledWritableFile(sourceFile);
			
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
		}

		@Override
		public boolean deleteFile(final AWritableFile file)
		{
			this.validateHandledWritableFile(file);
			
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
		

		@Override
		public void truncate(final AWritableFile file, final long newSize)
		{
			this.validateHandledWritableFile(file);
			
			file.iterateObservers(o ->
				o.onBeforeFileTruncation(file, newSize)
			);
			
			this.specificTruncateFile(this.typeWritableFile.cast(file), newSize);
	
			file.iterateObservers(o ->
				o.onAfterFileTruncation(file, newSize)
			);
		}
		
	}
	
}
