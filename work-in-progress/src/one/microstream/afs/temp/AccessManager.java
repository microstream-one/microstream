package one.microstream.afs.temp;

import java.util.function.Function;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.HashTable;
import one.microstream.collections.XArrays;
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
	
	// (29.04.2020 TM)TODO: priv#49: execute~IfPossible methods? Or coverable by execute~ methods below?

	public default <R> R executeWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return this.executeWriting(file, this.defaultUser(), logic);
	}
	
	public <R> R executeMutating(
		ADirectory                      directory,
		Function<? super ADirectory, R> logic
	);
	
	public default <R> R executeWriting(
		final AFile                              file ,
		final Object                             user,
		final Function<? super AWritableFile, R> logic
	)
	{
		// no locking needed, here since the implementation of #useWriting has to cover that
		final AWritableFile writableFile = this.useWriting(file, user);
		try
		{
			return logic.apply(writableFile);
		}
		finally
		{
			writableFile.release();
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
		private final HashTable<ADirectory, Thread>   mutatingDirectories;
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
			private static final AReadableFile[] NO_SHARED_USERS = new AReadableFile[0];
		
			private AReadableFile[] sharedUsers;
			
			AWritableFile exclusive;
			
			FileEntry(final AReadableFile wrapper)
			{
				super();
				this.sharedUsers = X.Array(wrapper);
			}
			
			FileEntry(final AWritableFile wrapper)
			{
				super();
				this.exclusive = wrapper;
				this.sharedUsers = NO_SHARED_USERS;
			}
			
			final boolean hasSharedUsers()
			{
				return this.sharedUsers != NO_SHARED_USERS;
			}
			
			final boolean isSoleUser(final Object user)
			{
				return this.sharedUsers.length == 1 && this.sharedUsers[0].user() == user;
			}
			
			final AReadableFile getForUser(final Object user)
			{
				for(final AReadableFile f : this.sharedUsers)
				{
					if(f.user() == user)
					{
						return f;
					}
				}
				
				return null;
			}
			
			private int indexForUser(final Object user)
			{
				return XArrays.indexOf(user, this.sharedUsers, FileEntry::isForUser);
			}
			
			static final boolean isForUser(final AReadableFile file, final Object user)
			{
				return file.user() == user;
			}
			
			final void add(final AReadableFile file)
			{
				// best performance and common case for first user
				if(this.sharedUsers == NO_SHARED_USERS)
				{
					this.sharedUsers = X.Array(file);
					return;
				}

				// general case: if not yet contained, add.
				if(this.getForUser(file.user()) == null)
				{
					this.sharedUsers = XArrays.add(this.sharedUsers, file);
					return;
				}

				// already contained
			}
			
			final boolean remove(final AReadableFile file)
			{
				if(this.sharedUsers.length == 1 && this.sharedUsers[0] == file)
				{
					this.sharedUsers = NO_SHARED_USERS;
					return true;
				}
				
				final int index = this.indexForUser(file.user());
				if(index < 0)
				{
					return false;
				}
				
				// should never happen since creation/registration checks for that
				if(this.sharedUsers[index] != file)
				{
					// (13.05.2020 TM)EXCP: proper exception
					throw new RuntimeException(
						"Inconsistency detected: to be removed file "
						+ AReadableFile.class.getSimpleName() + " " + XChars.systemString(file)
						+ " is not the same as the one contained for the same user: "
						+ AReadableFile.class.getSimpleName() + " " + XChars.systemString(this.sharedUsers[index])
						+ "."
					);
				}
				
				this.removeIndex(index);
				
				return true;
			}
			
			final void removeForUser(final Object user)
			{
				final int index = this.indexForUser(user);
				if(index < 0)
				{
					return;
				}

				this.removeIndex(index);
			}
			
			private void removeIndex(final int index)
			{
				// must enforce use of empty constant in any case.
				if(index == 0 && this.sharedUsers.length == 1)
				{
					this.sharedUsers = NO_SHARED_USERS;
				}
				else
				{
					this.sharedUsers = XArrays.remove(this.sharedUsers, index);
				}
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
			this.mutatingDirectories = HashTable.New();
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
				return this.mutatingDirectories.keys().contains(ADirectory.actual(directory));
			}
		}

		@Override
		public boolean isReading(final AFile file)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null || e.hasSharedUsers();
			}
		}
		
		@Override
		public boolean isWriting(final AFile file)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
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
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive == user || e.getForUser(user) != null;
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
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null && e.exclusive.user() == user;
			}
		}
				
		@Override
		public final <R> R executeMutating(
			final ADirectory                      directory,
			final Function<? super ADirectory, R> logic
		)
		{
			synchronized(this.mutex())
			{
				final ADirectory actual = ADirectory.actual(directory);
				
				// Step 1: check for already existing mutating entry
				final Thread mutatingThread = this.mutatingDirectories.get(actual);
				if(mutatingThread == Thread.currentThread())
				{
					// execute logic WITHOUT removing logic since the call is obviously nested.
					return logic.apply(directory);
				}
				if(mutatingThread != null)
				{
					// (22.05.2020 TM)EXCP: proper exception
					throw new RuntimeException(
						"Directory " + actual.path() + " already used for mutation by \"" + mutatingThread + "\"."
					);
				}
				
				// Step 2: check for already existing using entry
				final Object user = this.usedDirectories.get(actual);
				if(user != null && user != Thread.currentThread())
				{
					// (22.05.2020 TM)EXCP: proper exception
					throw new RuntimeException(
						"Directory " + actual.path() + " already used by \"" + user + "\"."
					);
				}
				
				// Step 3: create mutating entry, execute logic, remove entry in any case.
				this.mutatingDirectories.add(actual, Thread.currentThread());
				try
				{
					return logic.apply(directory);
				}
				finally
				{
					this.mutatingDirectories.removeFor(actual);
				}
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
				
				AReadableFile wrapper = e.getForUser(user);
				if(wrapper == null)
				{
					wrapper = this.synchRegisterReading(actual, user);
					e.add(wrapper);
				}
				
				return wrapper;
			}
		}
		
		private AReadableFile synchRegisterReading(
			final AFile  actual,
			final Object user
		)
		{
			this.checkForMutatingParents(actual, user);
			this.incrementDirectoryUsageCount(actual.parent());

			return this.wrapForReading(actual, user);
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
				
				if(e.hasSharedUsers())
				{
					if(e.isSoleUser(user))
					{
						e.removeForUser(user);
					}
					else
					{
						// (30.04.2020 TM)TODO: priv#49: proper exception
						throw new RuntimeException();
					}
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
			this.checkForMutatingParents(actual, user);
			this.incrementDirectoryUsageCount(actual.parent());
						
			return this.wrapForWriting(actual, user);
		}
		
		private void checkForMutatingParents(final AFile actual, final Object user)
		{
			for(ADirectory p = actual.parent(); p != null; p = p.parent())
			{
				if(this.mutatingDirectories.get(p) != user)
				{
					// (21.05.2020 TM)EXCP: proper exception
					throw new RuntimeException(
						"File \"" + actual.path()
						+ "\" cannot be accessed by user \"" + user + "\" since directory \""
						+ p.path() + "\" is in the process of being changed by user \"" + user + "\"."
					);
				}
			}
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
				optimizeMemoryUsage(this.usedDirectories);
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
		
		protected boolean internalUnregister(final AReadableFile file, final FileEntry entry)
		{
			// AWritableFile "is a" AReadableFile, so it could be passed here and must be covered as well.
			if(this.unregisterIfExclusive(file, entry))
			{
				// exclusive entries never have a shared entry (since they are not shared), so abort here.
				return true;
			}
			
			return entry.remove(file);
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