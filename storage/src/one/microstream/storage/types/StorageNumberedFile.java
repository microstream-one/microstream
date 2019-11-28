package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

import one.microstream.io.XIO;
import one.microstream.storage.exceptions.StorageExceptionIo;

public interface StorageNumberedFile extends StorageChannelFile
{
	public long number();
		
	public StorageInventoryFile inventorize();
	

	
	public static int orderByNumber(
		final StorageNumberedFile file1,
		final StorageNumberedFile file2
	)
	{
		return Long.compare(file1.number(), file2.number());
	}
	
	
	
	public static StorageNumberedFile New(final int channelIndex, final long number, final Path file)
	{
		return new StorageNumberedFile.Default(
			channelIndex ,
			number       ,
			notNull(file)
		);
	}
	
	public final class Default implements StorageNumberedFile
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final int      channelIndex;
		final long     number      ;
		final Path     file        ;
	          FileLock lock        ;

	          

		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final int channelIndex, final long number, final Path file)
		{
			super();
			this.channelIndex = channelIndex;
			this.number       = number      ;
			this.file         = file        ;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private boolean hasLock()
		{
			return this.lock != null;
		}

		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}
		
		@Override
		public final long number()
		{
			return this.number;
		}
		
		public final Path file()
		{
			return this.file;
		}
		
		@Override
		public final String qualifier()
		{
			return XIO.getFilePath(this.file.getParent());
		}
		
		@Override
		public final String identifier()
		{
			return XIO.getFilePath(this.file);
		}
		
		@Override
		public final String name()
		{
			return XIO.getFileName(this.file);
		}
		
		@Override
		public final boolean delete()
		{
			return XIO.unchecked.delete(this.file);
		}
		
		@Override
		public final boolean exists()
		{
			return XIO.unchecked.exists(this.file);
		}
		
		@Override
		public final StorageInventoryFile inventorize()
		{
			return StorageInventoryFile.New(this.channelIndex, this.number, this.file);
		}
		
		public final FileLock lock()
		{
			if(!this.hasLock())
			{
				this.lock = StorageLockedFile.openLockedFileChannel(this.file);
			}
			
			return this.lock;
		}
		
		@Override
		public final FileChannel fileChannel()
		{
			return this.lock().channel();
		}
		
		@Override
		public final long length()
		{
			// the slow File#length must be used because the channel might not be open. But with a proper check, first.
			if(!this.exists())
			{
				// (19.02.2019 TM)EXCP: proper exception
				throw new RuntimeException();
			}
			
			return XIO.unchecked.size(this.file);
		}
		
		@Override
		public final boolean isOpen()
		{
			return this.hasLock() && StorageNumberedFile.super.isOpen();
		}
		
		@Override
		public final StorageFile flush()
		{
			if(!this.hasLock())
			{
				return this;
			}
			
			return StorageNumberedFile.super.flush();
		}

		@Override
		public final void close()
		{
			if(!this.hasLock())
			{
				return;
			}
			
			try
			{
				@SuppressWarnings("resource") // moronic warning suppressed
				final FileChannel fileChannel = this.lock.channel();
				this.lock.release();
				fileChannel.close();
				this.lock = null;
			}
			catch(final IOException e)
			{
				throw new StorageExceptionIo(e);
			}
		}

	}
	
}
