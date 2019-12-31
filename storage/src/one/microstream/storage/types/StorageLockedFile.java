package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

import one.microstream.collections.XArrays;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.XIO;
import one.microstream.storage.exceptions.StorageException;


public interface StorageLockedFile extends StorageFile //, AutoCloseable
{
	// can't implement AutoCloseable because the naive resource warnings are idiotic.
	
	@Override
	public default long length()
	{
		return XIO.unchecked.size(this.fileChannel());
	}

	public boolean hasUsers();
	
	public boolean executeIfUnsued(Consumer<? super StorageLockedFile> action);
	
	public boolean registerUsage(StorageFileUser fileUser);
	
	public boolean clearUsages(StorageFileUser fileUser);
	
	public boolean unregisterUsage(StorageFileUser fileUser);
		
	public boolean unregisterUsageClosing(StorageFileUser fileUser, Consumer<? super StorageLockedFile> closingAction);
	
	@Override
	public void close();
	
	public boolean tryClose();
	
		
	

//	@SuppressWarnings("resource") // resource closed internally by FileChannel (JDK tricking Java compiler ^^)
	public static FileLock openLockedFileChannel(final Path file)
	{
		// the file is always completely and unshared locked.
		final FileLock lock;
		FileChannel channel = null;
		try
		{
			channel = XIO.openFileChannelRW(file, StandardOpenOption.CREATE);
//			channel = XIO.openFileChannelRW(XIO.unchecked.ensureDirectoryAndFile(file));

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
			if(lock == null)
			{
				// (29.11.2019 TM)EXCP: proper exception
				throw new StorageException("File seems to be already locked: " + file);
			}
			channel.position(channel.size());
		}
		catch(final IOException e)
		{
			XIO.unchecked.close(channel, e);
			
			// (28.06.2014 TM)EXCP: proper exception
			throw new StorageException("Cannot obtain lock for file " + file, e);
		}

		return lock;
	}

	public static StorageLockedFile openLockedFile(final Path file)
	{
		return StorageLockedFile.New(file, openLockedFileChannel(file));
	}



	public static StorageLockedFile New(final Path file, final FileLock lock)
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

		final Path file;

		/*
		 * note that the channel's position is always implicitely at the end of the file
		 * as write and truncate automatically update it and transfer and read use a transient position value
		 */
		final FileLock    lock       ;
		final FileChannel fileChannel;
		
		private Usage[] usages     = new Usage[1];
		private int     usagesSize = 0;
		
		static final class Usage
		{
			StorageFileUser user ;
			int             count;
			
			Usage(final StorageFileUser user)
			{
				super();
				this.user = user;
			}
			
			final boolean increment()
			{
				return ++this.count == 1;
			}
			
			final boolean decrement()
			{
				return --this.count == 0;
			}
			
		}



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final Path file, final FileLock lock)
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

		public final Path file()
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
			return XIO.getFilePath(this.file.getParent());
		}
		
		@Override
		public String identifier()
		{
			return XIO.getFilePath(this.file);
		}
		
		@Override
		public String name()
		{
			return XIO.getFileName(this.file);
		}
		
		@Override
		public boolean delete()
		{
			this.close();
			return XIO.unchecked.delete(this.file);
		}
		
		@Override
		public boolean exists()
		{
			return XIO.unchecked.exists(this.file);
		}

		@Override
		public synchronized final void close()
		{
			if(this.hasUsers())
			{
				// (29.11.2019 TM)EXCP: proper exception
				throw new StorageException(
					this.getClass().getCanonicalName() + " still has registered users and cannot be closed: " + this
				);
			}
			
			this.internalClose();
		}
		
		@Override
		public synchronized final boolean tryClose()
		{
			if(this.hasUsers())
			{
				return false;
			}
			
			this.internalClose();
			return true;
		}
		
		
		
		final void internalClose()
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
					throw new IORuntimeException(e);
				}
			}
		}
		

		private Usage ensureEntry(final StorageFileUser fileUser)
		{
			/* note: for very short arrays (~ 5 references), arrays are faster than hash tables.
			 * The planned/expected user count for storage files should be something around 2-3.
			 */
			for(int i = 0; i < this.usagesSize; i++)
			{
				if(this.usages[i].user == fileUser)
				{
					return this.usages[i];
				}
			}
			
			if(this.usagesSize >= this.usages.length)
			{
				this.usages = XArrays.enlarge(this.usages, this.usages.length * 2);
			}
			
			return this.usages[this.usagesSize++] = new Usage(fileUser);
		}
		

		@Override
		public synchronized final boolean hasUsers()
		{
			return this.usagesSize != 0;
		}
		
		@Override
		public synchronized final boolean executeIfUnsued(final Consumer<? super StorageLockedFile> action)
		{
			if(this.hasUsers())
			{
				return false;
			}
			
			action.accept(this);
			
			return true;
		}

		@Override
		public synchronized final boolean registerUsage(final StorageFileUser fileUser)
		{
			return this.ensureEntry(fileUser).increment();
		}
		
		@Override
		public synchronized final boolean clearUsages(final StorageFileUser fileUser)
		{
			for(int i = 0; i < this.usagesSize; i++)
			{
				if(this.usages[i].user == fileUser)
				{
					XArrays.removeFromIndex(this.usages, this.usagesSize--, i);
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public synchronized final boolean unregisterUsage(final StorageFileUser fileUser)
		{
			for(int i = 0; i < this.usagesSize; i++)
			{
				if(this.usages[i].user == fileUser)
				{
					if(this.usages[i].decrement())
					{
						XArrays.removeFromIndex(this.usages, this.usagesSize--, i);
						return true;
					}
					
					return false;
				}
			}
			
			// (29.11.2019 TM)EXCP: proper exception
			throw new StorageException(StorageFileUser.class.getSimpleName() + " not found " + fileUser);
		}
				
		@Override
		public boolean unregisterUsageClosing(
			final StorageFileUser                     fileUser     ,
			final Consumer<? super StorageLockedFile> closingAction
		)
		{
			if(this.unregisterUsage(fileUser) && !this.hasUsers())
			{
				if(closingAction != null)
				{
					closingAction.accept(this);
				}
				
				this.internalClose();
				return true;
			}
			
			return false;
		}

		@Override
		public String toString()
		{
			return this.file + " (" + XIO.unchecked.size(this.fileChannel) + ")";
		}

	}

}
