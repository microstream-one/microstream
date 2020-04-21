package one.microstream.afs.local;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableDirectory;
import one.microstream.afs.AWritableFile;
import one.microstream.afs.AWritableFile.Abstract;
import one.microstream.storage.io.ProtageNioChannelFile;
import one.microstream.storage.io.ProtageNioChannelWritableFile;


public interface LocalFile extends ProtageNioChannelFile
{
	@Override
	public LocalDirectory directory();
	
	public File file();
	
	@Override
	public FileChannel channel();
	
	
	
	// there is no publicly accessible constructor. Only directories can create file instances.
		
	public final class Default extends AWritableFile.Abstract<LocalDirectory>
	implements LocalFile
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
			final LocalDirectory directory,
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
			
			this.lock    = LocalFileSystem.openFileChannel(this.file);
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
		public void copyTo(final AWritableFile target, final long sourcePosition, final long length)
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
				throw new RuntimeException(e); // (01.10.2014 TM)EXCP: proper exception
			}
		}
		

		private void synchMoveTo(final LocalDirectory destination)
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
		
		private synchronized AWritableFile internalMoveTo(final AWritableDirectory destination)
		{
			if(this.directory() == destination || this.directory().path().equals(destination.path()))
			{
				// (27.10.2018 TM)EXCP: proper exception
				throw new RuntimeException(
					"Move destination is identical to the current destination: " + destination.path()
				);
			}
			
			final AWritableFile existingTargetFile = destination.files().get(this.name());
			if(existingTargetFile != null)
			{
				// (27.10.2018 TM)EXCP: proper exception
				throw new RuntimeException(
					"Move action target file already exists: " + destination.path()
					+ ": " + existingTargetFile.name()
				);
			}

			// no need to lock the target file since it has just been created from the lock-secured destination.
			final AWritableFile targetFile = destination.createFile(this.name());
			
			if(destination instanceof LocalDirectory)
			{
				// optimization for filesystem-to-filesystem move
				this.synchMoveTo((LocalDirectory)destination);
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
		public AWritableFile moveTo(final AWritableDirectory destination)
		{
			// no locking since the called method does the locking, but in a deadlock-free fashion.
			return ADirectory.executeLocked(this.directory(), destination, () ->
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
				throw new RuntimeException(e); // (01.10.2014 TM)EXCP: proper exception
			}
		}
		
		@Override
		public final synchronized <C extends Consumer<? super AReadableFile>> C waitOnClose(final C callback)
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
		public final synchronized <C extends Consumer<? super AWritableFile>> C waitOnDelete(final C callback)
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
