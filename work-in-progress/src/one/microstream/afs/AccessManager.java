package one.microstream.afs;

import java.util.function.Function;

import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;

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
	
	
	
	
	
	
	public default Object defaultuser()
	{
		return Thread.currentThread();
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
		final ADirectory                      directory,
		final Function<? super ADirectory, R> logic
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
		
		private final S                           fileSystem         ;
		private final HashEnum<ADirectory>        usedDirectories    ;
		private final HashEnum<ADirectory>        mutatingDirectories;
		private final HashTable<AFile, FileEntry> fileUsers          ;
				
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
			this.usedDirectories     = HashEnum.New() ;
			this.mutatingDirectories = HashEnum.New() ;
			this.fileUsers           = HashTable.New();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final S fileSystem()
		{
			return this.fileSystem;
		}

		@Override
		public final synchronized boolean isUsed(
			final ADirectory directory
		)
		{
			return this.usedDirectories.contains(ADirectory.actual(directory));
		}
		
		@Override
		public final synchronized boolean isMutating(
			final ADirectory directory
		)
		{
			return this.mutatingDirectories.contains(ADirectory.actual(directory));
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
			
			return e.exclusive != null || !e.sharedUsers.isEmpty();
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
			
			return e.exclusive != null;
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
			
			return e.exclusive == user || e.sharedUsers.get(user) != null;
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
			
			return e.exclusive != null && e.exclusive.user() == user;
		}
				
		@Override
		public final synchronized AReadableFile useReading(
			final AFile  file,
			final Object user
		)
		{
			final FileEntry e = this.fileUsers.get(file);
			if(e == null)
			{
				final AReadableFile wrapper = this.wrapForReading(file, user);
				this.fileUsers.add(file, new FileEntry(wrapper));
				
				return wrapper;
			}
			
			if(e.exclusive != null)
			{
				if(e.exclusive.user() == user)
				{
					return e.exclusive;
				}
				
				// (30.04.2020 TM)EXCP: proper exception
				throw new RuntimeException("File is exclusively used: " + file);
			}
			
			AReadableFile wrapper = e.sharedUsers.get(user);
			if(wrapper == null)
			{
				wrapper = this.wrapForReading(file, user);
				e.sharedUsers.add(user, wrapper);
			}
			
			return wrapper;
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
				final AWritableFile wrapper = this.wrapForWriting(file, user);
				this.fileUsers.add(file, new FileEntry(wrapper));
				
				return wrapper;
			}
			
			if(e.exclusive != null)
			{
				if(e.exclusive.user() == user)
				{
					return e.exclusive;
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

			final AWritableFile wrapper = this.wrapForWriting(file, user);
			e.exclusive = wrapper;
			
			return wrapper;
		}
		
		@Override
		public boolean unregister(final AReadableFile file)
		{
			// FIXME AccessManager.Abstract#unregister()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public boolean unregister(final AWritableFile file)
		{
			// FIXME AccessManager.Abstract#unregister()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		protected abstract AReadableFile wrapForReading(AFile file, Object user);
		
		protected abstract AWritableFile wrapForWriting(AFile file, Object user);
		
	}
	
}