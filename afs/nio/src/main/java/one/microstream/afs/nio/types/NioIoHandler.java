package one.microstream.afs.nio.types;

/*-
 * #%L
 * microstream-afs-nio
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AIoHandler;
import one.microstream.afs.types.AItem;
import one.microstream.afs.types.AReadableFile;
import one.microstream.afs.types.AWritableFile;
import one.microstream.afs.types.WriteController;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.BufferProvider;
import one.microstream.io.XIO;


public interface NioIoHandler extends AIoHandler
{
	public NioReadableFile castReadableFile(AReadableFile file);
	
	public NioWritableFile castWritableFile(AWritableFile file);
	
	public Path toPath(final AItem item);
	
	public Path toPath(final String... pathElements);
	
	public static NioIoHandler New()
	{
		return New(WriteController.Enabled());
	}
	
	public static NioIoHandler New(final WriteController writeController)
	{
		return new NioIoHandler.Default(
			notNull(writeController),
			NioPathResolver.New()
		);
	}
	
	public static NioIoHandler New(final NioPathResolver pathResolver)
	{
		return new NioIoHandler.Default(
			WriteController.Enabled(),
			pathResolver
		);
	}
	
	public static NioIoHandler New(
		final WriteController writeController,
		final NioPathResolver pathResolver
	)
	{
		return new NioIoHandler.Default(
			notNull(writeController),
			notNull(pathResolver)
		);
	}
	
	public final class Default
	extends AIoHandler.Abstract<Path, Path, NioItemWrapper, NioFileWrapper, ADirectory, NioReadableFile, NioWritableFile>
	implements NioIoHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final NioPathResolver pathResolver;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final WriteController writeController,
			final NioPathResolver pathResolver)
		{
			super(
				writeController,
				NioItemWrapper.class,
				NioFileWrapper.class,
				ADirectory.class,
				NioReadableFile.class,
				NioWritableFile.class
			);
			
			this.pathResolver = pathResolver;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public Path toPath(final AItem item)
		{
			if(item instanceof NioItemWrapper)
			{
				return ((NioItemWrapper)item).path();
			}
			
			return this.pathResolver.resolvePath(item.toPath());
		}
		
		@Override
		public Path toPath(final String... pathElements)
		{
			return this.pathResolver.resolvePath(pathElements);
		}
						
		@Override
		public NioReadableFile castReadableFile(final AReadableFile file)
		{
			return super.castReadableFile(file);
		}
		
		@Override
		public NioWritableFile castWritableFile(final AWritableFile file)
		{
			return super.castWritableFile(file);
		}
		
		@Override
		protected Path toSubjectFile(final AFile file)
		{
			return this.toPath(file.toPath());
		}
		
		@Override
		protected Path toSubjectDirectory(final ADirectory directory)
		{
			return this.toPath(directory.toPath());
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
		protected XGettingEnum<String> specificListItems(final ADirectory parent)
		{
			final Path dirPath = this.toSubjectDirectory(parent);
			
			final EqHashEnum<String> files = EqHashEnum.New();
			XIO.unchecked.listEntries(dirPath,
				p ->
				{
					files.add(XIO.getFileName(p));
				}
			);
			
			return files;
		}
		
		@Override
		protected XGettingEnum<String> specificListDirectories(final ADirectory parent)
		{
			final Path dirPath = this.toSubjectDirectory(parent);
			
			final EqHashEnum<String> files = EqHashEnum.New();
			XIO.unchecked.listEntries(dirPath,
				p ->
					files.add(XIO.getFileName(p)),
				p ->
					Files.isDirectory(p)
					
			);
			
			return files;
		}
		
		@Override
		protected XGettingEnum<String> specificListFiles(final ADirectory parent)
		{
			final Path dirPath = this.toSubjectDirectory(parent);
			
			final EqHashEnum<String> files = EqHashEnum.New();
			XIO.unchecked.listEntries(dirPath,
				p ->
					files.add(XIO.getFileName(p)),
				p ->
					!Files.isDirectory(p)
			);
			
			return files;
		}
		
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
		protected boolean specificIsEmpty(final ADirectory directory) 
		{		
			return XIO.unchecked.hasNoFiles(this.toSubjectDirectory(directory));			
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
			final Path dir = this.toPath(directory);
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
		protected void specificTruncateFile(
			final NioWritableFile targetFile,
			final long            newSize
		)
		{
			// ensure file is opened for writing
			this.openWriting(targetFile);

			try
			{
				XIO.truncate(targetFile.fileChannel(), newSize);
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
			final NioReadableFile sourceSubject,
			final AWritableFile   target
		)
		{
			final NioWritableFile handlableTarget = this.castWritableFile(target);
			
			try
			{
				return XIO.copyFile(
					sourceSubject.ensureOpenChannel(),
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
			final NioReadableFile sourceSubject ,
			final long            sourcePosition,
			final AWritableFile   target
		)
		{
			final NioWritableFile handlableTarget = this.castWritableFile(target);
			
			try
			{
				return XIO.copyFile(
					sourceSubject.ensureOpenChannel(),
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
			final NioReadableFile sourceSubject ,
			final long            sourcePosition,
			final long            length        ,
			final AWritableFile   target
		)
		{
			final NioWritableFile handlableTarget = this.castWritableFile(target);
			
			try
			{
				return XIO.copyFile(
					sourceSubject.ensureOpenChannel(),
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
		
		@Override
		protected long specificCopyFrom(
			final AReadableFile   source       ,
			final NioWritableFile targetSubject
		)
		{
			final NioReadableFile handlableSource = this.castReadableFile(source);
			
			try
			{
				return XIO.copyFile(
					handlableSource.ensureOpenChannel(),
					targetSubject.ensureOpenChannel()
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected long specificCopyFrom(
			final AReadableFile   source        ,
			final long            sourcePosition,
			final NioWritableFile targetSubject
		)
		{
			final NioReadableFile handlableSource = this.castReadableFile(source);
			
			try
			{
				return XIO.copyFile(
					handlableSource.ensureOpenChannel(),
					sourcePosition,
					targetSubject.ensureOpenChannel()
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected long specificCopyFrom(
			final AReadableFile   source        ,
			final long            sourcePosition,
			final long            length        ,
			final NioWritableFile targetSubject
		)
		{
			final NioReadableFile handlableSource = this.castReadableFile(source);
			
			try
			{
				return XIO.copyFile(
					handlableSource.ensureOpenChannel(),
					sourcePosition,
					length,
					targetSubject.ensureOpenChannel()
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
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
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected void specificMoveFile(
			final NioWritableFile sourceFile,
			final AWritableFile   targetFile
		)
		{
			if(this.isHandledFile(targetFile))
			{
				final NioWritableFile handlableTarget = this.castWritableFile(targetFile);
				this.specificTargetMoveFile(sourceFile, handlableTarget);
				
				return;
			}
			
			this.specificCopyTo(sourceFile, targetFile);
			this.specificDeleteFile(sourceFile);
		}
		

		
		protected void specificTargetMoveFile(
			final NioWritableFile sourceFile,
			final NioWritableFile targetFile
		)
		{
			try
			{
				XIO.move(
					sourceFile.path(),
					targetFile.path()
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
	}
	
}
