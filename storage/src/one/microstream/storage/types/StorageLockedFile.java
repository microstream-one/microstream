package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import one.microstream.files.XFiles;


public interface StorageLockedFile extends StorageFile //, AutoCloseable
{
	// can't implement AutoCloseable because the naive resource warnings are idiotic.
	
	@Override
	public default long length()
	{
		try
		{
			return this.fileChannel().size();
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (08.12.2014)EXCP: proper exception
		}
	}
	
	public int incrementUserCount();

	public boolean decrementUserCount();

	public boolean hasNoUsers();
	

	@SuppressWarnings("resource") // resource closed internally by FileChannel (JDK tricking Java compiler ^^)
	public static FileLock openLockedFileChannel(final File file)
	{
		// the file is always completely and unshared locked.
		final FileLock lock;
		FileChannel channel = null;
		try
		{
			// resource closed internally by FileChannel (JDK tricking Java compiler ^^)
			channel = new RandomAccessFile(file, "rw").getChannel();

			/*
			 * Tests showed that Java file locks even on Windows don't work properly:
			 * They only prevent other Java processes from acquiring another lock.
			 * But other applications (e.g. Hexeditor) can still open and write to the file.
			 * This basically makes any attempt to secure the file useless.
			 * Not only on linux which seems to be complete crap when it comes to locking files,
			 * but also on windows.
			 * As there is no alternative available and it at least works within Java, it is kept nevertheless.
			 */
			lock = channel.tryLock();
			channel.position(channel.size());
			if(lock == null)
			{
				throw new RuntimeException("File seems to be already locked: " + file);
			}
		}
		catch(final Exception e)
		{
			XFiles.closeSilent(channel);
			// (28.06.2014)EXCP: proper exception
			throw new RuntimeException("Cannot obtain lock for file " + file, e);
		}

		return lock;
	}

	public static StorageLockedFile openLockedFile(final File file)
	{
		return StorageLockedFile.New(file, openLockedFileChannel(file));
	}



	public static StorageLockedFile New(final File file, final FileLock lock)
	{
		return new StorageLockedFile.Default(
			notNull(file),
			notNull(lock)
		);
	}

	public class Default implements StorageLockedFile
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final File file;

		/*
		 * note that the channel's position is always implicitely at the end of the file
		 * as write and truncate automatically update it and transfer and read use a transient position value
		 */
		final FileLock    lock       ;
		final FileChannel fileChannel;
		
		private int users;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final File file, final FileLock lock)
		{
			super();
			this.file = file;
			this.lock = lock;
			
			// null channel required for SourceFileSlice tail dummy instances
			this.fileChannel = lock == null ? null : lock.channel();
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		public final File file()
		{
			return this.file;
		}

		@Override
		public final FileChannel fileChannel()
		{
			return this.fileChannel;
		}
		
		@Override
		public String qualifier()
		{
			return this.file.getParent();
		}
		
		@Override
		public String identifier()
		{
			return this.file.getPath();
		}
		
		@Override
		public String name()
		{
			return this.file.getName();
		}
		
		@Override
		public boolean delete()
		{
			this.close();
			return this.file.delete();
		}
		
		@Override
		public boolean exists()
		{
			return this.file.exists();
		}

		@Override
		public final synchronized void close()
		{
			if(this.fileChannel.isOpen())
			{
				try
				{
					this.lock.release();
					this.fileChannel.close();
				}
				catch(final IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		


		@Override
		public synchronized int incrementUserCount()
		{
			return ++this.users;
		}

		@Override
		public synchronized boolean decrementUserCount()
		{
			return --this.users == 0;
		}

		@Override
		public synchronized boolean hasNoUsers()
		{
			return this.users == 0;
		}

		@Override
		public String toString()
		{
			return this.file + " (" + this.file.length() + ")";
		}

	}

}
