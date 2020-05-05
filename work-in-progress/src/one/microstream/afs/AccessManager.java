package one.microstream.afs;

import java.util.function.Function;

import one.microstream.collections.HashTable;

public interface AccessManager
{
	public boolean isUsed(ADirectory directory);
	
	public boolean isMutating(ADirectory directory);

	public boolean isReading(AFile file);
	
	public boolean isWriting(AFile file);
	
	
	
	public boolean isUsed(ADirectory directory, Object user);
	
	public boolean isMutating(ADirectory directory, Object user);

	public boolean isReading(AFile file, Object user);
	
	public boolean isWriting(AFile file, Object user);
	
	// (29.04.2020 TM)TODO: priv#49: executeIfNot~ methods? Or coverable by execute~ methods below?
	

	
	public AUsedDirectory use(ADirectory directory, Object user);
	
	public AMutableDirectory useMutating(ADirectory directory, Object user);
	
	public AReadableFile useReading(AFile file, Object user);
	
	public AWritableFile useWriting(AFile file, Object user);
	
	
	
	
	
	
	public default Object defaultuser()
	{
		return Thread.currentThread();
	}
	
	public default AUsedDirectory use(final ADirectory directory)
	{
		return this.use(directory, this.defaultuser());
	}
	
	public default AMutableDirectory useMutating(final ADirectory directory)
	{
		return this.useMutating(directory, this.defaultuser());
	}
	
	public default AReadableFile useReading(final AFile file)
	{
		return this.useReading(file, this.defaultuser());
	}
	
	public default AWritableFile useWriting(final AFile file)
	{
		return this.useWriting(file, this.defaultuser());
	}

	public default <R> R executeMutating(
		final ADirectory                             directory,
		final Function<? super AMutableDirectory, R> logic
	)
	{
		return this.executeMutating(directory, this.defaultuser(), logic);
	}

	public default <R> R executeWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return this.executeWriting(file, this.defaultuser(), logic);
	}
	
	public default <R> R executeMutating(
		final ADirectory                             directory,
		final Object                                 user    ,
		final Function<? super AMutableDirectory, R> logic
	)
	{
		synchronized(directory)
		{
			final boolean isUsed = this.isUsed(directory, user);
			
			final AMutableDirectory mDirectory = this.useMutating(directory, user);
			
			try
			{
				return logic.apply(mDirectory);
			}
			finally
			{
				if(isUsed)
				{
					mDirectory.releaseMutating();
				}
				else
				{
					mDirectory.release();
				}
			}
		}
	}
	
	public default <R> R executeWriting(
		final AFile                              file ,
		final Object                             user,
		final Function<? super AWritableFile, R> logic
	)
	{
		synchronized(file)
		{
			final boolean isReading = this.isReading(file, user);
			
			final AWritableFile mFile = this.useWriting(file, user);
			
			try
			{
				return logic.apply(mFile);
			}
			finally
			{
				if(isReading)
				{
					mFile.releaseWriting();
				}
				else
				{
					mFile.release();
				}
			}
		}
	}
	
	
	public final class Default implements AccessManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFileSystem                      fileSystem    ;
		private final HashTable<ADirectory, DirEntry > directoryUsers;
		private final HashTable<AFile     , FileEntry> fileUsers     ;
		
		static final class DirEntry
		{
			final HashTable<Object, AUsedDirectory> sharedUsers = HashTable.New();
			
			Object            exclusiveUser   ;
			AMutableDirectory exclusiveWrapper;
			
			DirEntry(final Object user, final AUsedDirectory wrapper)
			{
				super();
				this.sharedUsers.add(user, wrapper);
			}
			
			DirEntry(final Object user, final AMutableDirectory wrapper)
			{
				super();
				this.exclusiveUser    = user   ;
				this.exclusiveWrapper = wrapper;
			}
			
		}
		
		static final class FileEntry
		{
			final HashTable<Object, AReadableFile> sharedUsers = HashTable.New();
			
			Object        exclusiveUser   ;
			AWritableFile exclusiveWrapper;

			
			FileEntry(final Object user, final AReadableFile wrapper)
			{
				super();
				this.sharedUsers.add(user, wrapper);
			}
			
			FileEntry(final Object user, final AWritableFile wrapper)
			{
				super();
				this.exclusiveUser    = user   ;
				this.exclusiveWrapper = wrapper;
			}
			
		}
		
		

		
		Default(
			final AFileSystem                      fileSystem    ,
			final HashTable<ADirectory, DirEntry > directoryUsers,
			final HashTable<AFile     , FileEntry> fileUsers
		)
		{
			super();
			this.fileSystem     = fileSystem    ;
			this.directoryUsers = directoryUsers;
			this.fileUsers      = fileUsers     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized boolean isUsed(
			final ADirectory directory
		)
		{
			final DirEntry e = this.directoryUsers.get(directory);
			if(e == null)
			{
				return false;
			}
			
			return e.exclusiveUser != null || !e.sharedUsers.isEmpty();
		}
		
		@Override
		public final synchronized boolean isMutating(
			final ADirectory directory
		)
		{
			final DirEntry e = this.directoryUsers.get(directory);
			if(e == null)
			{
				return false;
			}
			
			return e.exclusiveUser != null;
		}

		@Override
		public final synchronized boolean isReading(
			final AFile file
		)
		{
			final FileEntry e = this.fileUsers.get(file);
			if(e == null)
			{
				return false;
			}
			
			return e.exclusiveUser != null || !e.sharedUsers.isEmpty();
		}
		
		@Override
		public final synchronized boolean isWriting(
			final AFile file
		)
		{
			final FileEntry e = this.fileUsers.get(file);
			if(e == null)
			{
				return false;
			}
			
			return e.exclusiveUser != null;
		}
		
		
		
		@Override
		public final synchronized boolean isUsed(
			final ADirectory directory,
			final Object     user
		)
		{
			final DirEntry e = this.directoryUsers.get(directory);
			if(e == null)
			{
				return false;
			}
			
			return e.exclusiveUser == user || e.sharedUsers.get(user) != null;
		}
		
		@Override
		public final synchronized boolean isMutating(
			final ADirectory directory,
			final Object     user
		)
		{
			final DirEntry e = this.directoryUsers.get(directory);
			if(e == null)
			{
				return false;
			}
			
			return e.exclusiveUser == user;
		}

		@Override
		public final synchronized boolean isReading(
			final AFile  file,
			final Object user
		)
		{
			final FileEntry e = this.fileUsers.get(file);
			if(e == null)
			{
				return false;
			}
			
			return e.exclusiveUser == user || e.sharedUsers.get(user) != null;
		}
		
		@Override
		public final synchronized boolean isWriting(
			final AFile  file,
			final Object user
		)
		{
			final FileEntry e = this.fileUsers.get(file);
			if(e == null)
			{
				return false;
			}
			
			return e.exclusiveUser == user;
		}
				
		@Override
		public final synchronized AUsedDirectory use(
			final ADirectory directory,
			final Object     user
		)
		{
			final DirEntry e = this.directoryUsers.get(directory);
			if(e == null)
			{
				final AUsedDirectory wrapper = this.wrapForUse(directory);
				this.directoryUsers.add(directory, new DirEntry(user, wrapper));
				
				return wrapper;
			}
			
			if(e.exclusiveUser != null)
			{
				if(e.exclusiveUser == user)
				{
					return e.exclusiveWrapper;
				}
				
				// (30.04.2020 TM)EXCP: proper exception
				throw new RuntimeException("Directory is exclusively used: " + directory);
			}
			
			AUsedDirectory wrapper = e.sharedUsers.get(user);
			if(wrapper == null)
			{
				wrapper = this.wrapForUse(directory);
				e.sharedUsers.add(user, wrapper);
			}
			
			return wrapper;
		}
		
		private AUsedDirectory wrapForUse(final ADirectory directory)
		{
			return AUsedDirectory.New(this.fileSystem, directory);
		}
		
		private AMutableDirectory wrapForMutation(final ADirectory directory)
		{
			return AMutableDirectory.New(this.fileSystem, directory);
		}
		
		@Override
		public final synchronized AMutableDirectory useMutating(
			final ADirectory directory,
			final Object     user
		)
		{
			final DirEntry e = this.directoryUsers.get(directory);
			if(e == null)
			{
				final AMutableDirectory wrapper = this.wrapForMutation(directory);
				this.directoryUsers.add(directory, new DirEntry(user, wrapper));
				
				return wrapper;
			}
			
			if(e.exclusiveUser != null)
			{
				if(e.exclusiveUser == user)
				{
					return e.exclusiveWrapper;
				}
				
				// (30.04.2020 TM)EXCP: proper exception
				throw new RuntimeException("Directory is exclusively used: " + directory);
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

			final AMutableDirectory wrapper = this.wrapForMutation(directory);
			e.exclusiveUser = user;
			e.exclusiveWrapper = wrapper;
			
			return wrapper;
		}
		
		
		@Override
		public final synchronized AReadableFile useReading(
			final AFile  file ,
			final Object user
		)
		{
			final FileEntry e = this.fileUsers.get(file);
			if(e == null)
			{
				final AReadableFile wrapper = this.wrapForReading(file);
				this.fileUsers.add(file, new FileEntry(user, wrapper));
				
				return wrapper;
			}
			
			if(e.exclusiveUser != null)
			{
				if(e.exclusiveUser == user)
				{
					return e.exclusiveWrapper;
				}
				
				// (30.04.2020 TM)EXCP: proper exception
				throw new RuntimeException("File is exclusively used: " + file);
			}
			
			AReadableFile wrapper = e.sharedUsers.get(user);
			if(wrapper == null)
			{
				wrapper = this.wrapForReading(file);
				e.sharedUsers.add(user, wrapper);
			}
			
			return wrapper;
		}
		
		private AReadableFile wrapForReading(final AFile file)
		{
			return AReadableFile.New(this.fileSystem, file);
		}
		
		private AWritableFile wrapForWriting(final AFile file)
		{
			return AWritableFile.New(this.fileSystem, file);
		}
		
		@Override
		public final synchronized AWritableFile useWriting(
			final AFile  file,
			final Object user
		)
		{
			final FileEntry e = this.fileUsers.get(file);
			if(e == null)
			{
				final AWritableFile wrapper = this.wrapForWriting(file);
				this.fileUsers.add(file, new FileEntry(user, wrapper));
				
				return wrapper;
			}
			
			if(e.exclusiveUser != null)
			{
				if(e.exclusiveUser == user)
				{
					return e.exclusiveWrapper;
				}
				
				// (30.04.2020 TM)EXCP: proper exception
				throw new RuntimeException("File is exclusively used: " + file);
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

			final AWritableFile wrapper = this.wrapForWriting(file);
			e.exclusiveUser = user;
			e.exclusiveWrapper = wrapper;
			
			return wrapper;
		}
		
	}
	
}