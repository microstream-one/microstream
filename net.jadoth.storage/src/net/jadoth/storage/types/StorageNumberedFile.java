package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

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
	
	
	
	public static StorageNumberedFile New(final int channelIndex, final long number, final File file)
	{
		return new StorageNumberedFile.Implementation(
			channelIndex ,
			number       ,
			notNull(file)
		);
	}
	
	public final class Implementation implements StorageNumberedFile
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final int      channelIndex;
		final long     number      ;
		final File     file        ;
	          FileLock lock        ;


		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final int channelIndex, final long number, final File file)
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
		
		public final File file()
		{
			return this.file;
		}
		
		@Override
		public final String qualifier()
		{
			return this.file.getParent();
		}
		
		@Override
		public final String identifier()
		{
			return this.file.getPath();
		}
		
		@Override
		public final String name()
		{
			return this.file.getName();
		}
		
		@Override
		public final boolean delete()
		{
			return this.file.delete();
		}
		
		@Override
		public final boolean exists()
		{
			return this.file.exists();
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
		public final FileChannel channel()
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
			
			return this.file.length();
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
			
			StorageNumberedFile.super.close();
		}

	}
	
}
