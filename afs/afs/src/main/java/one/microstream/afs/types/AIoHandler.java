package one.microstream.afs.types;

/*-
 * #%L
 * microstream-afs
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.afs.exceptions.AfsExceptionConsistency;
import one.microstream.chars.XChars;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.io.BufferProvider;
import one.microstream.memory.XMemory;
import one.microstream.util.UtilStackTrace;


public interface AIoHandler extends WriteController
{
	/* (08.06.2020 TM)TODO: priv#49: JavaDoc: guaranteed completeness
	 * guarantees that all specified bytes (where applicable) are read/written.
	 * Any iterative, reattempting, waiting, timeouting etc. logic that is required for this must be covered
	 * by the IoHandler implementation.
	 */
	
	/* (16.07.2020 TM)TODO: priv#49: composite IoHandler
	 * The default implementation is rather strict on its handleable file and directory implementations.
	 * This is intentional.
	 * If a more flexible or dynamic/heuristic approach is desired, it has to be implemented as another IoHandler.
	 * For example a "composite" IoHandler that looks up the specific IoHandler depending on the implementation of
	 * a passed file or directory.
	 */
	
	/* (16.07.2020 TM)TODO: priv#49: target positional parameters
	 * So far, variants for target positional writing (including copying) is missing completely.
	 * There is a way to provide a source position, but the target can only be written to
	 * in the way the implementation has set the default behavior (always append or always start at 0).
	 * This matches the microstream strategy, but in general, it must be possible to write to a
	 * specified position in the target file.
	 * Simple example: a utility that replaces found byte ranges with a new value (search & replace).
	 * Currently, such a tricial functionality is not possible with AFS.
	 */
	
	public boolean isHandledItem(AItem item);
	
	public boolean isHandledFile(AFile file);
	
	public boolean isHandledDirectory(ADirectory directory);
	
	public boolean isHandledReadableFile(AReadableFile file);
	
	public boolean isHandledWritableFile(AWritableFile file);
	
	public void validateHandledFile(AFile file);
	
	public void validateHandledDirectory(ADirectory directory);
	
	public void validateHandledReadableFile(AReadableFile file);
	
	public void validateHandledWritableFile(AWritableFile file);
	
	public long size(AFile file);

	public boolean exists(AFile file);
	
	public boolean exists(ADirectory directory);

	public void create(ADirectory directory);

	public void create(AWritableFile file);
	
	public boolean ensureExists(ADirectory directory);

	public boolean ensureExists(AWritableFile file);
	
	public void inventorize(ADirectory directory);
	

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
		
	
	public long copyTo(AReadableFile sourceSubject, AWritableFile target);
	
	public long copyTo(AReadableFile sourceSubject, long sourcePosition, AWritableFile target);
	
	public long copyTo(AReadableFile sourceSubject, long sourcePosition, long length, AWritableFile target);
	
//	public long copyTo(AReadableFile sourceSubject, AWritableFile target, long targetPosition);
//
//	public long copyTo(AReadableFile sourceSubject, AWritableFile target, long targetPosition, long length);
//
//	public long copyTo(AReadableFile sourceSubject, long srcPos, AWritableFile target, long trgPos, long length);
	
	public long copyFrom(AReadableFile source, AWritableFile targetSubject);
	
	public long copyFrom(AReadableFile source, long sourcePosition, AWritableFile targetSubject);
	
	public long copyFrom(AReadableFile source, long sourcePosition, long length, AWritableFile targetSubject);
	
//	public long copyFrom(AReadableFile source, AWritableFile targetSubject, long targetPosition);
//
//	public long copyFrom(AReadableFile source, AWritableFile targetSubject, long targetPosition, long length);
//
//	public long copyFrom(AReadableFile source, long srcPos, AWritableFile targetSubject, long trgPos, long length);
		
	
	public long writeBytes(AWritableFile targetFile, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(AWritableFile sourceFile, AWritableFile targetFile);
	
	public boolean deleteFile(AWritableFile file);
	
	public void truncate(AWritableFile file, long newSize);

	public XGettingEnum<String> listItems(ADirectory parent);
	
	public XGettingEnum<String> listDirectories(ADirectory parent);
	
	public XGettingEnum<String> listFiles(ADirectory parent);
		
	public boolean isEmpty(ADirectory directory);
	
	
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
		
		private final WriteController writeController;
		
		private final Class<I> typeItem        ;
		private final Class<F> typeFile        ;
		private final Class<D> typeDirectory   ;
		private final Class<R> typeReadableFile;
		private final Class<W> typeWritableFile;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final WriteController writeController ,
			final Class<I>        typeItem        ,
			final Class<F>        typeFile        ,
			final Class<D>        typeDirectory   ,
			final Class<R>        typeReadableFile,
			final Class<W>        typeWritableFile
		)
		{
			super();
			this.writeController  = notNull(writeController) ;
			this.typeItem         = notNull(typeItem)        ;
			this.typeFile         = notNull(typeFile)        ;
			this.typeDirectory    = notNull(typeDirectory)   ;
			this.typeReadableFile = notNull(typeReadableFile);
			this.typeWritableFile = notNull(typeWritableFile);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void validateIsWritable()
		{
			this.writeController.validateIsWritable();
		}
		
		@Override
		public final boolean isWritable()
		{
			return this.writeController.isWritable();
		}
		
		protected abstract long specificSize(F file);

		protected abstract boolean specificExists(F file);

		protected abstract boolean specificExists(D directory);
		
		protected abstract XGettingEnum<String> specificListItems(D parent);
		
		protected abstract XGettingEnum<String> specificListDirectories(D parent);
		
		protected abstract XGettingEnum<String> specificListFiles(D parent);

		protected abstract void specificInventorize(D directory);
		
		protected abstract boolean specificIsEmpty(D directory);

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
			
		
		protected abstract long specificCopyTo(R sourceSubject, AWritableFile target);
		
		protected abstract long specificCopyTo(R sourceSubject, long sourcePosition, AWritableFile target);
		
		protected abstract long specificCopyTo(R sourceSubject, long sourcePosition, long length, AWritableFile target);
		
//		protected abstract long specificCopyTo(R sourceSubject, AWritableFile target, long targetPosition);
//
//		protected abstract long specificCopyTo(R sourceSubject, AWritableFile target, long targetPosition, long length);
//
//		protected abstract long specificCopyTo(R sourceSubject, long srcPos, AWritableFile target, long trgPos, long length);
		
		
		protected abstract long specificCopyFrom(AReadableFile source, W targetSubject);
		
		protected abstract long specificCopyFrom(AReadableFile source, long sourcePosition, W targetSubject);
		
		protected abstract long specificCopyFrom(AReadableFile source, long sourcePosition, long length, W targetSubject);
		
//		protected abstract long specificCopyFrom(AReadableFile source, W targetSubject, long targetPosition);
//
//		protected abstract long specificCopyFrom(AReadableFile source, W targetSubject, long targetPosition, long length);
//
//		protected abstract long specificCopyFrom(AReadableFile source, long srcPos, W targetSubject, long trgPos, long length);
		
		
		protected abstract long specificWriteBytes(W targetFile, Iterable<? extends ByteBuffer> sourceBuffers);

		protected abstract void specificMoveFile(W sourceFile, AWritableFile targetFile);
		
		protected abstract boolean specificDeleteFile(W file);
		
		protected abstract void specificTruncateFile(W file, long newSize);
		
		protected long copyGeneric(
			final AReadableFile source,
			final AWritableFile target
		)
		{
			final long sourceSize = source.size(); // explicit variable for debugging purposes
			
			final ByteBuffer dbb  = XMemory.allocateDirectNative(sourceSize);
			
			try
			{
				source.readBytes(dbb);
				dbb.flip();
				target.writeBytes(dbb);
			}
			finally
			{
				XMemory.deallocateDirectByteBuffer(dbb);
			}
			
			return sourceSize;
		}
		
		protected long copyGeneric(
			final AReadableFile source        ,
			final long          sourcePosition,
			final AWritableFile target
		)
		{
			final long sourceSize = source.size(); // explicit variable for debugging purposes
			final long length     = sourceSize - sourcePosition;
			X.validateRange(sourceSize, sourcePosition, length);
			
			final ByteBuffer dbb  = XMemory.allocateDirectNative(length);
			
			try
			{
				source.readBytes(dbb, sourcePosition);
				dbb.flip();
				target.writeBytes(dbb);
			}
			finally
			{
				XMemory.deallocateDirectByteBuffer(dbb);
			}
			
			return length;
		}
		
		protected long copyGeneric(
			final AReadableFile source        ,
			final long          sourcePosition,
			final AWritableFile target        ,
			final long          length
		)
		{
			final long sourceSize = source.size(); // explicit variable for debugging purposes
			X.validateRange(sourceSize, sourcePosition, length);
			
			final ByteBuffer dbb = XMemory.allocateDirectNative(length);
			
			try
			{
				source.readBytes(dbb, sourcePosition);
				dbb.flip();
				target.writeBytes(dbb);
			}
			finally
			{
				XMemory.deallocateDirectByteBuffer(dbb);
			}
			
			return length;
		}
		
		
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
			return UtilStackTrace.cutStacktraceByN(new AfsExceptionConsistency(
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
		
		@Override
		public boolean isHandledItem(final AItem item)
		{
			return this.typeItem.isInstance(item);
		}
		
		@Override
		public boolean isHandledFile(final AFile file)
		{
			return this.typeFile.isInstance(file);
		}
		
		@Override
		public boolean isHandledDirectory(final ADirectory directory)
		{
			return this.typeDirectory.isInstance(directory);
		}
		
		@Override
		public boolean isHandledReadableFile(final AReadableFile file)
		{
			return this.typeReadableFile.isInstance(file);
		}
		
		@Override
		public boolean isHandledWritableFile(final AWritableFile file)
		{
			return this.typeWritableFile.isInstance(file);
		}
		
		@Override
		public final void validateHandledFile(final AFile file)
		{
			if(this.isHandledFile(file))
			{
				return;
			}
			
			throw this.createUnhandledTypeExceptionFile(file);
		}
		
		@Override
		public final void validateHandledDirectory(final ADirectory directory)
		{
			if(this.isHandledDirectory(directory))
			{
				return;
			}
			
			throw this.createUnhandledTypeExceptionDirectory(directory);
		}
		
		@Override
		public final void validateHandledReadableFile(final AReadableFile file)
		{
			if(this.isHandledReadableFile(file))
			{
				return;
			}
			
			throw this.createUnhandledTypeExceptionReadableFile(file);
		}
		
		@Override
		public final void validateHandledWritableFile(final AWritableFile file)
		{
			if(this.isHandledWritableFile(file))
			{
				return;
			}
			
			throw this.createUnhandledTypeExceptionWritableFile(file);
		}
		
		protected R castReadableFile(final AReadableFile file)
		{
			this.validateHandledReadableFile(file);
			
			return this.typeReadableFile.cast(file);
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
			// required to query the size of a general AFile instance
			if(!this.isHandledFile(file))
			{
				return this.subjectFileSize(this.toSubjectFile(file));
			}

			return this.specificSize(this.typeFile.cast(file));
		}

		@Override
		public boolean exists(final AFile file)
		{
			// required to check the existence of a general AFile instance
			if(!this.isHandledFile(file))
			{
				return this.subjectFileExists(this.toSubjectFile(file));
			}

			return this.specificExists(this.typeFile.cast(file));
		}

		@Override
		public boolean exists(final ADirectory directory)
		{
			if(directory.parent() == null && directory.identifier().isEmpty())
			{
				// an empty-named root directory (= leading separator) is assumed implicitely and cannot be queried.
				return true;
			}
			
			// required to check the existence of a general ADirectory instance
			if(!this.isHandledDirectory(directory))
			{
				return this.subjectDirectoryExists(this.toSubjectDirectory(directory));
			}

			return this.specificExists(this.typeDirectory.cast(directory));
		}
		
		@Override
		public XGettingEnum<String> listItems(final ADirectory parent)
		{
			this.validateHandledDirectory(parent);
			
			return this.specificListItems(this.typeDirectory.cast(parent));
		}
		
		@Override
		public XGettingEnum<String> listDirectories(final ADirectory parent)
		{
			this.validateHandledDirectory(parent);
			
			return this.specificListDirectories(this.typeDirectory.cast(parent));
		}
		
		@Override
		public XGettingEnum<String> listFiles(final ADirectory parent)
		{
			this.validateHandledDirectory(parent);
			
			return this.specificListFiles(this.typeDirectory.cast(parent));
		}
		
		@Override
		public void inventorize(final ADirectory directory)
		{
			this.validateHandledDirectory(directory);
			
			this.specificInventorize(this.typeDirectory.cast(directory));
		}
		
		@Override
		public boolean isEmpty(final ADirectory directory)
		{
			this.validateHandledDirectory(directory);
			
			return this.specificIsEmpty(this.typeDirectory.cast(directory));
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
			this.validateIsWritable();

			return this.specificOpenWriting(this.typeWritableFile.cast(file));
		}
		
		@Override
		public void create(final ADirectory directory)
		{
			this.validateHandledDirectory(directory);
						
			synchronized(ADirectory.actual(directory))
			{
				this.validateIsWritable();
				
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
			
			final ADirectory parent = ADirectory.actual(file.parent());
			
			this.ensureExists(parent);
			
			synchronized(parent)
			{
				this.validateIsWritable();
				
				file.parent().iterateObservers(o ->
					o.onBeforeFileCreate(file)
				);
				
				synchronized(file.actual())
				{
					file.iterateObservers(o ->
						o.onBeforeFileCreate(file)
					);
					
					this.specificCreate(this.typeWritableFile.cast(file));
					
					file.iterateObservers(o ->
						o.onAfterFileCreate(file)
					);
				}
				
				file.parent().iterateObservers(o ->
					o.onAfterFileCreate(file)
				);
			}
		}
		
		@Override
		public boolean ensureExists(final ADirectory directory)
		{
			synchronized(ADirectory.actual(directory))
			{
				if(this.exists(directory))
				{
					return false;
				}
				
				this.create(directory);
			}
			
			return true;
		}

		@Override
		public boolean ensureExists(final AWritableFile file)
		{
			synchronized(file.actual())
			{
				if(this.exists(file))
				{
					return false;
				}
				
				this.create(file);
			}
			
			return true;
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
			final AReadableFile sourceSubject,
			final AWritableFile target
		)
		{
			if(sourceSubject.fileSystem() != target.fileSystem()
			|| !this.isHandledReadableFile(sourceSubject)
			|| !this.isHandledWritableFile(target)
			)
			{
				// it is by far the most common and intuitive case for copy to ensure existence implicitely
				target.ensureExists();
				
				return this.copyGeneric(sourceSubject, target);
			}
			
			this.validateHandledReadableFile(sourceSubject);

			// it is by far the most common and intuitive case for copy to ensure existence implicitely
			target.ensureExists();
			
			return this.specificCopyTo(this.typeReadableFile.cast(sourceSubject), target);
		}
		
		@Override
		public long copyTo(
			final AReadableFile sourceSubject ,
			final long          sourcePosition,
			final AWritableFile target
		)
		{
			if(sourceSubject.fileSystem() != target.fileSystem()
			|| !this.isHandledReadableFile(sourceSubject)
			|| !this.isHandledWritableFile(target)
			)
			{
				// it is by far the most common and intuitive case for copy to ensure existence implicitely
				target.ensureExists();
				
				return this.copyGeneric(sourceSubject, sourcePosition, target);
			}
			
			this.validateHandledReadableFile(sourceSubject);

			// it is by far the most common and intuitive case for copy to ensure existence implicitely
			target.ensureExists();

			this.validateIsWritable();
			
			return this.specificCopyTo(this.typeReadableFile.cast(sourceSubject), sourcePosition, target);
		}
		
		@Override
		public long copyTo(
			final AReadableFile sourceSubject ,
			final long          sourcePosition,
			final long          length        ,
			final AWritableFile target
		)
		{
			if(length == 0L)
			{
				// no-op
				return 0L;
			}
			
			if(sourceSubject.fileSystem() != target.fileSystem()
			|| !this.isHandledReadableFile(sourceSubject)
			|| !this.isHandledWritableFile(target)
			)
			{
				// it is by far the most common and intuitive case for copy to ensure existence implicitely
				target.ensureExists();
				
				return this.copyGeneric(sourceSubject, sourcePosition, target, length);
			}
			
			this.validateHandledReadableFile(sourceSubject);

			// it is by far the most common and intuitive case for copy to ensure existence implicitely
			target.ensureExists();

			this.validateIsWritable();
			
			return this.specificCopyTo(this.typeReadableFile.cast(sourceSubject), sourcePosition, length, target);
		}

//		@Override
//		public long copyTo(
//			final AReadableFile sourceSubject ,
//			final AWritableFile target        ,
//			final long          targetPosition
//		)
//		{
//			this.validateHandledReadableFile(sourceSubject);
//
//			// it is by far the most common and intuitive case for copy to ensure existence implicitely
//			target.ensureExists();
//
//			return this.specificCopyTo(this.typeReadableFile.cast(sourceSubject), target, targetPosition);
//		}
//
//		@Override
//		public long copyTo(
//			final AReadableFile sourceSubject ,
//			final AWritableFile target        ,
//			final long          targetPosition,
//			final long          length
//		)
//		{
//			this.validateHandledReadableFile(sourceSubject);
//
//			// it is by far the most common and intuitive case for copy to ensure existence implicitely
//			target.ensureExists();
//
//			this.validateIsWritable();
//
//			return this.specificCopyTo(this.typeReadableFile.cast(sourceSubject), target, targetPosition, length);
//		}
//
//		@Override
//		public long copyTo(
//			final AReadableFile sourceSubject ,
//			final long          sourcePosition,
//			final AWritableFile target        ,
//			final long          targetPosition,
//			final long          length
//		)
//		{
//			this.validateHandledReadableFile(sourceSubject);
//
//			// it is by far the most common and intuitive case for copy to ensure existence implicitely
//			target.ensureExists();
//
//			this.validateIsWritable();
//
//			return this.specificCopyTo(this.typeReadableFile.cast(sourceSubject), sourcePosition, target, targetPosition, length);
//		}
		
		@Override
		public long copyFrom(
			final AReadableFile source       ,
			final AWritableFile targetSubject
		)
		{
			if(source.fileSystem() != targetSubject.fileSystem()
			|| !this.isHandledReadableFile(source)
			|| !this.isHandledWritableFile(targetSubject)
			)
			{
				// it is by far the most common and intuitive case for copy to ensure existence implicitely
				targetSubject.ensureExists();
				
				return this.copyGeneric(source, targetSubject);
			}
			
			this.validateHandledWritableFile(targetSubject);

			// it is by far the most common and intuitive case for copy to ensure existence implicitely
			targetSubject.ensureExists();

			this.validateIsWritable();
			
			return this.specificCopyFrom(source, this.typeWritableFile.cast(targetSubject));
		}
		
		@Override
		public long copyFrom(
			final AReadableFile source        ,
			final long          sourcePosition,
			final AWritableFile targetSubject
		)
		{
			if(source.fileSystem() != targetSubject.fileSystem()
			|| !this.isHandledReadableFile(source)
			|| !this.isHandledWritableFile(targetSubject)
			)
			{
				// it is by far the most common and intuitive case for copy to ensure existence implicitely
				targetSubject.ensureExists();
				
				return this.copyGeneric(source, sourcePosition, targetSubject);
			}
			
			this.validateHandledWritableFile(targetSubject);

			// it is by far the most common and intuitive case for copy to ensure existence implicitely
			targetSubject.ensureExists();

			this.validateIsWritable();
			
			return this.specificCopyFrom(source, sourcePosition, this.typeWritableFile.cast(targetSubject));
		}
		
		@Override
		public long copyFrom(
			final AReadableFile source        ,
			final long          sourcePosition,
			final long          length        ,
			final AWritableFile targetSubject
		)
		{
			if(length == 0L)
			{
				// no-op
				return 0L;
			}
			
			if(source.fileSystem() != targetSubject.fileSystem()
			|| !this.isHandledReadableFile(source)
			|| !this.isHandledWritableFile(targetSubject)
			)
			{
				// it is by far the most common and intuitive case for copy to ensure existence implicitely
				targetSubject.ensureExists();
				
				return this.copyGeneric(source, sourcePosition, targetSubject, length);
			}
			
			this.validateHandledWritableFile(targetSubject);

			// it is by far the most common and intuitive case for copy to ensure existence implicitely
			targetSubject.ensureExists();

			this.validateIsWritable();
			
			return this.specificCopyFrom(source, sourcePosition, length, this.typeWritableFile.cast(targetSubject));
		}

//		@Override
//		public long copyFrom(
//			final AReadableFile source        ,
//			final AWritableFile targetSubject ,
//			final long          targetPosition
//		)
//		{
//			this.validateHandledWritableFile(targetSubject);
//
//			// it is by far the most common and intuitive case for copy to ensure existence implicitely
//			targetSubject.ensureExists();
//
//			this.validateIsWritable();
//
//			return this.specificCopyFrom(source, this.typeWritableFile.cast(targetSubject), targetPosition);
//		}
//
//		@Override
//		public long copyFrom(
//			final AReadableFile source        ,
//			final AWritableFile targetSubject ,
//			final long          targetPosition,
//			final long          length
//		)
//		{
//			this.validateHandledWritableFile(targetSubject);
//
//			// it is by far the most common and intuitive case for copy to ensure existence implicitely
//			targetSubject.ensureExists();
//
//			this.validateIsWritable();
//
//			return this.specificCopyFrom(source, this.typeWritableFile.cast(targetSubject), targetPosition, length);
//		}
//
//		@Override
//		public long copyFrom(
//			final AReadableFile source        ,
//			final long          sourcePosition,
//			final AWritableFile targetSubject ,
//			final long          targetPosition,
//			final long          length
//		)
//		{
//			this.validateHandledWritableFile(targetSubject);
//
//			// it is by far the most common and intuitive case for copy to ensure existence implicitely
//			targetSubject.ensureExists();
//
//			this.validateIsWritable();
//
//			return this.specificCopyFrom(source, sourcePosition, this.typeWritableFile.cast(targetSubject), targetPosition, length);
//		}

		@Override
		public long writeBytes(
			final AWritableFile                  targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			this.validateHandledWritableFile(targetFile);
			
			synchronized(targetFile.actual())
			{
				this.validateIsWritable();
				
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
		}
		
		@Override
		public void moveFile(
			final AWritableFile sourceFile,
			final AWritableFile targetFile
		)
		{
			this.validateHandledWritableFile(sourceFile);

			final ADirectory sourceParent = ADirectory.actual(sourceFile.parent());
			final ADirectory targetParent = ADirectory.actual(targetFile.parent());
			
			/*
			 * intentionally no locking here, since there are TWO sets of parent&file involved,
			 * which could cause deadlocks.
			 */

			this.validateIsWritable();
			
			targetParent.iterateObservers(o ->
			{
				o.onBeforeFileMove(sourceFile, targetFile);
			});
			sourceParent.iterateObservers(o ->
			{
				o.onBeforeFileDelete(sourceFile);
			});
			
			targetFile.iterateObservers(o ->
			{
				o.onBeforeFileMove(sourceFile, targetFile);
			});
			sourceFile.iterateObservers(o ->
			{
				o.onBeforeFileDelete(sourceFile);
			});
			
			this.specificMoveFile(this.typeWritableFile.cast(sourceFile), targetFile);
			
			targetFile.iterateObservers(o ->
			{
				o.onAfterFileMove(sourceFile, targetFile);
			});
			
			sourceFile.iterateObservers(o ->
			{
				o.onAfterFileDelete(sourceFile, true);
			});
			
			targetParent.iterateObservers(o ->
			{
				o.onAfterFileMove(sourceFile, targetFile);
			});
			sourceParent.iterateObservers(o ->
			{
				o.onAfterFileDelete(sourceFile, true);
			});
		}

		@Override
		public boolean deleteFile(final AWritableFile file)
		{
			this.validateHandledWritableFile(file);

			final ADirectory parent = ADirectory.actual(file.parent());

			final boolean result;
			synchronized(parent)
			{
				this.validateIsWritable();
				
				file.parent().iterateObservers(o ->
					o.onBeforeFileDelete(file)
				);
				
				synchronized(file.actual())
				{
					this.validateIsWritable();
					
					file.iterateObservers(o ->
						o.onBeforeFileDelete(file)
					);
					
					result = this.specificDeleteFile(this.typeWritableFile.cast(file));
					if(result)
					{
						file.close();
					}
			
					file.iterateObservers(o ->
						o.onAfterFileDelete(file, result)
					);
				}
				
				file.parent().iterateObservers(o ->
					o.onAfterFileDelete(file, result)
				);
			}
			
			return result;
		}
		

		@Override
		public void truncate(final AWritableFile file, final long newSize)
		{
			this.validateHandledWritableFile(file);
			
			synchronized(file.actual())
			{
				this.validateIsWritable();
				
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
	
}
