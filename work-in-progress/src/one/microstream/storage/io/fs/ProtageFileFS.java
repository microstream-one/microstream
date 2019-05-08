package one.microstream.storage.io.fs;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import one.microstream.storage.io.ProtageDirectory;
import one.microstream.storage.io.ProtageNioChannelFile;
import one.microstream.storage.io.ProtageNioChannelWritableFile;
import one.microstream.storage.io.ProtageReadableFile;
import one.microstream.storage.io.ProtageWritableDirectory;
import one.microstream.storage.io.ProtageWritableFile;


public interface ProtageFileFS extends ProtageNioChannelFile
{
	@Override
	public ProtageDirectoryFS directory();
	
	public File file();
	
	@Override
	public FileChannel channel();
	
	
	
	// there is no publicly accessible constructor. Only directories can create file instances.
		
	public final class Default extends ProtageWritableFile.Abstract<ProtageDirectoryFS>
	implements ProtageFileFS
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final     File        file   ;
		private transient FileLock    lock   ;
		private transient FileChannel channel;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final ProtageDirectoryFS directory,
			final String             name     ,
			final File               file     ,
			final FileLock           lock     ,
			final FileChannel        channel
		)
		{
			super(directory, name);
			this.file    = file   ;
			this.lock    = lock   ;
			this.channel = channel;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final File file()
		{
			return this.file;
		}
		
		public final Path path()
		{
			// (27.10.2018 TM)TODO: OGS-45: also hold or even replace File by Path? (Performance?)
			return this.file.toPath();
		}

		@Override
		public synchronized long length()
		{
			return this.file.length();
		}

		@Override
		public synchronized boolean exists()
		{
			// (29.10.2018 TM)FIXME: OGS-45: conflict with isDeleted flag in super class. Or differentiate better?
			return this.file.exists();
		}
		
		@Override
		public synchronized FileChannel channel()
		{
			return this.channel;
		}

		@Override
		public synchronized void open()
		{
			if(this.isOpen())
			{
				return;
			}
			
			this.lock    = ProtageFileSystem.openFileChannel(this.file);
			this.channel = this.lock.channel();
		}

		@Override
		public synchronized boolean isOpen()
		{
			return this.channel != null;
		}

		@Override
		public synchronized boolean isClosed()
		{
			return this.channel == null;
		}

		@Override
		public synchronized void close()
		{
			if(this.isClosed())
			{
				return;
			}
			
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#close()
		}

		@Override
		public synchronized void delete()
		{
			if(!this.exists())
			{
				return;
			}
			
			this.close();
			
			try
			{
				Files.delete(this.path());
				super.delete();
			}
			catch(final IOException e)
			{
				// (29.10.2018 TM)EXCP: proper exception
				throw new RuntimeException(e);
			}
		}

		@Override
		public synchronized long read(final ByteBuffer target, final long position)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#read()
		}
		
		@Override
		public void copyTo(final ProtageWritableFile target, final long sourcePosition, final long length)
		{
			if(target instanceof ProtageNioChannelWritableFile)
			{
				// (29.10.2018 TM)FIXME: OGS-45: must lock both files in order
				this.synchCopyToNioChannel((ProtageNioChannelWritableFile)target, sourcePosition, length);
				return;
			}
			
			super.copyTo(target, sourcePosition, length);
		}
		
		private void synchCopyToNioChannel(
			final ProtageNioChannelWritableFile target        ,
			final long                      sourcePosition,
			final long                      length
		)
		{
			try
			{
				// (28.10.2018 TM)TODO: Storage copy: what about incomplete writes? Loop?
				this.channel.transferTo(sourcePosition, length, target.channel());
			}
			catch(final IOException e)
			{
				throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
			}
		}
		

		private void synchMoveTo(final ProtageDirectoryFS destination)
		{
			final Path destDir  = destination.directory().toPath();
			final Path destFile = destDir.resolve(this.name());
			
			try
			{
				Files.move(this.path(), destFile);
			}
			catch(final IOException e)
			{
				// (27.10.2018 TM)EXCP: proper exception
				throw new RuntimeException(e);
			}
		}
		
		private synchronized ProtageWritableFile internalMoveTo(final ProtageWritableDirectory destination)
		{
			if(this.directory() == destination || this.directory().identifier().equals(destination.identifier()))
			{
				// (27.10.2018 TM)EXCP: proper exception
				throw new RuntimeException(
					"Move destination is identical to the current destination: " + destination.identifier()
				);
			}
			
			final ProtageWritableFile existingTargetFile = destination.files().get(this.name());
			if(existingTargetFile != null)
			{
				// (27.10.2018 TM)EXCP: proper exception
				throw new RuntimeException(
					"Move action target file already exists: " + destination.identifier()
					+ ": " + existingTargetFile.name()
				);
			}

			// no need to lock the target file since it has just been created from the lock-secured destination.
			final ProtageWritableFile targetFile = destination.createFile(this.name());
			
			if(destination instanceof ProtageDirectoryFS)
			{
				// optimization for filesystem-to-filesystem move
				this.synchMoveTo((ProtageDirectoryFS)destination);
			}
			else
			{
				this.copyTo(targetFile);
			}

			// delete must be called in both cases to update the storage.io meta structures
			this.delete();
			
			return targetFile;
		}

		@Override
		public ProtageWritableFile moveTo(final ProtageWritableDirectory destination)
		{
			// no locking since the called method does the locking, but in a deadlock-free fashion.
			return ProtageDirectory.executeLocked(this.directory(), destination, () ->
				this.internalMoveTo(destination)
			);
		}
		
		@Override
		public synchronized long write(final Iterable<? extends ByteBuffer> sources)
		{
			try
			{
				final FileChannel channel = this.channel;
				
				long totalBytesWritten = 0;
				for(final ByteBuffer source : sources)
				{
					while(source.hasRemaining())
					{
						totalBytesWritten += channel.write(source);
					}
//					channel.force(false); // (12.02.2015 TM)NOTE: replaced by explicit flush() calls on all usesites
				}
				
				return totalBytesWritten;
				
			}
			catch(final IOException e)
			{
				throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
			}
		}
		
		@Override
		public final synchronized <C extends Consumer<? super ProtageReadableFile>> C waitOnClose(final C callback)
		{
			while(!this.isClosed())
			{
				try
				{
					this.wait();
				}
				catch (final InterruptedException e)
				{
					// aborted, so return without executing the callback logic
					return callback;
				}
			}
			
			// callback logic is executed while still holding the lock on this file instance
			callback.accept(this);
			
			return callback;
		}
		
		@Override
		public final synchronized <C extends Consumer<? super ProtageWritableFile>> C waitOnDelete(final C callback)
		{
			while(!this.isDeleted())
			{
				try
				{
					this.wait();
				}
				catch (final InterruptedException e)
				{
					// aborted, so return without executing the callback logic
					return callback;
				}
			}
			
			// callback logic is executed while still holding the lock on this file instance
			callback.accept(this);
			
			return callback;
		}
	}

}
