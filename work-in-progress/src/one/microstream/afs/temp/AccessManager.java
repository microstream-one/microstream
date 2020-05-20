package one.microstream.afs.temp;

import java.util.function.Function;

import one.microstream.chars.XChars;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.interfaces.OptimizableCollection;

public interface AccessManager
{
	public AFileSystem fileSystem();
	
	public boolean isUsed(ADirectory directory);
	
	public boolean isMutating(ADirectory directory);

	public boolean isReading(AFile file);
	
	public boolean isWriting(AFile file);
	
	

	public boolean isReading(AFile file, Object user);
	
	public boolean isWriting(AFile file, Object user);
	
	// (29.04.2020 TM)TODO: priv#49: executeIfNot~ methods? Or coverable by execute~ methods below?
	
	
	public AReadableFile useReading(AFile file, Object user);
	
	public AWritableFile useWriting(AFile file, Object user);
	
	
	public boolean unregister(AReadableFile file);
	
	public boolean unregister(AWritableFile file);
	
	
	
	
	
	
	public default Object defaultUser()
	{
		return Thread.currentThread();
	}
		
	public default AReadableFile useReading(final AFile file)
	{
		return this.useReading(file, this.defaultUser());
	}
	
	public default AWritableFile useWriting(final AFile file)
	{
		return this.useWriting(file, this.defaultUser());
	}

	public default <R> R executeMutating(
		final ADirectory                      directory,
		final Function<? super ADirectory, R> logic
	)
	{
		return this.executeMutating(directory, this.defaultUser(), logic);
	}

	public default <R> R executeWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return this.executeWriting(file, this.defaultUser(), logic);
	}
	
	public default <R> R executeMutating(
		final ADirectory                      directory,
		final Object                          user    ,
		final Function<? super ADirectory, R> logic
	)
	{
		// (07.05.2020 TM)FIXME: priv#49: overhaul for new concept
		throw new one.microstream.meta.NotImplementedYetError();
//		synchronized(directory)
//		{
//			final boolean isUsed = this.isUsed(directory);
//
//			final ADirectory mDirectory = this.useMutating(directory);
//
//			try
//			{
//				return logic.apply(mDirectory);
//			}
//			finally
//			{
//				if(isUsed)
//				{
//					mDirectory.releaseMutating();
//				}
//				else
//				{
//					mDirectory.release();
//				}
//			}
//		}
	}
	
	public default <R> R executeWriting(
		final AFile                              file ,
		final Object                             user,
		final Function<? super AWritableFile, R> logic
	)
	{
		synchronized(file)
		{
			final AWritableFile mFile = this.useWriting(file, user);
			
			try
			{
				return logic.apply(mFile);
			}
			finally
			{
				mFile.release();
			}
		}
	}
	
	
	public interface Creator
	{
		public AccessManager createAccessManager(AFileSystem parent);
	}
	
	
	public abstract class Abstract<S extends AFileSystem> implements AccessManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final S                               fileSystem         ;
		private final HashTable<ADirectory, DirEntry> usedDirectories    ;
		private final HashEnum<ADirectory>            mutatingDirectories;
		private final HashTable<AFile, FileEntry>     fileUsers          ;
		
		static final class DirEntry
		{
			final ADirectory directory;
			int usingChildCount;
			
			DirEntry(final ADirectory directory)
			{
				super();
				this.directory = directory;
			}
			
		}
		
		static final class FileEntry
		{
			final HashTable<Object, AReadableFile> sharedUsers = HashTable.New();
			
			AWritableFile exclusive;
			
			FileEntry(final AReadableFile wrapper)
			{
				super();
				this.sharedUsers.add(wrapper.user(), wrapper);
			}
			
			FileEntry(final AWritableFile wrapper)
			{
				super();
				this.exclusive = wrapper;
			}
			
		}
		
		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final S fileSystem)
		{
			super();
			this.fileSystem          = fileSystem     ;
			this.usedDirectories     = HashTable.New();
			this.mutatingDirectories = HashEnum.New() ;
			this.fileUsers           = HashTable.New();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final Object mutex()
		{
			return this.fileSystem;
		}
		
		@Override
		public final S fileSystem()
		{
			return this.fileSystem;
		}

		@Override
		public boolean isUsed(
			final ADirectory directory
		)
		{
			synchronized(this.mutex())
			{
				return this.usedDirectories.get(ADirectory.actual(directory)) != null;
			}
		}
		
		@Override
		public boolean isMutating(
			final ADirectory directory
		)
		{
			synchronized(this.mutex())
			{
				return this.mutatingDirectories.contains(ADirectory.actual(directory));
			}
		}

		@Override
		public boolean isReading(
			final AFile file
		)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(file);
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null || !e.sharedUsers.isEmpty();
			}
		}
		
		@Override
		public boolean isWriting(
			final AFile file
		)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(file);
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null;
			}
		}
		
		@Override
		public boolean isReading(
			final AFile  file,
			final Object user
		)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(file);
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive == user || e.sharedUsers.get(user) != null;
			}
		}
		
		@Override
		public boolean isWriting(
			final AFile  file,
			final Object user
		)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(file);
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null && e.exclusive.user() == user;
			}
		}
				
		@Override
		public AReadableFile useReading(
			final AFile  file,
			final Object user
		)
		{
			synchronized(this.mutex())
			{
				final AFile actual = AFile.actual(file);
				final FileEntry e = this.fileUsers.get(actual);
				if(e == null)
				{
					final AReadableFile wrapper = this.synchRegisterReading(actual, user);
					this.fileUsers.add(actual, new FileEntry(wrapper));
					
					return wrapper;
				}
				
				if(e.exclusive != null)
				{
					if(e.exclusive.user() == user)
					{
						return e.exclusive;
					}
					
					// (30.04.2020 TM)EXCP: proper exception
					throw new RuntimeException("File is exclusively used: " + actual);
				}
				
				AReadableFile wrapper = e.sharedUsers.get(user);
				if(wrapper == null)
				{
					wrapper = this.synchRegisterReading(actual, user);
					e.sharedUsers.add(user, wrapper);
				}
				
				return wrapper;
			}
		}
		
		private AReadableFile synchRegisterReading(
			final AFile  actual,
			final Object user
		)
		{
			final AReadableFile wrapper = this.wrapForReading(actual, user);
			this.incrementDirectoryUsageCount(actual.parent());
			
			return wrapper;
		}
				
		protected final void incrementDirectoryUsageCount(final ADirectory directory)
		{
			DirEntry entry = this.usedDirectories.get(directory);
			if(entry == null)
			{
				entry = this.addUsedDirectoryEntry(directory);
				
				// new entry means increment usage count for parent incrementally
				if(directory.parent() != null)
				{
					this.incrementDirectoryUsageCount(directory);
				}
			}
			
			entry.usingChildCount++;
			
			// note: child count incrementation on one level does not concern the parent directory count.
		}
		
		private DirEntry addUsedDirectoryEntry(final ADirectory directory)
		{
			final DirEntry entry;
			this.usedDirectories.add(directory, entry = new DirEntry(directory));
			
			return entry;
		}
		
		@Override
		public AWritableFile useWriting(
			final AFile  file,
			final Object user
		)
		{
			synchronized(this.mutex())
			{
				final AFile actual = AFile.actual(file);
				final FileEntry e = this.fileUsers.get(actual);
				if(e == null)
				{
					final AWritableFile wrapper = this.synchRegisterWriting(actual, user);
					this.fileUsers.add(actual, new FileEntry(wrapper));
					
					return wrapper;
				}
				
				if(e.exclusive != null)
				{
					if(e.exclusive.user() == user)
					{
						return e.exclusive;
					}
					
					// (30.04.2020 TM)EXCP: proper exception
					throw new RuntimeException("File is exclusively used: " + actual);
				}
				
				if(!e.sharedUsers.isEmpty())
				{
					if(e.sharedUsers.size() > 1 || e.sharedUsers.get().key() != user)
					{
						// (30.04.2020 TM)EXCP: priv#49: proper exception
						throw new RuntimeException();
					}
					e.sharedUsers.removeFor(user);
				}
	
				final AWritableFile wrapper = this.synchRegisterWriting(actual, user);
				e.exclusive = wrapper;
				
				return wrapper;
			}
		}

		
		private AWritableFile synchRegisterWriting(
			final AFile  actual,
			final Object user
		)
		{
			final AWritableFile wrapper = this.wrapForWriting(actual, user);
			this.incrementDirectoryUsageCount(actual.parent());
			
			return wrapper;
		}
		
		@Override
		public boolean unregister(final AReadableFile file)
		{
			synchronized(this.mutex())
			{
				return this.internalUnregister(file);
			}
		}
		
		@Override
		public boolean unregister(final AWritableFile file)
		{
			synchronized(this.mutex())
			{
				// logic has to cover writing case, anyway.
				return this.internalUnregister(file);
			}
		}
		
		protected boolean internalUnregister(final AReadableFile file)
		{
			final AFile actual = file.actual();
			final FileEntry e = this.fileUsers.get(actual);
			if(e == null)
			{
				return false;
			}
			
			if(!this.internalUnregister(file, e))
			{
				return false;
			}
			
			this.decrementDirectoryUsageCount(actual.parent());
			optimizeMemoryUsage(this.fileUsers);
			
			
			return true;
		}
		
		private static void optimizeMemoryUsage(final OptimizableCollection collection)
		{
			if((collection.size() & 127) != 0)
			{
				return;
			}

			collection.optimize();
		}
		
		protected void decrementDirectoryUsageCount(final ADirectory directory)
		{
			final DirEntry entry = this.getNonNullDirEntry(directory);
			if(--entry.usingChildCount == 0)
			{
				if(directory.parent() != null)
				{
					this.decrementDirectoryUsageCount(directory.parent());
				}
				this.usedDirectories.removeFor(directory);
			}
		}
		
		protected final DirEntry getNonNullDirEntry(final ADirectory directory)
		{
			final DirEntry entry = this.usedDirectories.get(directory);
			if(entry == null)
			{
				// (20.05.2020 TM)EXCP: proper exception
				throw new RuntimeException("Directory not registered as used: " + directory.path());
			}
			
			return entry;
		}
		
		protected final DirEntry ensureDirEntry(final ADirectory directory)
		{
			return this.usedDirectories.ensure(directory, DirEntry::new);
		}
		
		protected boolean internalUnregister(final AReadableFile file, final FileEntry entry)
		{
			// AWritableFile "is a" AReadableFile, so it could be passed here and must be covered as well.
			if(this.unregisterIfExclusive(file, entry))
			{
				// exclusive entries never have a shared entry (since they are not shared), so abort here.
				return true;
			}
			
			return this.unregisterReading(file, entry);
		}

		protected boolean unregisterReading(final AReadableFile file, final FileEntry entry)
		{
			final AReadableFile removed = entry.sharedUsers.removeFor(file.user());
			if(removed == null)
			{
				return false;
			}
			
			// should never happen since creation/registration checks for that
			if(removed != file)
			{
				// (13.05.2020 TM)EXCP: proper exception
				throw new RuntimeException(
					"Inconsistency detected: "
					+ AReadableFile.class.getSimpleName() + " " + XChars.systemString(file)
					+ " is not the same as removed  "
					+ AReadableFile.class.getSimpleName() + " " + XChars.systemString(removed)
					+ "."
				);
			}
			
			return true;
		}
		
		protected boolean unregisterIfExclusive(final AReadableFile file, final FileEntry entry)
		{
			// AWritableFile "is a" AReadableFile
			if(entry.exclusive != file)
			{
				return false;
			}
			entry.exclusive = null;
			
			return true;
		}
		
		protected abstract AReadableFile wrapForReading(AFile file, Object user);
		
		protected abstract AWritableFile wrapForWriting(AFile file, Object user);
		
	}
	
}